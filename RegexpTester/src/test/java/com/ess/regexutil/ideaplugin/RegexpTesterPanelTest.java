package com.ess.regexutil.ideaplugin;

import static org.assertj.core.api.Assertions.assertThat;

public class RegexpTesterPanelTest extends RegexPanelTestBase {

    public void testMatchModes() {
        init("\\d+", "1111    777    2");

        assertThat(panel.matchTypeCombobox.getItem()).isEqualTo(MatchType.SUBSTRING);

        assertMatches("1111", "777", "2");

        edt(() -> panel.matchTypeCombobox.setItem(MatchType.BEGINNING));

        waitForResults();

        assertMatches("1111");

        edt(() -> panel.matchTypeCombobox.setItem(MatchType.ENTIRE_STRING));

        waitForResults();

        assertMatches();
    }

}