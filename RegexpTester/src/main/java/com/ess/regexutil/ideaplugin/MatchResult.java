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

    private Exception error;

    private final List<Occurrence> occurrences;
    private final List<Pair<TextRange, String>> groupPositions;

    private final State state;

    private final String replaced;

    public MatchResult(State state, List<Occurrence> occurrences, String replaced) {
        this.state = state;
        this.occurrences = occurrences;
        this.replaced = replaced;
        this.groupPositions = state.getGroupPositions();
    }

    public MatchResult(@NotNull Exception error) {
        this.error = error;
        state = null;
        occurrences = List.of();
        groupPositions = List.of();
        replaced = null;
    }

    @Nullable
    public String getReplaced() {
        return replaced;
    }

    public Exception getError() {
        return error;
    }

    public List<Pair<TextRange, String>> getGroupPositions() {
        return groupPositions;
    }

    public MatchType getMatchType() {
        return state == null ? null : state.getMatchType();
    }

    @NotNull
    public List<Occurrence> getOccurrences() {
        return occurrences;
    }

    public String getText() {
        return state == null ? null : state.getText();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MatchResult)) return false;
        MatchResult that = (MatchResult) o;

        if (error != that.error) {
            if (error == null || that.error == null)
                return false;

            if (error.getClass() != that.error.getClass() || !Objects.equals(error.getMessage(), that.error.getMessage()))
                return false;

            if (error instanceof PatternSyntaxException
                    && ((PatternSyntaxException)error).getIndex() != ((PatternSyntaxException)that.error).getIndex()) {
                return false;
            }

            return true;
        }

        if (!Objects.equals(state, that.state))
            return false;

        if (!Objects.equals(replaced, that.replaced))
            return false;

        if (!Objects.equals(occurrences, that.occurrences))
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                error == null ? null : error.getMessage(),
                state,
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

        private final TextRange replacementRange;

        public Occurrence(Matcher matcher, List<Pair<TextRange, String>> groupPositions) {
            this(matcher, groupPositions, null);
        }

        public Occurrence(Matcher matcher, List<Pair<TextRange, String>> groupPositions, @Nullable TextRange replacementRange) {
            super(matcher.start(), matcher.end());

            assert groupPositions.size() == matcher.groupCount();

            for (int i = 0; i < matcher.groupCount() + 1; i++) {
                String name = null;
                if (i > 0)
                    name = groupPositions.get(i - 1).second;

                groups.add(new MatchGroup(name, i, matcher.start(i), matcher.end(i)));
            }

            this.replacementRange = replacementRange;
        }

        public List<MatchGroup> getGroups() {
            return groups;
        }

        public TextRange getReplacementRange() {
            return replacementRange;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;

            Occurrence occurrence = (Occurrence) o;

            return super.equals(o)
                    && Objects.equals(replacementRange, occurrence.replacementRange)
                    && groups.equals((occurrence).groups);
        }

        @Override
        public int hashCode() {
            return getStartOffset();
        }
    }
}
