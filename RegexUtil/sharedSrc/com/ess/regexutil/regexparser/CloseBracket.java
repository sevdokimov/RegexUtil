package com.ess.regexutil.regexparser;

import com.ess.regexutil.regexparser.RegexParser.RegexParserState;

public class CloseBracket extends Bracket {

	public CloseBracket(RegexParserState st) {
		super(st, 1, '(', false);
	}

}
