package com.ess.regexutil.ideaplugin;

import com.ess.regexutil.ideaplugin.utils.HoverEditorListener;
import com.ess.regexutil.ideaplugin.utils.JBLableHyprlink;
import com.intellij.execution.impl.EditorHyperlinkSupport;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.editor.colors.CodeInsightColors;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.ex.MarkupModelEx;
import com.intellij.openapi.editor.ex.RangeHighlighterEx;
import com.intellij.openapi.editor.markup.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.Segment;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.HtmlBuilder;
import com.intellij.openapi.util.text.HtmlChunk;
import com.intellij.ui.ColorUtil;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.regex.PatternSyntaxException;

public class MatchingResultPanel extends JPanel implements Disposable {

    public static final String CARD_PROGRESS = "progress";
    public static final String CARD_EMPTY = "empty";
    public static final String CARD_ERROR = "error";
    public static final String CARD_MATCHES = "matches";
    public static final String CARD_GROUPS = "groups";

    static final Key<Boolean> GROUP_TEXT = Key.create("MatchingResultPanel.GROUP_TEXT");

    public static final TextAttributes CURRENT_GROUP_ATTR = new TextAttributes(null, null,
            new JBColor(new Color(0x00bb00), new Color(98, 150, 85)),
            EffectType.BOXED, 0);

    private String currentCard = CARD_EMPTY;

    private MatchResult result;

    private BiConsumer<Integer, Integer> hoverGroup;

    private Consumer<Segment> textSelection = t -> {};

    final JLabel errorLabel = new JLabel();

    final JLabel matchesTitle = new JLabel();
    {
        UIUtil.applyStyle(UIUtil.ComponentStyle.LARGE, matchesTitle);
    }

    final JBLableHyprlink groupTitle = new JBLableHyprlink("|", e -> {
        int occurrenceIdx = Integer.parseInt(e.getDescription());
        if (result != null && occurrenceIdx < result.getOccurrences().size()) {
            selectOccurrence(occurrenceIdx);
            textSelection.accept(result.getOccurrences().get(occurrenceIdx));
        }
    });

    Editor groupEditor;
    private int selectedOccurrence;

