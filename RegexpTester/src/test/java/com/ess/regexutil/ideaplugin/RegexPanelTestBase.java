package com.ess.regexutil.ideaplugin;

import com.ess.regexutil.ideaplugin.utils.HoverEditorListener;
import com.ess.regexutil.ideaplugin.utils.JBLableHyprlink;
import com.intellij.execution.filters.HyperlinkInfo;
import com.intellij.execution.impl.EditorHyperlinkSupport;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.RangeMarker;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.editor.ex.MarkupModelEx;
import com.intellij.openapi.editor.ex.RangeHighlighterEx;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.util.*;
import com.intellij.openapi.util.registry.Registry;
import com.intellij.util.ThrowableRunnable;
import org.intellij.lang.annotations.Language;
import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Comparator;
import java.util.List;
import java.util.MissingResourceException;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertArrayEquals;

public abstract class RegexPanelTestBase extends MyBasePlatformTestCase {

    protected RegexpTesterPanel panel;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        edt(() -> {
            panel = new RegexpTesterPanel(getProject());
            Disposer.register(getTestRootDisposable(), panel);
        });
    }

    protected void init(@Language("RegExp") @NotNull String regex, @NotNull String text) {
        edt(() -> {
            WriteAction.run(() -> panel.textEditor.getDocument().setText(text));
            panel.regexEditor.setText(regex);
        });

        waitForResults();
    }

    @Override
    protected boolean runInDispatchThread() {
        return false;
    }

    protected void assertResultActivePanel(@MagicConstant(stringValues = {
            MatchingResultPanel.CARD_GROUPS,
            MatchingResultPanel.CARD_MATCHES,
            MatchingResultPanel.CARD_ERROR,
            MatchingResultPanel.CARD_PROGRESS,
            MatchingResultPanel.CARD_EMPTY,
    }) String resultPanel) {
        assertThat(panel.resultsPanel.getCurrentCard()).isEqualTo(resultPanel);
    }

    protected <T, E extends Throwable> T edt(ThrowableComputable<T, E> fun) throws E {
        Ref<T> res = new Ref<>();
        Ref<E> error = new Ref<>();

        invokeLaterIfNeeded(() -> {
            try {
                res.set(fun.compute());
            } catch (Throwable e) {
                error.set((E) e);
            }
        });

        if (error.get() != null)
            throw error.get();

        return res.get();
    }

    protected void invokeLaterIfNeeded(Runnable runnable) {
        Application application = ApplicationManager.getApplication();
        if (application.isDispatchThread()) {
            runnable.run();
        } else {
            application.invokeAndWait(runnable);
        }
    }

    protected void waitForResults() {
        waitFor(() -> panel.isResultReady());
    }

    protected static void waitFor(Callable<Boolean> condition) {
        waitFor(condition, null);
    }

    protected static void waitFor(Callable<Boolean> condition, @Nullable Runnable onFailure) {
        assert !ApplicationManager.getApplication().isDispatchThread();

        long startTime = System.currentTimeMillis();

        try {
            AtomicBoolean res = new AtomicBoolean();

            do {
                if (System.currentTimeMillis() - startTime > 4000) {
                    if (onFailure != null)
                        onFailure.run();

                    throw new RuntimeException("Failed to wait for condition: timeout");
                }

                Thread.sleep(10);
                ApplicationManager.getApplication().invokeAndWait(() -> {
                    try {
                        res.set(condition.call());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
            } while (!res.get());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static Pattern linkPattern(String linkText) {
        return Pattern.compile(" +href=\"([^\"])\" *>" + Pattern.quote(linkText) + "</a>");
    }

    protected boolean hasLink(@NotNull JBLableHyprlink label, @NotNull String linkText) {
        Pattern pattern = linkPattern(linkText);
        Matcher matcher = pattern.matcher(label.getText());
        return matcher.find();
    }

    protected void assertCaretAfter(@NotNull String expectedTextBeforeCaret) {
        assertCaretAfter(panel.textEditor, expectedTextBeforeCaret);
    }

    protected void assertRegexCaretAfter(@NotNull String expectedTextBeforeCaret) {
        if (panel.regexEditor.getEditor() == null)
            initRegexEditor();

        assertCaretAfter(panel.regexEditor.getEditor(), expectedTextBeforeCaret);
    }

    protected void assertCaretAfter(@NotNull Editor editor, @NotNull String expectedTextBeforeCaret) {
        String text = editor.getDocument().getText();
        int caretOffset = editor.getCaretModel().getOffset();

        assertTrue(text, text.substring(0, caretOffset).endsWith(expectedTextBeforeCaret));
    }

    protected void assertSelectedTextAfterCaret(@NotNull String expectedSelectedTextAfterCaret) {
        SelectionModel selectionModel = panel.textEditor.getSelectionModel();
        assertEquals(selectionModel.getSelectionStart(), panel.textEditor.getCaretModel().getOffset());

        assertEquals(expectedSelectedTextAfterCaret, selectionModel.getSelectedText());
    }

    protected void click(@NotNull JBLableHyprlink label, @NotNull String linkText) {
        Pattern pattern = linkPattern(linkText);
        Matcher matcher = pattern.matcher(label.getText());
        if (!matcher.find())
            throw new IllegalStateException("Failed to find a link with text '" + linkText + "', label text: " + label.getText());

        String href = matcher.group(1);

        boolean hasAnotherLink = matcher.find();
        assert !hasAnotherLink;

        label.getListener().accept(new HyperlinkEvent(new Object(), HyperlinkEvent.EventType.ACTIVATED, null, href));
    }

    protected void emulateClick(JComponent component) {
        for (MouseListener mouseListener : component.getMouseListeners()) {
            if (mouseListener.getClass().getPackageName().startsWith("com.ess.regexutil.")) {

                mouseListener.mouseClicked(new MouseEvent(component, MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(), 0, 0, 0, 1, false));
            }
        }
    }

    protected void assertMatches(String ... matches) {
        invokeLaterIfNeeded(() -> {
            String text = panel.textEditor.getDocument().getText();

            String[] actualMatches = Stream.of(findHighlights(panel.textEditor, RegexpTesterPanel.MATCH_IDX))
                    .sorted(Comparator.comparingInt(h -> h.getUserData(RegexpTesterPanel.MATCH_IDX)))
                    .map(RegexPanelTestBase::text)
                    .toArray(String[]::new);

            assertArrayEquals(matches, actualMatches);
        });
    }

    protected static HyperlinkInfo findInnerLink(Editor editor, Segment segment) {
        RangeHighlighter hlt = findInnerHighlight(editor, segment, h -> EditorHyperlinkSupport.getHyperlinkInfo(h) != null);
        return hlt == null ? null : EditorHyperlinkSupport.getHyperlinkInfo(hlt);
    }

    protected static RangeHighlighter findInnerHighlight(Editor editor, Segment segment, Key<?> key) {
        return findInnerHighlight(editor, segment, h -> h.getUserData(key) != null);
    }

    protected static RangeHighlighter findInnerHighlight(Editor editor, Segment segment, Predicate<RangeHighlighter> filter) {
        if (segment instanceof RangeMarker)
            assert ((RangeMarker) segment).getDocument() == editor.getDocument();

        Ref<RangeHighlighter> res = new Ref<>();

        ((MarkupModelEx)editor.getMarkupModel()).processRangeHighlightersOverlappingWith(
                segment.getStartOffset(), segment.getEndOffset(),
                h -> {
                    if (h.getStartOffset() >= segment.getStartOffset() && h.getEndOffset() <= segment.getEndOffset()) {
                        if (filter.test(h)) {
                            if (res.get() != null)
                                throw new IllegalStateException("More than one highlight found");

                            res.set(h);
                        }
                    }
                    
                    return true;
                });

        return res.get();
    }

    protected static RangeHighlighter[] findHighlights(Editor editor, Key<?> key) {
        return Stream.of(editor.getMarkupModel().getAllHighlighters())
                .filter(h -> h.getUserData(key) != null)
                .sorted(Comparator.comparingInt(RangeMarker::getStartOffset))
                .toArray(RangeHighlighter[]::new);
    }

    protected static List<String> text(RangeMarker ... highlights) {
        return Stream.of(highlights)
                .map(RegexPanelTestBase::text)
                .collect(Collectors.toList());
    }

    protected static String text(@NotNull RangeMarker hlt) {
        return hlt.getDocument().getImmutableCharSequence().subSequence(hlt.getStartOffset(), hlt.getEndOffset()).toString();
    }

    protected <T extends Throwable> void withHover(RangeHighlighter rangeHighlighter, @NotNull ThrowableRunnable<T> runnable) throws T {
        BiConsumer<RangeHighlighterEx, Boolean> hoverListener = rangeHighlighter.getUserData(HoverEditorListener.ON_HOVER_KEY);

        hoverListener.accept((RangeHighlighterEx) rangeHighlighter, true);

        try {
            runnable.run();
        } finally {
            hoverListener.accept((RangeHighlighterEx) rangeHighlighter, false);
        }
    }

    protected void initRegexEditor() {
        runWithFlag("editor.text.field.init.on.shown", false, () -> panel.regexEditor.addNotify());
    }

    protected void runWithFlag(String name, boolean value, Runnable run) {
        try {
            boolean savedFlag = Registry.get(name).asBoolean();
            Registry.get(name).setValue(value);
            try {
                run.run();
            } finally {
                Registry.get(name).setValue(savedFlag);
            }
        } catch (MissingResourceException e) {
            run.run();
        }
    }
}
