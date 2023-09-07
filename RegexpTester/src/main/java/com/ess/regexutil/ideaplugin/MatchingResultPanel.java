package com.ess.regexutil.ideaplugin;

import com.ess.regexutil.ideaplugin.utils.HoverEditorListener;
import com.ess.regexutil.ideaplugin.utils.JBLableHyprlink;
import com.intellij.execution.impl.EditorHyperlinkSupport;
import com.intellij.ide.BrowserUtil;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.editor.colors.*;
import com.intellij.openapi.editor.ex.MarkupModelEx;
import com.intellij.openapi.editor.ex.RangeHighlighterEx;
import com.intellij.openapi.editor.markup.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.Segment;
import com.intellij.openapi.util.TextRange;
import com.intellij.ui.ColorUtil;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import org.apache.commons.lang.StringEscapeUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.regex.PatternSyntaxException;

public class MatchingResultPanel extends JPanel implements Disposable {

    public static final int INVALID_REPLACEMENT = -1000;

    private static final String DISABLE_RATE_PROP = "regextester2917.rate";

    public static final String CARD_PROGRESS = "progress";
    public static final String CARD_EMPTY = "empty";
    public static final String CARD_ERROR = "error";
    public static final String CARD_MATCHES = "matches";
    public static final String CARD_GROUPS = "groups";
    public static final String CARD_REPLACED = "replaced";

    public static final String CARD_AN_BUTTON = "button";
    public static final String CARD_AN_RESULTS = "res";

    static final Key<Boolean> GROUP_TEXT = Key.create("MatchingResultPanel.GROUP_TEXT");

    public static final TextAttributes CURRENT_GROUP_ATTR = new TextAttributes(null, null,
            new JBColor(new Color(0x00bb00), new Color(98, 150, 85)),
            EffectType.BOXED, 0);

    private static final TextAttributesKey REPLACED_ATTR_KEY = EditorColors.LIVE_TEMPLATE_ATTRIBUTES;

    private final MatchingProcessor matchingProcessor;

    private String currentCard = CARD_EMPTY;

    private MatchResult result;

    private BiConsumer<Integer, Integer> hoverGroup;

    private Consumer<Segment> textSelection = t -> {};

    final JLabel errorLabel = new JLabel();

    final JLabel matchesTitle = new JLabel();
    {
        UIUtil.applyStyle(UIUtil.ComponentStyle.LARGE, matchesTitle);
    }

    final JButton analyzeButton = new JButton("Find Unmatched Part");
    final JPanel analyzePanel = new JPanel(new CardLayout());

    final JBLableHyprlink groupTitle = new JBLableHyprlink("|", e -> {
        int occurrenceIdx = Integer.parseInt(e.getDescription());
        if (result != null && occurrenceIdx < result.getOccurrences().size()) {
            selectOccurrence(occurrenceIdx);
            textSelection.accept(result.getOccurrences().get(occurrenceIdx));
        }
    });

    final JBLableHyprlink rateLabel = new JBLableHyprlink("<a href=\"rate\">Rate the plugin in the Marketplace</a>" +
            "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" +
            "<a href=\"no\">No, thanks</a><br><br>", e -> rate(e.getDescription()));

    Editor groupEditor;
    final Editor replacedEditor;
    private int selectedOccurrence;

