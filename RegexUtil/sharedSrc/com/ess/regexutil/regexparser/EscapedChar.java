package com.ess.regexutil.regexparser;

import com.ess.regexutil.regexparser.RegexParser.RegexParserState;

public class EscapedChar extends RegexItem implements IOneSymbol {

	private final char symbol;
	
	private static final String needEscape = "\\.*?+|^$(){[";
	private static final String needEscapeBrack = "\\[]$^-";
	
	private final boolean notNeed;
	
	public EscapedChar(RegexParserState st) {
		super(st, 2);
		symbol = st.get(1);
		String s = ((st.getFlags() & IRegexParserConst.IN_SQR_BRACKET) == 0) ? needEscape : needEscapeBrack;
		notNeed = s.indexOf(symbol) == -1;
		if (notNeed) {
			hint = SimpleSymbol.getHint(symbol) + " (escaping is not necessarily)";
			style = rc.getNotUsage();
		} else {
			hint = SimpleSymbol.getHint(symbol);
		}
	}
	
	public static final ItemFactory factory = new ItemFactory() {
		public RegexItem tryCreate(RegexParserState st) {
			char a = st.get(1);
			if (a == '0' || a >= 'a' && a <= 'z' || a >= 'A' && a <= 'Z')
				return null;
			return new EscapedChar(st);
		}
	};

	public char getSymbol() {
		return symbol;
	}
	
}
