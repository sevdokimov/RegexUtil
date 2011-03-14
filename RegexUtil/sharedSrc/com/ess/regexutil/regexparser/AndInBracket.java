package com.ess.regexutil.regexparser;

import com.ess.regexutil.regexparser.RegexParser.RegexParserState;



public class AndInBracket extends RegexItem {

	public AndInBracket(RegexParserState st) {
		super(st, 2);
		hint = Messages.get("AndInBracket.AndInBracket.description"); //$NON-NLS-1$
		style = rc.getComma();
	}

}
