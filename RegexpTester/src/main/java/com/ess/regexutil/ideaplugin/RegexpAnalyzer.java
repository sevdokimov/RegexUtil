package com.ess.regexutil.ideaplugin;

import com.ess.regexutil.ideaplugin.utils.Utils;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import org.intellij.lang.regexp.RegExpLanguage;
import org.intellij.lang.regexp.psi.RegExpBranch;
import org.intellij.lang.regexp.psi.RegExpPattern;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexpAnalyzer extends Task.Backgroundable {

    private final State state;

    private final Runnable onDone;

    private List<Item> items;

    private List<TextRange> matchedRegexp;
    private List<TextRange> additionalMatchedRegexp;

    private TextRange matchedText;

    private boolean success;
    private boolean finished;

    public RegexpAnalyzer(@NotNull Project project, @NotNull State state, @Nullable Runnable onDone) {
        super(project, "Analyzing regexp", true);

        this.state = state;
        this.onDone = onDone;
    }

    public State getState() {
        return state;
    }

    public boolean isSuccess() {
        return success;
    }

    public boolean isFinished() {
        return finished;
    }

    public List<TextRange> getMatchedRegexp() {
        assert success;
        return matchedRegexp;
    }

    public List<TextRange> getAdditionalMatchedRegexp() {
        assert success;
        return additionalMatchedRegexp;
    }

    @Nullable
    public TextRange getMatchedText() {
        assert success;
        return matchedText;
    }

    @Override
    public void run(@NotNull ProgressIndicator indicator) {
        try {
            items = parseRegexp();

            Pair<Integer, TextRange> pair = findMaxMatchedPart(indicator, "", items);

            int matchedItemsCount = pair.first;

            matchedRegexp = new ArrayList<>();

            if (matchedItemsCount == 0) {
                additionalMatchedRegexp = List.of();
            } else {
                matchedRegexp.add(new TextRange(0, items.get(matchedItemsCount - 1).range.getEndOffset()));
                additionalMatchedRegexp = List.of();
                matchedText = pair.second;
            }

            if (matchedItemsCount < items.size()) {
                List<List<Item>> unmatchedVariants = ReadAction.compute(() -> {
                    Item unmatched = items.get(matchedItemsCount);

                    if (unmatched.element instanceof RegExpPattern) {
                        RegExpBranch[] branches = ((RegExpPattern) unmatched.element).getBranches();

                        List<List<Item>> res = new ArrayList<>();

                        for (RegExpBranch branch : branches) {
                            List<Item> items = new ArrayList<>();
                            parseRegexp(branch, items);
                            res.add(items);
                        }

                        return res;
                    }

                    return null;
                });
            }

            success = true;
        } finally {
            finished = true;

            if (onDone != null)
                onDone.run();
        }
    }

    private Pair<Integer, TextRange> findMaxMatchedPart(ProgressIndicator progress, String prefix, List<Item> items) throws ProcessCanceledException {
        if (items.size() == 0)
            return Pair.create(0, null);

        return Utils.runCancelable("regextester-analyze", progress, () -> {
            int begin = 0;
            int end = items.size();

            StringBuilder regexpBuilder = new StringBuilder();

            regexpBuilder.append(prefix);

            TextRange foundText = null;

            while (begin < end) {
                int middle = (end + begin) / 2;

                regexpBuilder.setLength(prefix.length());
                regexpBuilder.append(state.getRegexp(), items.get(0).range.getStartOffset(), items.get(middle).range.getEndOffset());

                Pattern pattern = Pattern.compile(regexpBuilder.toString(), state.getFlags());
                Matcher matcher = pattern.matcher(state.getText());

                if (matcher.find()) {
                    foundText = new TextRange(matcher.start(), matcher.end());
                    begin = middle + 1;
                } else {
                    end = middle;
                }
            }

            return Pair.create(begin, foundText);
        });
    }

    private List<Item> parseRegexp() {
        return ReadAction.compute(() -> {
            List<Item> res = new ArrayList<>();

            PsiFile psiFile = PsiFileFactory.getInstance(getProject()).createFileFromText("r", RegExpLanguage.INSTANCE, state.getRegexp());
            parseRegexp(psiFile, res);

            return res;
        });
    }

    private static void parseRegexp(PsiElement parent, List<Item> res) {
        for (PsiElement e = parent.getFirstChild(); e != null; e = e.getNextSibling()) {
            if (e instanceof RegExpPattern) {
                PsiElement firstChild = e.getFirstChild();
                assert firstChild instanceof RegExpBranch;
                if (firstChild.getNextSibling() == null) {
                    parseRegexp(e, res);
                    continue;
                }
            }

            if (e instanceof RegExpBranch) {
                parseRegexp(e, res);
                continue;
            }

            res.add(new Item(e));

            int last = res.size() - 1;
            if (last > 0)
                assert res.get(last - 1).range.getEndOffset() == res.get(last).range.getStartOffset();
        }
    }

    private static class Item {
        private final TextRange range;

        private final PsiElement element;

        public Item(PsiElement element) {
            this.element = element;
            this.range = element.getTextRange();
        }

        public TextRange getRange() {
            return range;
        }
    }
}
