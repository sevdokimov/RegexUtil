package com.ess.regexutil.ideaplugin;

import com.ess.regexutil.ideaplugin.utils.Utils;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.impl.BackgroundableProcessIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import com.intellij.util.concurrency.EdtScheduledExecutorService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class MatchingProcessor implements Disposable {

    private static final Logger log = Logger.getInstance(RegexpTesterPanel.class);

    public static int REHIGHLIGHT_DELAY = 30;

    private final Supplier<State> stateSupplier;
    private final Project project;

    private long rehighlightTime;

    private boolean disposed;

    private State state;
    private Future<?> matchingFuture;

    private MatchResult result;

    private final List<Consumer<MatchResult>> listeners = new CopyOnWriteArrayList<>();

    private Pair<RegexpAnalyzer, ProgressIndicator> analyzeTask;

    private final List<Runnable> analyzingListeners = new CopyOnWriteArrayList<>();

    public MatchingProcessor(Supplier<State> stateSupplier, @NotNull Project project) {
        this.stateSupplier = stateSupplier;
        this.project = project;
    }

    public void addListener(Consumer<MatchResult> listener) {
        listeners.add(listener);
    }

    public void addAnalyzingListener(Runnable listener) {
        analyzingListeners.add(listener);
    }

    private void notifyAnalyzingStateListeners() {
        ApplicationManager.getApplication().assertIsDispatchThread();

        for (Runnable listener : analyzingListeners) {
            listener.run();
        }
    }

    public boolean isResultReady() {
        ApplicationManager.getApplication().assertIsDispatchThread();

        return rehighlightTime == 0 && matchingFuture == null;
    }

    public void onStateChanged() {
        ApplicationManager.getApplication().assertIsDispatchThread();

        boolean rehighlightScheduled = rehighlightTime != 0;

        rehighlightTime = System.currentTimeMillis() + REHIGHLIGHT_DELAY;

        if (!rehighlightScheduled) {
            EdtScheduledExecutorService.getInstance().schedule(this::rehighlight, ModalityState.NON_MODAL, REHIGHLIGHT_DELAY, TimeUnit.MILLISECONDS);
            notifyListeners(null);
        }
    }

    private void notifyListeners(@Nullable MatchResult result) {
        if (disposed)
            return;

        for (Consumer<MatchResult> listener : listeners) {
            listener.accept(result);
        }
    }

    private void rehighlight() {
        if (disposed)
            return;

        long delay = rehighlightTime - System.currentTimeMillis();
        if (delay > 0) {
            EdtScheduledExecutorService.getInstance().schedule(this::rehighlight, ModalityState.NON_MODAL, delay, TimeUnit.MILLISECONDS);
            return;
        }

        rehighlightTime = 0;

        State newState = stateSupplier.get();

        if (newState.equals(state))
            return;

        state = newState;

        if (matchingFuture != null)
            matchingFuture.cancel(true);

        if (analyzeTask != null) {
            analyzeTask.second.cancel();
            analyzeTask = null;
            notifyAnalyzingStateListeners();
        }

        matchingFuture = ApplicationManager.getApplication().executeOnPooledThread(() -> {
            if (Thread.interrupted())
                return;

            MatchResult result;

            try {
                result = match(state);
            } catch (ProcessCanceledException e) {
                return;
            } catch (Exception e) {
                log.error("Failed to match text", e);
                result = new MatchResult(e);
            }

            if (Thread.interrupted())
                return;

            MatchResult resultFinal = result;

            ApplicationManager.getApplication().invokeLater(() -> {
                if (!newState.equals(state))
                    return;

                matchingFuture = null;

                if (rehighlightTime == 0) {
                    MatchingProcessor.this.result = resultFinal;
                    notifyListeners(resultFinal);
                }
            });
        });

        notifyListeners(null);
    }

    public MatchResult getResult() {
        return result;
    }

    private MatchResult match(State state) throws ProcessCanceledException {
        if (state.getRegexp().isEmpty())
            return null;

        Pattern pattern;

        try {
            pattern = Pattern.compile(state.getRegexp(), state.getFlags());
        } catch (PatternSyntaxException e) {
            return new MatchResult(e);
        }

        if (Thread.currentThread().isInterrupted())
            throw new ProcessCanceledException();

        return Utils.runCancelable("regexptester-matcher", null, () -> match0(state, pattern));
    }

    private MatchResult match0(@NotNull State state, Pattern pattern) {
        Matcher matcher = pattern.matcher(state.getText());

        List<MatchResult.Occurrence> groups = new ArrayList<>();
        String replaced = null;

        switch (state.getMatchType()) {
            case ENTIRE_STRING: {
                if (matcher.matches()) {
                    groups.add(new MatchResult.Occurrence(matcher, state.getGroupPositions()));
                }

                break;
            }

            case BEGINNING: {
                if (matcher.lookingAt()) {
                    groups.add(new MatchResult.Occurrence(matcher, state.getGroupPositions()));
                }

                break;
            }
            case REPLACE: {
                StringBuilder replacedBuff = new StringBuilder();
                int lastAppendPosition = 0;

                while (matcher.find()) {
                    int replacedStart = replacedBuff.length() + (matcher.start() - lastAppendPosition);

                    try {
                        matcher.appendReplacement(replacedBuff, state.getReplacement());
                    } catch (IllegalArgumentException | IndexOutOfBoundsException e) {
                        return new MatchResult(new IllegalArgumentException("Invalid replacement: " + e.getMessage(), e));
                    }

                    lastAppendPosition = matcher.end();

                    groups.add(new MatchResult.Occurrence(matcher, state.getGroupPositions(), new TextRange(replacedStart, replacedBuff.length())));
                    if (Thread.currentThread().isInterrupted())
                        return null;
                }

                replaced = replacedBuff.toString();

                break;
            }
            case SUBSTRING: {
                while (matcher.find()) {
                    groups.add(new MatchResult.Occurrence(matcher, state.getGroupPositions()));
                    if (Thread.currentThread().isInterrupted())
                        return null;
                }

                break;
            }

            default:
                throw new IllegalStateException();
        }

        return new MatchResult(state, groups, replaced);
    }


    @Override
    public void dispose() {
        if (disposed)
            return;

        disposed = true;
    }

    public void findUnmatched() {
        ApplicationManager.getApplication().assertIsDispatchThread();

        if (disposed || state == null || result == null || !isResultReady() || result.getError() != null
                || result.getOccurrences().size() > 0
                || analyzeTask != null
        )
            return;

        RegexpAnalyzer analyzer = new RegexpAnalyzer(project, state, this::analyzingFinished);
        ProgressIndicator indicator = new BackgroundableProcessIndicator(analyzer);

        analyzeTask = Pair.create(analyzer, indicator);

        ProgressManager.getInstance().runProcessWithProgressAsynchronously(analyzer, indicator);

        notifyAnalyzingStateListeners();
    }

    public void analyzingFinished() {
        ApplicationManager.getApplication().invokeLater(() -> {
            if (analyzeTask != null) {
                RegexpAnalyzer analyzer = analyzeTask.first;
                assert analyzer.isFinished();

                notifyAnalyzingStateListeners();
            }
        });
    }

    public boolean isAnalyzingInProgress() {
        ApplicationManager.getApplication().assertIsDispatchThread();

        return analyzeTask != null && analyzeTask.first.isFinished();
    }

    @Nullable
    public RegexpAnalyzer getAnalyzingResult() {
        ApplicationManager.getApplication().assertIsDispatchThread();

        if (analyzeTask == null || !analyzeTask.first.isFinished())
            return null;
        
        return analyzeTask.first;
    }
}
