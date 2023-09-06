package com.ess.regexutil.ideaplugin;

import com.intellij.openapi.progress.EmptyProgressIndicator;
import org.intellij.lang.annotations.Language;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class RegexpAnalyzerTest extends MyBasePlatformTestCase {

    public void testAnalyze() throws ExecutionException, InterruptedException {
        doTest("abcd", "  ab!", "ab", "ab");
        doTest("a(b)c", "  ab!", "a(b)", "ab");
        doTest("a?(b)c", "  ab!", "a?(b)", "ab");
        doTest("x", "  ab!", "", "");
        doTest("a\b", "  ab!", "a", "a");
        doTest("a\\b!", "  a b!", "a\\b", "a");
    }

    private void doTest(@Language("RegExp") String regexp, String text, String expectedMatchedRegexp, String matchedText, String ... secondaryRegexp) throws ExecutionException, InterruptedException {
        State state = new State(regexp, text, "", 0, MatchType.REPLACE, List.of());

        RegexpAnalyzer analyzer = new RegexpAnalyzer(getProject(), state, null);

        analyzer.run(new EmptyProgressIndicator());

        String matchedRegexp = analyzer.getMatchedRegexp().stream()
                .map(r -> r.substring(regexp))
                .collect(Collectors.joining());

        assertEquals(expectedMatchedRegexp, matchedRegexp);

        if (expectedMatchedRegexp.length() > 0) {
            assertEquals(matchedText, analyzer.getMatchedText().substring(text));
        }

        assertEquals(List.of(secondaryRegexp), analyzer.getAdditionalMatchedRegexp().stream()
                .map(r -> regexp)
                .collect(Collectors.toList()));
    }

}