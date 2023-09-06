package com.ess.regexutil.ideaplugin;

import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;

import java.util.List;
import java.util.Objects;

public class State {

    private final String regexp;
    private final String text;
    private final String replacement;
    private final int flags;
    private final MatchType matchType;
    private final List<Pair<TextRange, String>> groupPositions;

    public State(String regexp, String text, String replacement, int flags, MatchType matchType, List<Pair<TextRange, String>> groupPositions) {
        this.regexp = regexp;
        this.text = text;
        this.replacement = replacement;
        this.flags = flags;
        this.matchType = matchType;
        this.groupPositions = groupPositions;
    }

    public String getRegexp() {
        return regexp;
    }

    public String getText() {
        return text;
    }

    public int getFlags() {
        return flags;
    }

    public MatchType getMatchType() {
        return matchType;
    }

    public String getReplacement() {
        return replacement;
    }

    public List<Pair<TextRange, String>> getGroupPositions() {
        return groupPositions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof State)) return false;
        State state = (State) o;
        return flags == state.flags
                && Objects.equals(regexp, state.regexp)
                && Objects.equals(text, state.text)
                && Objects.equals(replacement, state.replacement)
                && matchType == state.matchType;
    }

    @Override
    public int hashCode() {
        return regexp.hashCode();
    }

}
