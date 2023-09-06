package com.ess.regexutil.ideaplugin;

import com.ess.regexutil.ideaplugin.utils.HoverEditorListener;
import com.google.common.collect.Iterables;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.XmlHighlighterColors;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.util.Segment;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class ResultPanelTest extends RegexPanelTestBase {

    private static final String PREV_MATCH_LINK = "&lt;&lt;";
    private static final String NEXT_MATCH_LINK = "&gt;&gt;";

    public void testEmptyRegexp() {
        init("", "xxx");
        assertResultActivePanel(MatchingResultPanel.CARD_EMPTY);
        init("(", "xxx");
        assertResultActivePanel(MatchingResultPanel.CARD_ERROR);
        init("", "xxx");
        assertResultActivePanel(MatchingResultPanel.CARD_EMPTY);
    }

    public void testInvalidRegexp1() {
        init("\\d+(", "");

        assertResultActivePanel(MatchingResultPanel.CARD_ERROR);

        edt(() -> {
            assertThat(panel.resultsPanel.errorLabel.getText()).contains("Unclosed group");
            emulateClick(panel.resultsPanel.errorLabel);
            assertRegexCaretAfter("(");
        });
    }

    public void testInvalidRegexp2() {
        init("\n\n    \\u ff\n", "");

        assertResultActivePanel(MatchingResultPanel.CARD_ERROR);

        edt(() -> {
            assertThat(panel.resultsPanel.errorLabel.getText()).contains("Illegal Unicode escape");
            emulateClick(panel.resultsPanel.errorLabel);
            assertRegexCaretAfter("  \\u");
        });
    }

    public void testSelectMatchUnderCaret() {
        init("\\d+", "11  22");

        edt(() -> {
            assertResultActivePanel(MatchingResultPanel.CARD_MATCHES);

            assertThat(panel.resultsPanel.matchesTitle.getText()).contains("2"); // 6 matches

            panel.textEditor.getCaretModel().moveToOffset(panel.textEditor.getDocument().getTextLength());

            assertThat(panel.resultsPanel.getSelectedOccurrence()).isEqualTo(1);
            assertThat(panel.resultsPanel.groupEditor.getDocument().getText()).contains(": 22");
        });
    }

    public void testSelectMatchUnderCaret2() {
        init("\\d+|", "1900-01");

        edt(() -> {
            assertResultActivePanel(MatchingResultPanel.CARD_MATCHES);

            assertThat(panel.resultsPanel.matchesTitle.getText()).contains("4"); // 6 matches

            panel.textEditor.getCaretModel().moveToOffset(1);

            assertThat(panel.resultsPanel.getSelectedOccurrence()).isEqualTo(0);
            assertThat(panel.resultsPanel.groupEditor.getDocument().getText()).contains(": 1900");

            panel.textEditor.getCaretModel().moveToOffset(5);

            assertThat(panel.resultsPanel.getSelectedOccurrence()).isEqualTo(2);
            assertThat(panel.resultsPanel.groupEditor.getDocument().getText()).contains(": 01");

            panel.textEditor.getCaretModel().moveToOffset(7);

            assertThat(panel.resultsPanel.getSelectedOccurrence()).isEqualTo(3);
            assertCaretAfter("-01");

            panel.textEditor.getCaretModel().moveToOffset(4);

            assertThat(panel.resultsPanel.getSelectedOccurrence()).isEqualTo(1);
            assertCaretAfter("1900");

            panel.textEditor.getCaretModel().moveToOffset(0);

            assertThat(panel.resultsPanel.getSelectedOccurrence()).isEqualTo(0);
            assertThat(panel.resultsPanel.groupEditor.getDocument().getText()).contains(": 1900");
        });
    }

    public void testNextPrevMatch() {
        init("\\d+|", "1900-01");

        edt(() -> {
            assertResultActivePanel(MatchingResultPanel.CARD_MATCHES);

            assertThat(panel.resultsPanel.matchesTitle.getText()).contains("4"); // 6 matches

            // Select the first match
            panel.textEditor.getCaretModel().moveToOffset(1);

            assertResultActivePanel(MatchingResultPanel.CARD_GROUPS);

            assertThat(panel.resultsPanel.getSelectedOccurrence()).isEqualTo(0);
            assertThat(panel.resultsPanel.groupEditor.getDocument().getText()).contains(": 1900");
            assertThat(resultGroupTitle()).contains("1 / 4");
            assertFalse(hasLink(panel.resultsPanel.groupTitle, PREV_MATCH_LINK));

            click(panel.resultsPanel.groupTitle, NEXT_MATCH_LINK);

            assertThat(panel.resultsPanel.getSelectedOccurrence()).isEqualTo(1);
            assertThat(resultGroupTitle()).contains("2 / 4");
            assertThat(panel.resultsPanel.groupEditor.getDocument().getText()).endsWith(": ");
            assertTrue(hasLink(panel.resultsPanel.groupTitle, PREV_MATCH_LINK));
            assertCaretAfter("1900");

            click(panel.resultsPanel.groupTitle, NEXT_MATCH_LINK);

            assertThat(panel.resultsPanel.getSelectedOccurrence()).isEqualTo(2);
            assertThat(resultGroupTitle()).contains("3 / 4");
            assertThat(panel.resultsPanel.groupEditor.getDocument().getText()).endsWith(": 01");
            assertSelectedTextAfterCaret("01");

            click(panel.resultsPanel.groupTitle, NEXT_MATCH_LINK);

            assertThat(panel.resultsPanel.getSelectedOccurrence()).isEqualTo(3);
            assertThat(resultGroupTitle()).contains("4 / 4");
            assertThat(panel.resultsPanel.groupEditor.getDocument().getText()).endsWith(": ");
            assertCaretAfter("-01");
            assertFalse(hasLink(panel.resultsPanel.groupTitle, NEXT_MATCH_LINK));

            click(panel.resultsPanel.groupTitle, PREV_MATCH_LINK);

            assertThat(panel.resultsPanel.getSelectedOccurrence()).isEqualTo(2);
            assertThat(resultGroupTitle()).contains("3 / 4");
            assertThat(panel.resultsPanel.groupEditor.getDocument().getText()).endsWith(": 01");
            assertSelectedTextAfterCaret("01");

            click(panel.resultsPanel.groupTitle, PREV_MATCH_LINK);

            assertThat(panel.resultsPanel.getSelectedOccurrence()).isEqualTo(1);
            assertThat(resultGroupTitle()).contains("2 / 4");
            assertThat(panel.resultsPanel.groupEditor.getDocument().getText()).endsWith(": ");
            assertTrue(hasLink(panel.resultsPanel.groupTitle, PREV_MATCH_LINK));
            assertCaretAfter("1900");

            click(panel.resultsPanel.groupTitle, PREV_MATCH_LINK);

            assertThat(panel.resultsPanel.getSelectedOccurrence()).isEqualTo(0);
            assertThat(panel.resultsPanel.groupEditor.getDocument().getText()).contains(": 1900");
            assertThat(resultGroupTitle()).contains("1 / 4");
            assertFalse(hasLink(panel.resultsPanel.groupTitle, PREV_MATCH_LINK));
        });
    }

    private String resultGroupTitle() {
        return StringUtil.removeHtmlTags(panel.resultsPanel.groupTitle.getText());
    }

    public void testGroups() {
        init("(\\d{4})-(\\d\\d)-(?<day>\\d\\d)(?: +(?<time>\\d\\d:\\d\\d:\\d\\d))?", " 2023-04-02 ");

        MatchResult.Occurrence occurrence = Iterables.getOnlyElement(panel.resultsPanel.getResult().getOccurrences());
        assertThat(occurrence.getGroups().get(0).isMatch()).isTrue();
        assertThat(occurrence.getGroups().get(1).isMatch()).isTrue();
        assertThat(occurrence.getGroups().get(4).isMatch()).isFalse();

        edt(() -> {
            assertResultActivePanel(MatchingResultPanel.CARD_GROUPS);

            assertThat(resultGroupTitle()).contains("1 / 1");
            assertThat(panel.resultsPanel.getSelectedOccurrence()).isEqualTo(0);
            assertFalse(hasLink(panel.resultsPanel.groupTitle, PREV_MATCH_LINK));
            assertFalse(hasLink(panel.resultsPanel.groupTitle, NEXT_MATCH_LINK));

            RangeHighlighter[] hlts = findHighlights(panel.resultsPanel.groupEditor, HoverEditorListener.ON_HOVER_KEY);

            RangeHighlighter missedGroup = hlts[3];
            assertThat(text(missedGroup)).isEqualTo("time: null");
            assertThat(missedGroup.getTextAttributesKey()).isEqualTo(XmlHighlighterColors.XML_COMMENT);

            RangeHighlighter yearGroup = hlts[0];
            assertThat(text(yearGroup)).endsWith(": 2023");
            assertThat(yearGroup.getTextAttributesKey()).isNull();

            RangeHighlighter dayGroup = hlts[2];
            assertThat(text(dayGroup)).endsWith(": 02").startsWith("day [");

            RangeHighlighter entireMatchedTextGroup = hlts[4];
            assertThat(text(entireMatchedTextGroup)).endsWith(": 2023-04-02");
            assertThat(entireMatchedTextGroup.getTextAttributesKey()).isNull();

            // test clicking on groups
            findInnerLink(panel.resultsPanel.groupEditor, entireMatchedTextGroup).navigate(getProject());
            assertSelectedTextAfterCaret("2023-04-02");

            findInnerLink(panel.resultsPanel.groupEditor, yearGroup).navigate(getProject());
            assertSelectedTextAfterCaret("2023");

            assertThat(findInnerLink(panel.resultsPanel.groupEditor, missedGroup)).isNull();

            // test highlighting
            assertThat(findHighlights(panel.textEditor, RegexpTesterPanel.GROUP_HLT)).isEmpty();
            assertThat(findInnerHighlight(panel.resultsPanel.groupEditor, entireMatchedTextGroup, MatchingResultPanel.GROUP_TEXT).getTextAttributes()).isNull();
            assertThat(findInnerHighlight(panel.resultsPanel.groupEditor, yearGroup, MatchingResultPanel.GROUP_TEXT).getTextAttributes()).isNull();
            assertThat(findInnerHighlight(panel.resultsPanel.groupEditor, missedGroup, MatchingResultPanel.GROUP_TEXT)).isNull();

            initRegexEditor();

            withHover(entireMatchedTextGroup, () -> {
                RangeHighlighter[] textHlt = findHighlights(panel.textEditor, RegexpTesterPanel.GROUP_HLT);
                assertThat(textHlt).hasSize(1);

                assertThat(text(textHlt[0])).isEqualTo("2023-04-02");

                RangeHighlighter groupTextHlt = findInnerHighlight(panel.resultsPanel.groupEditor, entireMatchedTextGroup, MatchingResultPanel.GROUP_TEXT);
                assertThat(groupTextHlt.getTextAttributes()).isEqualTo(MatchingResultPanel.CURRENT_GROUP_ATTR);

                assertThat(findHighlights(panel.regexEditor.getEditor(), RegexpTesterPanel.GROUP_HLT)).isEmpty();
            });

            assertThat(findHighlights(panel.textEditor, RegexpTesterPanel.GROUP_HLT)).isEmpty();
            RangeHighlighter groupTextHlt = findInnerHighlight(panel.resultsPanel.groupEditor, entireMatchedTextGroup, MatchingResultPanel.GROUP_TEXT);
            assertThat(groupTextHlt.getTextAttributes().getEffectColor()).isNull();

            withHover(dayGroup, () -> {
                RangeHighlighter[] textHlt = findHighlights(panel.textEditor, RegexpTesterPanel.GROUP_HLT);
                assertThat(textHlt).hasSize(1);

                assertThat(text(textHlt[0])).isEqualTo("02");

                RangeHighlighter dayTextHlt = findInnerHighlight(panel.resultsPanel.groupEditor, dayGroup, MatchingResultPanel.GROUP_TEXT);
                assertThat(dayTextHlt.getTextAttributes()).isEqualTo(MatchingResultPanel.CURRENT_GROUP_ATTR);

                RangeHighlighter[] regexGroupHlt = findHighlights(panel.regexEditor.getEditor(), RegexpTesterPanel.GROUP_HLT);
                assertThat(regexGroupHlt).hasSize(1);

                assertThat(text(regexGroupHlt[0])).isEqualTo("(?<day>\\d\\d)");
            });

            assertThat(findHighlights(panel.regexEditor.getEditor(), RegexpTesterPanel.GROUP_HLT)).isEmpty();
        });
    }

    public void testAnalyzingButton() {
        init("\\d+!", " 2023 ");

        edt(() -> {
            click(panel.resultsPanel.analyzeButton);
            assert !panel.resultsPanel.analyzeButton.isEnabled();
        });

        waitFor(() -> {
            return Stream.of(panel.textEditor.getMarkupModel().getAllHighlighters()).anyMatch(h -> h.getUserData(RegexpTesterPanel.MATCHED_PARTS));
        });

        edt(() -> {
            assert !panel.resultsPanel.analyzeButton.isEnabled();
        });
    }

    public void testReplaceError() {
        init("\\d+", " 2023 | +04 | -02");

        edt(() -> {
            panel.matchTypeCombobox.setItem(MatchType.REPLACE);
            panel.replacementInput.setText("a\\");
        });

        waitForResults();

        edt(() -> {
            assertResultActivePanel(MatchingResultPanel.CARD_ERROR);

            assertThat(panel.resultsPanel.errorLabel.getText()).contains("Invalid replacement: ", "escaped is missing");
        });
    }

    public void testReplaceError2() {
        init("\\d+", " 2023 | +04 | -02");

        edt(() -> {
            panel.matchTypeCombobox.setItem(MatchType.REPLACE);
            panel.replacementInput.setText("a$9");
        });

        waitForResults();

        edt(() -> {
            assertResultActivePanel(MatchingResultPanel.CARD_ERROR);

            assertThat(panel.resultsPanel.errorLabel.getText()).contains("Invalid replacement: ", "No group 9");
        });
    }

    public void testReplaceError3() {
        init("\\d+", " 2023 | +04 | -02");

        edt(() -> {
            panel.matchTypeCombobox.setItem(MatchType.REPLACE);
            panel.replacementInput.setText("a${aaa}");
        });

        waitForResults();

        edt(() -> {
            assertResultActivePanel(MatchingResultPanel.CARD_ERROR);

            assertThat(panel.resultsPanel.errorLabel.getText()).contains("Invalid replacement: ", "No group", "aaa");
        });
    }

    public void testReplace() {
        init("([+\\-])?\\d+", " 2023 | +04 | -02");

        edt(() -> {
            panel.matchTypeCombobox.setItem(MatchType.REPLACE);
            panel.replacementInput.setText("($1)");
        });

        waitForResults();

        edt(() -> {
            assertResultActivePanel(MatchingResultPanel.CARD_REPLACED);

            checkReplaced(" () | (+) | (-)", "()", "(+)", "(-)");

            panel.replacementInput.setText("X");
        });

        waitForResults();

        checkReplaced(" X | X | X", "X", "X", "X");

        edt(() -> panel.replacementInput.setText(""));

        waitForResults();

        checkReplaced("  |  | ", "", "", "");
    }

    private void checkReplaced(String replaced, String ... expectedOccurrences) {
        invokeLaterIfNeeded(() -> {
            assertThat(panel.resultsPanel.getResult().getReplaced()).isEqualTo(replaced);

            Editor editor = panel.resultsPanel.replacedEditor;
            assertThat(editor.getDocument().getText()).isEqualTo(replaced);

            String[] occurrences = Stream.of(editor.getMarkupModel().getAllHighlighters())
                    .sorted(Segment.BY_START_OFFSET_THEN_END_OFFSET)
                    .map(s -> TextRange.create(s).substring(replaced))
                    .toArray(String[]::new);

            assertOrderedEquals(expectedOccurrences, occurrences);
        });
    }
}
