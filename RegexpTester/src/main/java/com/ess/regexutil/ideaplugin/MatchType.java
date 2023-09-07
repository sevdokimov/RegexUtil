package com.ess.regexutil.ideaplugin;

public enum MatchType {
    SUBSTRING("Substring"), ENTIRE_STRING("Entire string"), BEGINNING("From the beginning"),
    REPLACE("Replace");

    MatchType(String title) {
        this.title = title;
    }

    private final String title;


    @Override
    public String toString() {
        return title;
    }
}
