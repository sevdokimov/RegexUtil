package com.ess.regexutil.ideaplugin;

import com.ess.regexutil.ideaplugin.utils.Utils;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.CaretEvent;
import com.intellij.openapi.editor.event.CaretListener;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.editor.markup.EffectType;
import com.intellij.openapi.editor.markup.HighlighterLayer;
import com.intellij.openapi.editor.markup.HighlighterTargetArea;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.Segment;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.ui.JBColor;
import org.intellij.lang.regexp.RegExpFile;
import org.intellij.lang.regexp.RegExpTT;
import org.intellij.lang.regexp.psi.RegExpBranch;
import org.intellij.lang.regexp.psi.RegExpGroup;
import org.intellij.lang.regexp.psi.RegExpNamedGroupRef;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.IntConsumer;

public class RegexHighlighter implements FocusListener, CaretListener, DocumentListener {

    static final Key<Boolean> ELEMENT_UNDER_CARET = Key.create("RegexHighlighter.ELEMENT_UNDER_CARET");

    public static final Comparator<Segment> SEGMENT_COMPARATOR = Comparator.<Segment>comparingInt(Segment::getStartOffset).thenComparingInt(Segment::getEndOffset);

    public static final TextAttributes UNION_BRANCH_ATTR = new TextAttributes(null, null,
            new JBColor(new Color(0x50dd40), new Color(88, 120, 75)),
            EffectType.BOXED, 0);

    private final Editor editor;
    private final IntConsumer groupSelectListener;

    RegexHighlighter(@NotNull Editor editor, @NotNull IntConsumer groupSelectListener) {
        this.editor = editor;
        this.groupSelectListener = groupSelectListener;

        editor.getContentComponent().addFocusListener(this);
        editor.getCaretModel().addCaretListener(this);
        editor.getDocument().addDocumentListener(this);
    }

    @Override
    public void documentChanged(@NotNull DocumentEvent event) {
        PsiDocumentManager.getInstance(editor.getProject()).performForCommittedDocument(editor.getDocument(), () -> {
            ApplicationManager.getApplication().assertIsDispatchThread();
            highlight();
        });
    }

    @Override
    public void focusGained(FocusEvent e) {
        highlight();
    }

    @Override
    public void focusLost(FocusEvent e) {
        clearHighlighting();
    }

    @Override
    public void caretPositionChanged(@NotNull CaretEvent event) {
        if (event.getCaret() == null)
            return;

        highlight();
    }

    public static void install(@NotNull Editor editor, @NotNull IntConsumer groupSelectListener) {
        new RegexHighlighter(editor, groupSelectListener);
    }

    @Nullable
    private RegExpGroup findGroupByIndex(RegExpFile file, int groupIdx) {
        int idx = 1;

        for (RegExpGroup group : file.getGroups()) {
            if (!group.isCapturing())
                continue;

            if (idx == groupIdx)
                return group;

            idx++;
        }

        return null;
    }

    private int findGroupByIndex(RegExpGroup group) {
        if (!group.isCapturing())
            return -1;

        RegExpFile file = (RegExpFile) group.getContainingFile();

        int idx = 1;

        for (RegExpGroup g : file.getGroups()) {
            if (!g.isCapturing())
                continue;

            if (g == group)
                return idx;

            idx++;
        }

        Utils.throwIfDebug("Group not found [group=" + group.getText() + ", file=" + file.getText());
        return -1;
    }

    private int groupAtCaret(RegExpFile file, int offset) {
        PsiElement elementAtCaret = file.findElementAt(offset);
        PsiElement elementPrev = offset > 0 ? file.findElementAt(offset - 1) : null;

        PsiElement e;

        if (Utils.isLeafElementOfType(elementPrev, RegExpTT.GROUP_END)) {
            e = elementPrev;
        } else if (Utils.isLeafElementOfType(elementAtCaret, RegExpTT.RUBY_NAMED_GROUP)
                || Utils.isLeafElementOfType(elementAtCaret, RegExpTT.GROUP_BEGIN)) {
            e = elementAtCaret;
        } else if (Utils.isLeafElementOfType(elementAtCaret, RegExpTT.GROUP_END)) {
            e = elementAtCaret;
        } else if (Utils.isLeafElementOfType(elementPrev, RegExpTT.RUBY_NAMED_GROUP)
                || Utils.isLeafElementOfType(elementPrev, RegExpTT.GROUP_BEGIN)) {
            e = elementPrev;
        } else {
            return -1;
        }

        PsiElement parent = e.getParent();
        if (parent instanceof RegExpGroup)
            return findGroupByIndex((RegExpGroup) parent);

        return -1;
    }

