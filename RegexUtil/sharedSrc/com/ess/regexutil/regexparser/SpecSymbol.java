package com.ess.regexutil.regexparser;

import java.util.regex.Pattern;

import com.ess.regexutil.regexparser.RegexParser.RegexParserState;

public class SpecSymbol extends RegexItem {

	private final char a;
	
	private final static String[] HINTS = {
		"'^' Begin of input sequence.\n" +
		"To make the '^' match line start (after a line terminator) as well," +
		"switch on flag \"Multiline\" (?m)",
		
		"'^' Begin of input sequence or line start (after a line terminator).",

		"'$' End of input sequence.\n" +
		"To make the '$' match line end (before a line terminator) as well," +
		"switch on flag \"Multiline\" (?m)",
		
		"'$' End of input sequence or line end (before a line terminator).",
	};
	
	public SpecSymbol(RegexParserState st) {
		super(st, 1);
		this.a = st.get();
		assert a == '^' || a == '$';
		hint = HINTS[((a == '^' ? 0 : 1) << 1)
		             + ((st.getFlags() & Pattern.MULTILINE) == 0 ? 0 : 1)];
		style = rc.getSpecSimbol();
	}

	public char getA() {
		return a;
	}

}