    public MatchingResultPanel(@NotNull Project project, IntConsumer errorClickListener, MatchingProcessor matchingProcessor) {
        super(new CardLayout());

        this.matchingProcessor = matchingProcessor;

        add(new JPanel(), CARD_EMPTY);

        JBLabel matching = new JBLabel("Matching...");
        matching.setBorder(subpanelBorder());
        add(matching, CARD_PROGRESS);

        errorLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        errorLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (result == null || result.getError() == null)
                    return;

                Exception error = result.getError();

                if (error instanceof PatternSyntaxException) {
                    errorClickListener.accept(((PatternSyntaxException)error).getIndex());
                    return;
                }

                if (error instanceof IllegalArgumentException)
                    errorClickListener.accept(INVALID_REPLACEMENT);
            }
        });

        add(createErrorPanel(), CARD_ERROR);

        add(createMatchesPanel(), CARD_MATCHES);
        add(createGroupsPanel(project), CARD_GROUPS);

        replacedEditor = createEditor(project);
        replacedEditor.getSettings().setUseSoftWraps(false);
        add(replacedEditor.getComponent(), CARD_REPLACED);

        matchingProcessor.addAnalyzingListener(this::onAnalyzingStateChanged);
    }

    private void onAnalyzingStateChanged() {
        analyzeButton.setEnabled(!matchingProcessor.isAnalyzingInProgress());
        analyzeButton.setVisible(matchingProcessor.isResultReady() && result.getOccurrences().isEmpty());

        RegexpAnalyzer anResult = matchingProcessor.getAnalyzingResult();

        String card;

        if (anResult != null) {
            if (!rateLabel.isVisible()
                    && PropertiesComponent.getInstance().getValue(DISABLE_RATE_PROP) == null
                    && anResult.isFinished() && anResult.getMatchedRegexp().size() > 0 && anResult.getBlockers().size() > 0) {
                if (new Random().nextInt(5) == 0)
                    rateLabel.setVisible(true);
            }

            card = CARD_AN_RESULTS;
        } else {
            card = CARD_AN_BUTTON;
        }

        ((CardLayout) analyzePanel.getLayout()).show(analyzePanel, card);
    }

    private static Border subpanelBorder() {
        return JBUI.Borders.emptyTop(10);
    }

    private JComponent createMatchesPanel() {
        JPanel res = new JPanel(new BorderLayout());
        res.setBorder(subpanelBorder());

        res.add(matchesTitle, BorderLayout.NORTH);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnPanel.add(analyzeButton, BorderLayout.WEST);

        JLabel legend = new JBLableHyprlink("" +
                "<html><body>" +

                "<div style=\"padding: 5px; background: " + ColorUtil.toHtmlColor(EditorColorsManager.getInstance().getGlobalScheme().getDefaultBackground()) + "\">" +

                "<table cellpadding=\"0\" cellspacing=\"7\">" +
                "  <tr>" +
                "    <td style=\"background: " + ColorUtil.toHtmlColor(RegexpTesterPanel.MATCHED_REGEXP.getBackgroundColor())
                +       "; text-decoration: underline " + ColorUtil.toHtmlColor(RegexpTesterPanel.MATCHED_REGEXP.getEffectColor()) + "\">***</td>" +
                "    <td> - regexp parts matched to the highlighted text</td>" +
                "  </tr>" +
                "  <tr>" +
                "    <td style=\"background: " + ColorUtil.toHtmlColor(RegexpTesterPanel.MATCHED_REGEXP.getBackgroundColor())+ "\">***</td>" +
                "    <td> - regexp branches that can be matched</td>" +
                "  </tr>" +
                "  <tr>" +
                "    <td style=\"border: 1px solid " + ColorUtil.toHtmlColor(RegexpTesterPanel.BLOCKER.getEffectColor()) + "; background: " + ColorUtil.toHtmlColor(RegexpTesterPanel.BLOCKER.getBackgroundColor()) + "\">***</td>" +
                "    <td> - blockers</td>" +
                "  </tr>" +
                "</table>" +
                "<a href=\"clear\">clear</a>" +
                "</div>" +
                "</body></html>", e -> matchingProcessor.clearAnalyzeResult());

        analyzePanel.add(btnPanel, CARD_AN_BUTTON);
        analyzePanel.add(legend, CARD_AN_RESULTS);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(JBUI.Borders.emptyTop(15));
        bottomPanel.add(analyzePanel, BorderLayout.NORTH);
        bottomPanel.add(rateLabel, BorderLayout.SOUTH);
        rateLabel.setVisible(false);
        
        res.add(bottomPanel, BorderLayout.CENTER);

        analyzeButton.addActionListener(e -> matchingProcessor.findUnmatched());

        return res;
    }

    private JComponent createErrorPanel() {
        JPanel error = new JPanel(new BorderLayout());
        error.setBorder(subpanelBorder());

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

    private Editor createEditor(@NotNull Project project) {
        Document document = EditorFactory.getInstance().createDocument("");

        Editor res = EditorFactory.getInstance().createViewer(document, project, EditorKind.CONSOLE);

        EditorSettings settings = res.getSettings();

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

        HoverEditorListener.install(res);

        return res;
    }

    private JComponent createGroupsPanel(@NotNull Project project) {
        JPanel res = new JPanel(new BorderLayout(0, 10));
        res.setBorder(subpanelBorder());

        res.add(groupTitle, BorderLayout.PAGE_START);

        groupEditor = createEditor(project);

        res.add(groupEditor.getComponent(), BorderLayout.CENTER);

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

        if (result.getMatchType() == MatchType.REPLACE) {
            showReplacedText();

            select(CARD_REPLACED);
            return;
        }

        if (result.getOccurrences().isEmpty()) {
            matchesTitle.setText("<html><body><b>&nbsp;no match</b></body></html>");
        } else {
            if (result.getMatchType() == MatchType.ENTIRE_STRING || result.getMatchType() == MatchType.BEGINNING) {
                assert result.getOccurrences().size() == 1;
                matchesTitle.setText("match");
            } else {
                matchesTitle.setText(String.format("<html><body><b>%d</b> match%s found (move the caret to a match to see details)</body></html>",
                        result.getOccurrences().size(),
                        result.getOccurrences().size() == 1 ? "" : "es"
                        ));
            }
        }

        onAnalyzingStateChanged();

        if (result.getOccurrences().size() == 1) {
            selectOccurrence(0);
        } else {
            select(CARD_MATCHES);
        }
    }

    private void showReplacedText() {
        assert result.getMatchType() == MatchType.REPLACE;
        assert result.getReplaced() != null;

        replacedEditor.getMarkupModel().removeAllHighlighters();

        WriteAction.run(() -> replacedEditor.getDocument().setText(result.getReplaced()));

        List<MatchResult.Occurrence> occurrences = result.getOccurrences();
        
        for (int i = 0; i < occurrences.size(); i++) {
            MatchResult.Occurrence occurrence = occurrences.get(i);

            TextRange range = occurrence.getReplacementRange();

            RangeHighlighter hlt = replacedEditor.getMarkupModel()
                    .addRangeHighlighter(REPLACED_ATTR_KEY, range.getStartOffset(), range.getEndOffset(),
                            HighlighterLayer.WARNING, HighlighterTargetArea.EXACT_RANGE);

            int occurrenceIdx = i;
            hlt.putUserData(HoverEditorListener.ON_HOVER_KEY, (h, isMouseOn) -> replacedHover(h, isMouseOn, occurrenceIdx));
        }
    }

    private void replacedHover(RangeHighlighterEx h, Boolean isMouseOn, int occurrenceIdx) {
        if (hoverGroup != null)
            hoverGroup.accept(occurrenceIdx, isMouseOn ? 0 : -1);

        TextAttributes replacedTextAttr = replacedEditor.getColorsScheme().getAttributes(REPLACED_ATTR_KEY);

        if (isMouseOn) {
            TextAttributes selectedAttr = replacedEditor.getColorsScheme().getAttributes(EditorColors.IDENTIFIER_UNDER_CARET_ATTRIBUTES);
            h.setTextAttributes(TextAttributes.merge(selectedAttr, replacedTextAttr));
        } else {
            h.setTextAttributes(replacedTextAttr);
        }
    }

    public int getSelectedOccurrence() {
        return selectedOccurrence;
    }

    public void selectOccurrence(int occurrenceIdx) {
        if (result.getMatchType() == MatchType.REPLACE)
            return;

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

    private void rate(@NotNull String button) {
        PropertiesComponent.getInstance().setValue(DISABLE_RATE_PROP, button);
        rateLabel.setVisible(false);

        if (button.equals("rate"))
            BrowserUtil.browse("https://plugins.jetbrains.com/plugin/2917-regexp-tester/reviews");
    }

    static String renderError(Exception ex) {
        EditorColorsScheme globalScheme = EditorColorsManager.getInstance().getGlobalScheme();
        TextAttributes errorAttr = globalScheme.getAttributes(CodeInsightColors.WRONG_REFERENCES_ATTRIBUTES);
        Color fc = errorAttr.getForegroundColor();

        String errorText;

        if (ex instanceof PatternSyntaxException) {
            errorText = ((PatternSyntaxException)ex).getDescription();
        } else if (ex instanceof IllegalArgumentException) {
            errorText = ex.getMessage();
        } else {
            errorText = "Failed to match text, see logs for details";
        }

        if (fc != null) {
            errorText = "<span style=\"" + "color: #" + ColorUtil.toHex(fc) + "\">" + StringEscapeUtils.escapeHtml(errorText) + "</span>";
        }

        StringBuilder html = new StringBuilder();
        html.append("<html><body>");

        if (ex instanceof PatternSyntaxException) {
            int index = ((PatternSyntaxException) ex).getIndex();

            if (index >= 0) {
                int lineCount = 0;
                int lineStart = 0;
                String pattern = ((PatternSyntaxException)ex).getPattern();
                for (int i = 0; i < index; i++) {
                    if (i >= pattern.length())
                        break;

                    char a = pattern.charAt(i);
                    if (a == '\n') {
                        lineStart = i + 1;
                        lineCount++;
                    }
                }

                html.append("<a href=\"#\">" + "[").append(lineCount + 1).append(':').append(index - lineStart + 1).append("]</a>&nbsp;");
            }
        }
        
        html.append(errorText);

        html.append("</body></html>");

        return html.toString();
    }

    @Override
    public void dispose() {
        if (groupEditor != null) {
            EditorFactory.getInstance().releaseEditor(groupEditor);
            groupEditor = null;
        }
        if (replacedEditor != null) {
            EditorFactory.getInstance().releaseEditor(replacedEditor);
        }
    }
}
