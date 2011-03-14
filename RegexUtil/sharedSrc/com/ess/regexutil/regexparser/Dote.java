package com.ess.regexutil.regexparser;

import java.util.regex.Pattern;

import com.ess.regexutil.regexparser.RegexParser.RegexParserState;

public class Dote extends RegexItem {

	public Dote(RegexParserState st) {
		super(st, 1);
		hint = (st.getFlags() & Pattern.DOTALL) == 0 ?
				"The dot matches any character except line terminators.\n\n" +
				"To make the dot match line terminators as well,\n" +
				"switch on flag \"Dotall\" (?s)\n"
				
				: "The dot matches any character.";
		style = rc.getComma();
	}

}
