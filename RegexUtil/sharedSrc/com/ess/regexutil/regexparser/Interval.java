package com.ess.regexutil.regexparser;

import com.ess.regexutil.regexparser.RegexParser.RegexParserState;

public class Interval extends RegexItem {

	public Interval(RegexParserState st, IOneSymbol first, SimpleSymbol minus, IOneSymbol second) {
		super(st, first.getLength() + minus.getLength() + second.getLength());
		hint = "Matches all character from " 
			+ SimpleSymbol.getHint(first.getSymbol()) 
			+ " to "
			+ SimpleSymbol.getHint(second.getSymbol());
		style = rc.getSymbolGroup();
	}

}
