package com.ess.regexutil.regexparser;

import com.ess.regexutil.regexparser.RegexParser.RegexParserState;
import com.ess.util.Helper;

public class SimpleSymbol extends RegexItem implements IOneSymbol {

	private final char symbol;
	
	public SimpleSymbol(RegexParserState st) {
		super(st, 1);
		symbol = st.get();
		hint = getHint(symbol);
	}

	public char getSymbol() {
		return symbol;
	}

	public static final String getHint(char a) {
		if (!Character.isISOControl(a)) {
			return "'" + a + "' (\\u" + Helper.getCharHexCode(a) + ')';
		} else {
			return "'\\u" + Helper.getCharHexCode(a) + '\'';
		}
	}
	
}
