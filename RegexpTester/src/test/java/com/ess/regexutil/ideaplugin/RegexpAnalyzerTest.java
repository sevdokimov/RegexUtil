package com.ess.regexutil.ideaplugin;

import com.intellij.openapi.progress.EmptyProgressIndicator;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class RegexpAnalyzerTest extends MyBasePlatformTestCase {

    public void testAnalyze() {
        doTest("abcd", "  ab!", "ab", "ab");
        doTest("abcd", " xxx", "", "", List.of("a"));
        doTest("a(b)c", "  ab!", "a(b)", "ab");
        doTest("a?(b)c", "  ab!", "a?(b)", "ab");
        doTest("x", "  ab!", "", "");
        doTest("a\b", "  ab!", "a", "a");
        doTest("a\\b!", "  a b!", "a\\b", "a");
        doTest("_(123|\\d:\\d|(mmm|rrr)--)", " _1777", "_1", "_1", List.of("2", ":", "m", "r"), "\\d");

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

        doTest("\\b\\d+(\\.\\d+)+\\b", " 44x!!! 5555x ", "\\b\\d+", "44", List.of("\\."));
        doTest("\\b\\d+(\\.\\d+)+\\b", " 44.x", "\\b\\d+\\.", "44.", List.of("\\d+"));
        doTest("\\b\\d+(?<xx>\\.\\d+)+\\b", " 44.x", "\\b\\d+\\.", "44.", List.of("\\d+"));

        doTest("\\b\\d+(\\.\\d+){2,}\\b", " 44.222!!! 5555x ", "\\b\\d+\\.\\d+", "44.222", List.of()); // todo
        doTest("\\b\\d+(\\.\\d+){2,}\\b", " 44.2.!!! 5555x ", "\\b\\d+\\.\\d+", "44.2", List.of());

        doTest("\\b\\d+(\\.\\d+)?\\b", " 44x!!! 5555x ", "\\b\\d+(\\.\\d+)?", "44", List.of("\\b"));
//        doTest("\\b\\d+(\\.\\d+)?\\b", " 44.x", "\\b\\d+\\.", "44.", List.of("\\b", "\\d+"), "\\.");
        doTest("\\b\\d+(\\.\\d+)*\\b", " 44x!!! 5555x ", "\\b\\d+(\\.\\d+)*", "44", List.of("\\b"));
        doTest("\\b\\d+(\\.\\d+)*?\\b", " 44x!!! 5555x ", "\\b\\d+(\\.\\d+)*?", "44", List.of("\\b"));
//        doTest("xy+", " x!!", "x", "x", List.of("y"));

        doTest("((a|e)|(y|(i)))|\\d+", " !", "", "", List.of("a", "e", "y", "i", "\\d+"));
        doTest("((a|e)|(y|(i)))|\\d+", " i", "((a|e)|(y|(i)))|\\d+", "i", List.of());

        doTest("_((?<a>a)(?<b>b))+", " _a", "_(?<a>a)", "_a", List.of("b"));
        doTest("_((a)(bbb))+", " _a", "_(a)", "_a", List.of("b"));
        doTest("_((?:(a))b)+", " _a", "_(?:(a))", "_a", List.of("b"));

        doTest("(\\d+)-\\1", " 1  22 123-12", "(\\d+)-", "123-", List.of("\\1"));
        doTest("_\\Qabc\\E_", " _abc", "_\\Qabc\\E", "_abc", List.of("_"));
        doTest("_\\Qabc\\E_", " _ab", "_\\Qab", "_ab", List.of("c"));
//        doTest("\\b\\d+(\\.\\d+)?(x!|y!)\\b", " 44x", "\\b\\d+(\\.\\d+)?x", "44x", List.of("!"));
//        doTest("\\b\\d+(\\.\\d+)?((x)?!|y!)\\b", " 44x", "\\b\\d+(\\.\\d+)?x", "44x", List.of("!"));
//        doTest("\\b\\d+(abcd!)?m\\b", " 44m 55abcd", "\\b\\d+", "44", List.of("\\b", "!"), "abcd");
    }

    public void testBranchPriority() {
        doTest("a+(b+!|1+!)", "aaaaaaaa  ab a1", "a+b+", "ab", List.of("!", "!"), "1+");
        doTest("a+(b+!|1+!)", "aaaaaaaa  ab a111", "a+1+", "a111", List.of("!", "!"), "b+");
        doTest("1+!|abc!", "aaaaaaaa  11111111 abc", "abc", "abc", List.of("!", "!"), "1+");
//        doTest("a(b(c!))|#1+!", "aaaaaaaa  #11111111 abc", "abc", "abc", List.of("!", "!"), "1+");
//        doTest("#1+!|a(b(c!))", "aaaaaaaa  #11111111 abc", "abc", "abc", List.of("!", "!"), "1+");
    }

    public void testBranchWeigher() {
        assertThat(getBranchWeight("a")).isEqualTo(1);
        assertThat(getBranchWeight("abc")).isEqualTo(3);
//        assertThat(getBranchWeight("(abc)")).isEqualTo(3);
    }

    private void doTest(@Language("RegExp") String regexp, String text, String expectedMatchedRegexp, String matchedText, String ... secondaryRegexp) {
        doTest(regexp, text, expectedMatchedRegexp, matchedText, null, secondaryRegexp);
    }

    private int getBranchWeight(@Language("RegExp") String branch) {
        State state = new State(branch, "", "", 0, MatchType.REPLACE, List.of());
        RegexpAnalyzer analyzer = new RegexpAnalyzer(getProject(), state, null);

        List<RegexpAnalyzer.Item> items = analyzer.parseRegexp();

        int res = 0;

        for (RegexpAnalyzer.Item item : items) {
            res += item.weight;
        }

        return res;
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