    private void highlight() {
        Project project = editor.getProject();

        if (!PsiDocumentManager.getInstance(project).isCommitted(editor.getDocument()))
            return;

        if (!editor.getContentComponent().hasFocus() && !ApplicationManager.getApplication().isUnitTestMode()) {
            clearHighlighting();
            return;
        }

        int offset = editor.getCaretModel().getOffset();

        RegExpFile file = (RegExpFile) PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());

        int groupIdx = groupAtCaret(file, offset);

        groupSelectListener.accept(groupIdx);

        List<TextRange> ranges = getHighlightRanges(file, offset, groupIdx >= 0);

        BitSet existHighlights = new BitSet(ranges.size());

        for (RangeHighlighter hlt : editor.getMarkupModel().getAllHighlighters()) {
            if (hlt.getUserData(ELEMENT_UNDER_CARET) == null)
                continue;

            int idx = Collections.binarySearch(ranges, hlt, SEGMENT_COMPARATOR);
            if (idx < 0) {
                editor.getMarkupModel().removeHighlighter(hlt);
            } else {
                existHighlights.set(idx);
            }
        }

        for (int i = 0; i < ranges.size(); i++) {
            if (!existHighlights.get(i)) {
                TextRange range = ranges.get(i);

                RangeHighlighter hlt = editor.getMarkupModel().addRangeHighlighter(
                        range.getStartOffset(), range.getEndOffset(),
                        HighlighterLayer.ELEMENT_UNDER_CARET,
                        UNION_BRANCH_ATTR,
                        HighlighterTargetArea.EXACT_RANGE);

                hlt.putUserData(ELEMENT_UNDER_CARET, true);
            }
        }
    }

    private List<TextRange> getHighlightRanges(RegExpFile file, int caretOffset, boolean bracketHighlighted) {
        PsiElement element = file.findElementAt(caretOffset);
        if (Utils.isLeafElementOfType(element, RegExpTT.UNION))
            return highlightUnion(element);

        if (caretOffset > 0) {
            PsiElement prev = file.findElementAt(caretOffset - 1);
            if (Utils.isLeafElementOfType(prev, RegExpTT.UNION))
                return highlightUnion(prev);
        }

        if (!bracketHighlighted && Utils.isLeafElementOfType(element, RegExpTT.BACKREF)) {
            String text = element.getText();
            assert text.length() > 0 && text.charAt(0) == '\\';
            int groupIdx = Integer.parseInt(text.substring(1));

            RegExpGroup group = findGroupByIndex(file, groupIdx);
            if (group != null) {
                List<TextRange> res = new ArrayList<>();
                res.add(group.getTextRange());
                res.add(element.getTextRange());
                res.sort(SEGMENT_COMPARATOR);
                return res;
            }
        }

        if (!bracketHighlighted && element != null && element.getParent() instanceof RegExpNamedGroupRef) {
            RegExpNamedGroupRef ref = (RegExpNamedGroupRef) element.getParent();
            String groupName = ref.getGroupName();
            if (groupName != null) {
                List<TextRange> res = new ArrayList<>();

                for (RegExpGroup group : file.getGroups()) {
                    if (groupName.equals(group.getGroupName())) {
                        res.add(group.getTextRange());
                    }
                }

                res.add(ref.getTextRange());
                res.sort(SEGMENT_COMPARATOR);
                
                return res;
            }
        }

        return List.of();
    }

    private List<TextRange> highlightUnion(@NotNull PsiElement union) {
        PsiElement e = union;
        while (e.getPrevSibling() != null)
            e = e.getPrevSibling();

        List<TextRange> res = new ArrayList<>();

        for (; e != null; e = e.getNextSibling()) {
            if (Utils.isLeafElementOfType(e, RegExpTT.UNION))
                continue;

            if (e instanceof RegExpBranch) {
                res.add(e.getTextRange());
                continue;
            }

            Utils.throwIfDebug("Unexpected element: " + e + ", regexp: " + editor.getDocument().getText());
            break;
        }

        return res;
    }

    private void clearHighlighting() {
        for (RangeHighlighter hlt : editor.getMarkupModel().getAllHighlighters()) {
            if (hlt.getUserData(ELEMENT_UNDER_CARET) != null)
                editor.getMarkupModel().removeHighlighter(hlt);
        }

        groupSelectListener.accept(-1);
    }

}
