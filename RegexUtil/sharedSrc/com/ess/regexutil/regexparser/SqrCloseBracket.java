package com.ess.regexutil.regexparser;

import com.ess.regexutil.regexparser.RegexParser.RegexParserState;

public class SqrCloseBracket extends Bracket {

	public SqrCloseBracket(RegexParserState st) {
		super(st, 1, '[', false);
	}

}
