package com.ess.regexutil.ideaplugin;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.editor.markup.RangeHighlighter;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class ReplacementTextFieldTest extends RegexPanelTestBase {

    public void testHighlightOnEditorInitializing() {
        edt(() -> {
            panel.matchTypeCombobox.setItem(RegexpTesterPanel.MatchType.REPLACE);
            panel.replacementInput.setText("_$1_");

            assertThat(panel.replacementInput.getEditor()).isNull();

            initEditor(panel.replacementInput);

            checkHighlight(Map.of("$1", ReplacementTextField.GROUP_REF_KEY));
        });
    }

    public void testRehighlightOnTextChange() {
        edt(() -> {
            panel.matchTypeCombobox.setItem(RegexpTesterPanel.MatchType.REPLACE);
            panel.replacementInput.setText("_$1_");
            initEditor(panel.replacementInput);

            checkHighlight(Map.of("$1", ReplacementTextField.GROUP_REF_KEY));

            panel.replacementInput.setText("$5-$xx");

            checkHighlight(Map.of(
                    "$5", ReplacementTextField.GROUP_REF_KEY,
                    "$x", ReplacementTextField.ERROR_KEY
                    ));
        });
    }

    public void testHighlighting() {
        edt(() -> {
            panel.matchTypeCombobox.setItem(RegexpTesterPanel.MatchType.REPLACE);
            initEditor(panel.replacementInput);

            doTestHighlighting("", Map.of());
            doTestHighlighting("   1223 ", Map.of());
            doTestHighlighting("   1223 \\", Map.of("\\", ReplacementTextField.ERROR_KEY));
            doTestHighlighting("   1223 \\x", Map.of("\\x", ReplacementTextField.ESC_KEY));
            doTestHighlighting("   1223 \\x\\", Map.of("\\x", ReplacementTextField.ESC_KEY, "\\", ReplacementTextField.ERROR_KEY));

            doTestHighlighting("$xx ${xxx} ${} $22$", Map.of(
                    "$x", ReplacementTextField.ERROR_KEY,
                    "${xxx}", ReplacementTextField.GROUP_REF_KEY,
                    "${", ReplacementTextField.ERROR_KEY,
                    "$22", ReplacementTextField.GROUP_REF_KEY,
                    "$", ReplacementTextField.ERROR_KEY
                    ));
        });
    }

    private void doTestHighlighting(String replacement, Map<String, TextAttributesKey> expectedHighlighting) {
        panel.replacementInput.setText(replacement);
        checkHighlight(expectedHighlighting);
    }

    private void checkHighlight(Map<String, TextAttributesKey> expectedHighlighting) {
        Editor editor = panel.replacementInput.getEditor();

        String replacement = editor.getDocument().getText();

        Map<String, TextAttributesKey> highlighting = new HashMap<>();

        for (RangeHighlighter hlt : editor.getMarkupModel().getAllHighlighters()) {
            highlighting.put(replacement.substring(hlt.getStartOffset(), hlt.getEndOffset()), hlt.getTextAttributesKey());
        }

        assertEquals(expectedHighlighting, highlighting);
    }


}