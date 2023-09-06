package com.ess.regexutil.ideaplugin;

import com.intellij.openapi.progress.EmptyProgressIndicator;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;

public class RegexpAnalyzerTest extends MyBasePlatformTestCase {

    public void testAnalyze() {
        doTest("abcd", "  ab!", "ab", "ab");
        doTest("a(b)c", "  ab!", "a(b)", "ab");
        doTest("a?(b)c", "  ab!", "a?(b)", "ab");
        doTest("x", "  ab!", "", "");
        doTest("a\b", "  ab!", "a", "a");
        doTest("a\\b!", "  a b!", "a\\b", "a");
        doTest("_(123|\\d:\\d|(mmm|rrr)--)", " _1777", "_", "_", List.of("2", ":", "m", "r"), "1", "\\d");

        doTest("a\\x20!", " a b!", "a\\x20", "a ", List.of("!"));
        doTest("a[0-9]b", " a b!", "a", "a", List.of("[0-9]"));
        doTest("a[a-z&&[^bc]]b", " a1b!", "a", "a", List.of("[a-z&&[^bc]]"));
        doTest("a\\p{Upper}b", " a1b!", "a", "a", List.of("\\p{Upper}"));
        doTest("a\\p{Upper}b", " aX!", "a\\p{Upper}", "aX", List.of("b"));
        doTest("^\\d+x", "44", "^\\d+", "44", List.of("x"));
        doTest("^\\d+x", "_44", "^", "", List.of("\\d+"));
        doTest("\\d(?:$| )", " 44", "\\d(?:$| )", "4", List.of());
        doTest("\\d(?:$| )", " 44!!!", "\\d", "4", List.of("$", " "));
        doTest("\\b\\d+\\b", " 44x!!! 5555x ", "\\b\\d+", "44", List.of("\\b"));
    }

    private void doTest(@Language("RegExp") String regexp, String text, String expectedMatchedRegexp, String matchedText, String ... secondaryRegexp) {
        doTest(regexp, text, expectedMatchedRegexp, matchedText, null, new String[0]);
    }

    private void doTest(@Language("RegExp") String regexp, String text, String expectedMatchedRegexp, String matchedText, @Nullable List<String> blockers, String ... secondaryRegexp) {
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

        if (blockers != null) {
            assertEquals(blockers, analyzer.getBlockers().stream()
                    .map(r -> r.substring(regexp))
                    .collect(Collectors.toList()));
        }

        assertEquals(List.of(secondaryRegexp), analyzer.getAdditionalMatchedRegexp().stream()
                .map(r -> r.substring(regexp))
                .collect(Collectors.toList()));
    }

}