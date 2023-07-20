package com.ess.regexutil.ideaplugin;

import java.awt.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class RegexHighlighterTest extends RegexPanelTestBase {

    private List<String> getHighlightedText() {
        return text(findHighlights(panel.textEditor, RegexpTesterPanel.GROUP_HLT));
    }

    public void testGroupSelection() {
        init("(a(b))((c)d)", "abcd abcd");

        edt(() -> initRegexEditor());

        edt(() -> {
            panel.regexEditor.getEditor().getCaretModel().moveToOffset(0);

            assertThat(getHighlightedText()).containsExactly("ab", "ab");

            panel.regexEditor.getEditor().getCaretModel().moveToOffset(2);

            assertThat(getHighlightedText()).containsExactly("b", "b");

            panel.regexEditor.getEditor().getCaretModel().moveToOffset(1);

            assertThat(getHighlightedText()).containsExactly("ab", "ab");

            panel.regexEditor.getEditor().getCaretModel().moveToOffset(3);

            assertThat(getHighlightedText()).containsExactly("b", "b");

            panel.regexEditor.getEditor().getCaretModel().moveToOffset(6);

            assertThat(getHighlightedText()).containsExactly("ab", "ab");

            panel.regexEditor.getEditor().getCaretModel().moveToOffset(4);

            assertThat(getHighlightedText()).containsExactly("b", "b");

            panel.regexEditor.getEditor().getCaretModel().moveToOffset(7);

            assertThat(getHighlightedText()).containsExactly("c", "c");

            panel.regexEditor.getEditor().getCaretModel().moveToOffset(5);

            assertThat(getHighlightedText()).containsExactly("b", "b");

            panel.regexEditor.getEditor().getCaretModel().moveToOffset(10);

            assertThat(getHighlightedText()).containsExactly("c", "c");

            panel.regexEditor.getEditor().getCaretModel().moveToOffset(12);

            assertThat(getHighlightedText()).containsExactly("cd", "cd");
        });
    }

    public void testNamedGroupSelection() {
        init("(?:x) (?<name>aaa) (?<name2>bbb)", "  x aaa bbb");

        edt(() -> initRegexEditor());

        edt(() -> {
            panel.regexEditor.getEditor().getCaretModel().moveToOffset(6);

            assertThat(getHighlightedText()).containsExactly("aaa");

            panel.regexEditor.getEditor().getCaretModel().moveToOffset(31);

            assertThat(getHighlightedText()).containsExactly("bbb");

            panel.regexEditor.getEditor().getCaretModel().moveToOffset(8);

            assertThat(getHighlightedText()).containsExactly("aaa");
        });
    }

    public void testUnionHighlight() {
        init("(?:aaa|bbb|ccc)+", "  x aaa bbb");

        edt(() -> initRegexEditor());

        edt(() -> {
            panel.regexEditor.getEditor().getCaretModel().moveToOffset(6);

            assertThat(text(findHighlights(panel.regexEditor.getEditor(), RegexHighlighter.ELEMENT_UNDER_CARET))).containsExactly("aaa", "bbb", "ccc");

            panel.regexEditor.getEditor().getCaretModel().moveToOffset(4);

            assertThat(text(findHighlights(panel.regexEditor.getEditor(), RegexHighlighter.ELEMENT_UNDER_CARET))).isEmpty();

            panel.regexEditor.getEditor().getCaretModel().moveToOffset(7);

            assertThat(text(findHighlights(panel.regexEditor.getEditor(), RegexHighlighter.ELEMENT_UNDER_CARET))).containsExactly("aaa", "bbb", "ccc");
        });
    }

    public void testBackreferenceHighlight() {
        init("(aaa) \\1", "");

        edt(() -> initRegexEditor());

        edt(() -> {
            panel.regexEditor.getEditor().getCaretModel().moveToOffset(6);

            assertThat(text(findHighlights(panel.regexEditor.getEditor(), RegexHighlighter.ELEMENT_UNDER_CARET))).containsExactly("(aaa)", "\\1");
        });
    }

    public void testNamedBackreferenceHighlight() {
        init("(?<a>aaa) \\k<a>", "");

        edt(() -> initRegexEditor());

        edt(() -> {
            panel.regexEditor.getEditor().getCaretModel().moveToOffset(11);

            assertThat(text(findHighlights(panel.regexEditor.getEditor(), RegexHighlighter.ELEMENT_UNDER_CARET))).containsExactly("(?<a>aaa)", "\\k<a>");
        });
    }

    public void testBackreferenceAndBracketHighlight() {
        init("(aaa)\\1", "  aaaaaa");

        edt(() -> initRegexEditor());

        edt(() -> {
            panel.regexEditor.getEditor().getCaretModel().moveToOffset(5);

            assertThat(text(findHighlights(panel.regexEditor.getEditor(), RegexHighlighter.ELEMENT_UNDER_CARET))).containsExactly();
            assertThat(getHighlightedText()).containsExactly("aaa");
        });
    }

    public void testBackreferenceHighlightError2() {
        init("\\9 (aaa)", "");

        edt(() -> initRegexEditor());

        edt(() -> {
            panel.regexEditor.getEditor().getCaretModel().moveToOffset(1);

            assertThat(text(findHighlights(panel.regexEditor.getEditor(), RegexHighlighter.ELEMENT_UNDER_CARET))).containsExactly();
        });
    }


}