package com.ess.regexutil.ideaplugin;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RegexPanelStateServiceTest {

    @Test
    public void testEscape() {
        doTestEscape("");
        doTestEscape("d");
        doTestEscape("|d");
        doTestEscape("||");
        doTestEscape("||d");
        doTestEscape("aaa");
        doTestEscape("a$PATH$aa");
        doTestEscape("a$PATH$|$PATH$aa");
    }

    private void doTestEscape(String s) {
        String escaped = RegexPanelStateService.State.escape(s);
        String res = RegexPanelStateService.State.unescape(escaped);
        assertEquals(s, res);
    }
}