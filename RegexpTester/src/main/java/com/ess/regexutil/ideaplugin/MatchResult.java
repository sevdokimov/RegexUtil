package com.ess.regexutil.ideaplugin;

import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.Segment;
import com.intellij.openapi.util.TextRange;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.PatternSyntaxException;

public class MatchResult {

    private PatternSyntaxException error;

    private final RegexpTesterPanel.MatchType matchType;
    private final List<Occurrence> occurrences;
    private final List<Pair<TextRange, String>> groupPositions;

    private final String text;

    public MatchResult(RegexpTesterPanel.MatchType matchType, String text, List<Occurrence> occurrences, List<Pair<TextRange, String>> groupPositions) {
        this.matchType = matchType;
        this.text = text;
        this.occurrences = occurrences;
        this.groupPositions = groupPositions;
    }

    public MatchResult(PatternSyntaxException error) {
        this.error = error;
        matchType = null;
        occurrences = List.of();
        text = "";
        groupPositions = List.of();
    }

    public PatternSyntaxException getError() {
        return error;
    }

    public List<Pair<TextRange, String>> getGroupPositions() {
        return groupPositions;
    }

    public RegexpTesterPanel.MatchType getMatchType() {
        return matchType;
    }

    @NotNull
    public List<Occurrence> getOccurrences() {
        return occurrences;
    }

    public String getText() {
        return text;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MatchResult)) return false;
        MatchResult that = (MatchResult) o;

        if (error != that.error) {
            if (error == null || that.error == null)
                return false;

            return !error.getMessage().equals(that.error.getMessage()) || error.getIndex() != that.error.getIndex();
        }

        if (!Objects.equals(text, that.text))
            return false;

        if (!Objects.equals(matchType, that.matchType))
            return false;

        if (!Objects.equals(occurrences, that.occurrences))
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                error == null ? null : error.getMessage(),
                matchType,
                occurrences
                );
    }

    public static class MatchGroup implements Segment {
        private final int start;
        private final int end;

        private final String name;
        private final int index;

        public MatchGroup(String name, int index, int start, int end) {
            assert (start >= 0) == (end >= 0);
            this.start = start;
            this.end = end;

            this.name = name;
            this.index = index;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof MatchGroup)) return false;

            MatchGroup that = (MatchGroup) o;
            return index == that.index && Objects.equals(name, that.name)
                    && start == that.start && end == that.end;
        }

        @Nullable
        public String getName() {
            return name;
        }

        public int getIndex() {
            return index;
        }

        public boolean isMatch() {
            return start >= 0;
        }

        @Override
        public int getStartOffset() {
            assert isMatch();
            return start;
        }

        @Override
        public int getEndOffset() {
            assert isMatch();
            return end;
        }

        @Override
        public int hashCode() {
            return start;
        }
    }

    public static class Occurrence extends TextRange {

        private final List<MatchGroup> groups = new ArrayList<>();

        public Occurrence(Matcher matcher, List<Pair<TextRange, String>> groupPositions) {
            super(matcher.start(), matcher.end());

            assert groupPositions.size() == matcher.groupCount();

            for (int i = 0; i < matcher.groupCount() + 1; i++) {
                String name = null;
                if (i > 0)
                    name = groupPositions.get(i - 1).second;

                groups.add(new MatchGroup(name, i, matcher.start(i), matcher.end(i)));
            }
        }

        public List<MatchGroup> getGroups() {
            return groups;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;

            return super.equals(o)
                    && groups.equals(((Occurrence) o).groups);
        }

        @Override
        public int hashCode() {
            return getStartOffset();
        }
    }
}
