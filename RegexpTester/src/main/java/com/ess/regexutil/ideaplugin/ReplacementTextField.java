package com.ess.regexutil.ideaplugin;

import com.ess.regexutil.ideaplugin.utils.DocumentChangeListener;
import com.ess.regexutil.ideaplugin.utils.Utils;
import com.google.common.collect.Lists;
import com.intellij.openapi.editor.colors.CodeInsightColors;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.ex.util.EditorUtil;
import com.intellij.openapi.editor.markup.HighlighterLayer;
import com.intellij.openapi.editor.markup.HighlighterTargetArea;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.fileTypes.PlainTextLanguage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Segment;
import com.intellij.openapi.util.TextRange;
import com.intellij.ui.LanguageTextField;
import org.intellij.lang.regexp.RegExpHighlighter;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReplacementTextField extends LanguageTextField {

    private static final Pattern GROUPS = Pattern.compile("(?<ref>\\$(?:(?<refVal>\\{\\w+\\}|\\d+)|(?:.|$)))|(?<escape>\\\\(?:(?<escapeChar>.)|$))", Pattern.DOTALL);

    static final TextAttributesKey ERROR_KEY = CodeInsightColors.ERRORS_ATTRIBUTES;
    static final TextAttributesKey ESC_KEY = RegExpHighlighter.ESC_CHARACTER;
    static final TextAttributesKey GROUP_REF_KEY = CodeInsightColors.TODO_DEFAULT_ATTRIBUTES;

    public ReplacementTextField(@NotNull Project project) {
        super(PlainTextLanguage.INSTANCE, project, "", false);

        setPlaceholder("Replacement");
        setShowPlaceholderWhenFocused(true);
    }

    @Override
    protected @NotNull EditorEx createEditor() {
        EditorEx editor = super.createEditor();

        editor.setBorder(Utils.createEditorBorder(true));

        editor.getSettings().setUseSoftWraps(false);
        editor.setHorizontalScrollbarVisible(true);
        editor.setVerticalScrollbarVisible(true);

        Color editorBackground = EditorColorsManager.getInstance().getGlobalScheme().getDefaultBackground();
        editor.setBackgroundColor(editorBackground);

        Font font = EditorUtil.getEditorFont();
        editor.getColorsScheme().setEditorFontName(font.getFontName());
        editor.getColorsScheme().setEditorFontSize(font.getSize());
        editor.getSettings().setLineCursorWidth(EditorUtil.getDefaultCaretWidth());

        editor.getDocument().addDocumentListener(new DocumentChangeListener(e -> rehighlight(editor)));

        rehighlight(editor);

        return editor;
    }

    private void rehighlight(EditorEx editor) {
        CharSequence text = editor.getDocument().getImmutableCharSequence();

        List<RangeHighlighter> highlighters = Lists.newArrayList(editor.getMarkupModel().getAllHighlighters());
        highlighters.sort(Segment.BY_START_OFFSET_THEN_END_OFFSET);

        BitSet existHighlighters = new BitSet(highlighters.size());

        Matcher matcher = GROUPS.matcher(text);

        while (matcher.find()) {
            TextAttributesKey attrs;

            if (matcher.start("ref") >= 0) {
                if (matcher.start("refVal") >= 0) {
                    attrs = GROUP_REF_KEY;
                } else {
                    attrs = ERROR_KEY;
                }
            } else if (matcher.start("escape") >= 0) {
                if (matcher.start("escapeChar") >= 0) {
                    attrs = ESC_KEY;
                } else {
                    attrs = ERROR_KEY;
                }
            } else {
                throw new IllegalStateException(text.toString());
            }

            int idx = Collections.binarySearch(highlighters, new TextRange(matcher.start(), matcher.end()), Segment.BY_START_OFFSET_THEN_END_OFFSET);
            if (idx >= 0 && highlighters.get(idx).getTextAttributesKey() == attrs) {
                existHighlighters.set(idx);
            } else {
                editor.getMarkupModel().addRangeHighlighter(attrs, matcher.start(), matcher.end(), HighlighterLayer.SYNTAX, HighlighterTargetArea.EXACT_RANGE);
            }
        }

        for (int i = 0; i < highlighters.size(); i++) {
            if (!existHighlighters.get(i))
                editor.getMarkupModel().removeHighlighter(highlighters.get(i));
        }
    }
}