    public MatchingResultPanel(@NotNull Project project, IntConsumer errorClickListener) {
        super(new CardLayout());

        JPanel emptyPanel = new JPanel();
        add(emptyPanel, CARD_EMPTY);

        add(new JBLabel("Matching..."), CARD_PROGRESS);

        errorLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        errorLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (result == null || result.getError() == null)
                    return;

                errorClickListener.accept(result.getError().getIndex());
            }
        });

        add(createErrorPanel(), CARD_ERROR);

        add(createMatchesPanel(), CARD_MATCHES);
        add(createGroupsPanel(project), CARD_GROUPS);
    }

    private JComponent createMatchesPanel() {
        JPanel res = new JPanel(new BorderLayout());
        res.add(matchesTitle, BorderLayout.PAGE_START);

        return res;
    }

    private JComponent createErrorPanel() {
        JPanel error = new JPanel(new BorderLayout());
        JPanel errorBox = new JPanel();
        errorBox.setLayout(new BoxLayout(errorBox, BoxLayout.Y_AXIS));
        JLabel errorHeader = new JLabel("<html><body><div style=\"padding-bottom: 5px; font-weight: bold\">Invalid pattern</div></body></html>");
        errorBox.add(errorHeader);
        errorBox.add(errorLabel);

        JPanel errorIn = new JPanel(new BorderLayout());

        errorIn.add(errorBox, BorderLayout.LINE_START);
        error.add(errorIn, BorderLayout.PAGE_START);

        return error;
    }

    private JComponent createGroupsPanel(@NotNull Project project) {
        JPanel res = new JPanel(new BorderLayout(0, 10));
        res.add(groupTitle, BorderLayout.PAGE_START);

        Document document = EditorFactory.getInstance().createDocument("");

        groupEditor = EditorFactory.getInstance().createViewer(document, project, EditorKind.CONSOLE);

        EditorSettings settings = groupEditor.getSettings();

        settings.setUseSoftWraps(true);
        settings.setAdditionalLinesCount(0);
        settings.setAdditionalColumnsCount(1);
        settings.setFoldingOutlineShown(false);
        settings.setLineNumbersShown(false);
        settings.setLineMarkerAreaShown(false);
        settings.setIndentGuidesShown(false);
        settings.setVirtualSpace(false);
        settings.setWheelFontChangeEnabled(false);
        settings.setAdditionalPageAtBottom(false);
        settings.setCaretRowShown(false);

        res.add(groupEditor.getComponent(), BorderLayout.CENTER);

        HoverEditorListener.install(groupEditor);

        return res;
    }

    public MatchResult getResult() {
        return result;
    }

    private void select(String card) {
        ((CardLayout) getLayout()).show(this, card);
        currentCard = card;
    }

    @NotNull
    public String getCurrentCard() {
        return currentCard;
    }

    public void setProgressState() {
        result = null;
        select(CARD_PROGRESS);
    }

    public void setResult(MatchResult result) {
        ApplicationManager.getApplication().assertIsDispatchThread();

        boolean noChanges = Objects.equals(this.result, result);

        this.result = result;

        if (noChanges)
            return;

        selectedOccurrence = -1;

        if (result == null) {
            select(CARD_EMPTY);
            return;
        }

        if (result.getError() != null) {
            errorLabel.setText(renderError(result.getError()));
            select(CARD_ERROR);
            return;
        }

        if (result.getOccurrences().isEmpty()) {
            matchesTitle.setText("no match");
        } else {
            if (result.getMatchType() == RegexpTesterPanel.MatchType.ENTIRE_STRING || result.getMatchType() == RegexpTesterPanel.MatchType.BEGINNING) {
                assert result.getOccurrences().size() == 1;
                matchesTitle.setText("match");
            } else {
                matchesTitle.setText(String.format("<html><body><b>%d</b> match%s found</body></html>",
                        result.getOccurrences().size(),
                        result.getOccurrences().size() == 1 ? "" : "es"
                        ));
            }
        }

        if (result.getOccurrences().size() == 1) {
            selectOccurrence(0);
        } else {
            select(CARD_MATCHES);
        }
    }

    public int getSelectedOccurrence() {
        return selectedOccurrence;
    }

    public void selectOccurrence(int occurrenceIdx) {
        if (selectedOccurrence == occurrenceIdx)
            return;

        selectedOccurrence = occurrenceIdx;

        MatchResult.Occurrence o = result.getOccurrences().get(occurrenceIdx);

        StringBuilder title = new StringBuilder();
        title.append("Match   ");
        if (occurrenceIdx > 0) {
            title.append("<a href=\"").append(occurrenceIdx - 1).append("\">&lt;&lt;</a>");
        } else {
            title.append("<span>&lt;&lt;</span>");
        }

        title.append(" <b>").append(occurrenceIdx + 1).append("</b> / ").append(result.getOccurrences().size()).append(' ');
        if (occurrenceIdx + 1 < result.getOccurrences().size()) {
            title.append(" <a href=\"").append(occurrenceIdx + 1).append("\">&gt;&gt;</a> ");
        } else {
            title.append("&gt;&gt;");
        }

        groupTitle.setText(title.toString());

        StringBuilder sb = new StringBuilder();

        List<TextRange> offsetsRanges = new ArrayList<>();
        List<TextRange> textRanges = new ArrayList<>();

        for (int i = 1; i < o.getGroups().size(); i++) {
            MatchResult.MatchGroup group = o.getGroups().get(i);

            int start = sb.length();

            if (group.getName() != null) {
                sb.append(group.getName());
            } else {
                sb.append("group #").append(i);
            }

            if (group.isMatch())
                sb.append(" [").append(group.getStartOffset()).append('-').append(group.getEndOffset()).append(']');

            offsetsRanges.add(new TextRange(start, sb.length()));

            sb.append(": ");

            int textStart = sb.length();

            if (group.isMatch()) {
                sb.append(result.getText(), group.getStartOffset(), group.getEndOffset());
            } else {
                sb.append("null");
            }
            
            textRanges.add(new TextRange(textStart, sb.length()));

            sb.append("\n\n");
        }

        int entireTextLabelStart = sb.length();
        sb.append("match text");
        int entireTextLabelEnd = sb.length();

        sb.append(": ");

        int entireTextTextStart = sb.length();
        sb.append(result.getText(), o.getStartOffset(), o.getEndOffset());

        MarkupModel markupModel = groupEditor.getMarkupModel();
        markupModel.removeAllHighlighters();

        WriteAction.run(() -> groupEditor.getDocument().setText(sb));

        EditorHyperlinkSupport hyperlinkSupport = EditorHyperlinkSupport.get(groupEditor);

        for (int i = 0; i < offsetsRanges.size(); i++) {
            TextRange labelRange = offsetsRanges.get(i);
            TextRange textRange = textRanges.get(i);

            RangeHighlighter regionHlt = markupModel.addRangeHighlighter(null, labelRange.getStartOffset(), textRange.getEndOffset(),
                    HighlighterLayer.SELECTION, HighlighterTargetArea.EXACT_RANGE);

            MatchResult.MatchGroup group = o.getGroups().get(i + 1);

            int groupIdx = i + 1;
            regionHlt.putUserData(HoverEditorListener.ON_HOVER_KEY, (hlt, isMouseOn) -> groupRegionHover(hlt, isMouseOn, occurrenceIdx, groupIdx));

            if (group.isMatch()) {
                RangeHighlighter labelHlt = markupModel.addRangeHighlighter(CodeInsightColors.HYPERLINK_ATTRIBUTES, labelRange.getStartOffset(), labelRange.getEndOffset(),
                        HighlighterLayer.HYPERLINK, HighlighterTargetArea.EXACT_RANGE);

                hyperlinkSupport.createHyperlink(labelHlt, project -> textSelection.accept(group));

                RangeHighlighter textHlt = markupModel.addRangeHighlighter(null, textRange.getStartOffset(), textRange.getEndOffset(),
                        HighlighterLayer.SELECTION, HighlighterTargetArea.EXACT_RANGE);
                textHlt.putUserData(GROUP_TEXT, true);
            } else {
                regionHlt.setTextAttributesKey(XmlHighlighterColors.XML_COMMENT);
            }
        }

        // Highlight "entire occurrence text" block (group #0)
        RangeHighlighter entireTextLabelHlt = markupModel.addRangeHighlighter(CodeInsightColors.HYPERLINK_ATTRIBUTES, entireTextLabelStart, entireTextLabelEnd, HighlighterLayer.HYPERLINK, HighlighterTargetArea.EXACT_RANGE);
        hyperlinkSupport.createHyperlink(entireTextLabelHlt, project -> textSelection.accept(o));

        RangeHighlighter entireTextHlt = markupModel.addRangeHighlighter(null, entireTextTextStart, sb.length(), HighlighterLayer.SELECTION, HighlighterTargetArea.EXACT_RANGE);
        entireTextHlt.putUserData(GROUP_TEXT, true);

        RangeHighlighter entireTextRegion = markupModel.addRangeHighlighter(null, entireTextLabelStart, sb.length(), HighlighterLayer.SYNTAX, HighlighterTargetArea.EXACT_RANGE);
        entireTextRegion.putUserData(HoverEditorListener.ON_HOVER_KEY, (hlt, isMouseOn) -> groupRegionHover(hlt, isMouseOn, occurrenceIdx, 0));

        select(CARD_GROUPS);
    }

    private void groupRegionHover(RangeHighlighterEx regionHlt, Boolean isMouseOn, int occurrenceIdx, int groupIdx) {
        ((MarkupModelEx) groupEditor.getMarkupModel()).processRangeHighlightersOverlappingWith(regionHlt.getStartOffset(), regionHlt.getEndOffset(), h -> {
            if (h.getUserData(GROUP_TEXT) == null)
                return true;

            if (isMouseOn) {
                h.setTextAttributes(CURRENT_GROUP_ATTR);
            } else {
                h.setTextAttributes(TextAttributes.ERASE_MARKER);
            }
            return false;
        });

        if (hoverGroup != null) {
            hoverGroup.accept(occurrenceIdx, isMouseOn ? groupIdx : -1);
        }
    }

    /**
     * 1th number - occurrence index,
     * 2th number - group index,
     */
    public void setHoverListener(BiConsumer<Integer, Integer> hoverListener) {
        this.hoverGroup = hoverListener;
    }

    public void setTextSelection(Consumer<Segment> textSelection) {
        this.textSelection = textSelection;
    }

    static String renderError(PatternSyntaxException ex) {
        EditorColorsScheme globalScheme = EditorColorsManager.getInstance().getGlobalScheme();
        TextAttributes errorAttr = globalScheme.getAttributes(CodeInsightColors.WRONG_REFERENCES_ATTRIBUTES);
        Color fc = errorAttr.getForegroundColor();

        HtmlChunk.Element errorText = HtmlChunk.text(ex.getDescription()).wrapWith("span");
        if (fc != null) {
            errorText = errorText.style("color: #" + ColorUtil.toHex(fc));
        }

        HtmlBuilder html = new HtmlBuilder();

        if (ex.getIndex() >= 0) {
            int lineCount = 0;
            int lineStart = 0;
            String pattern = ex.getPattern();
            for (int i = 0; i < ex.getIndex(); i++) {
                if (i >= pattern.length())
                    break;

                char a = pattern.charAt(i);
                if (a == '\n') {
                    lineStart = i + 1;
                    lineCount++;
                }
            }

            html.append(HtmlChunk.link("#", "[" + (lineCount + 1) + ':' + (ex.getIndex() - lineStart + 1) + ']'));
            html.appendRaw("&nbsp;");
        }
        
        html.append(errorText);

        return html.wrapWithHtmlBody().toString();
    }

    @Override
    public void dispose() {
        if (groupEditor != null) {
            EditorFactory.getInstance().releaseEditor(groupEditor);
            groupEditor = null;
        }
    }
}
