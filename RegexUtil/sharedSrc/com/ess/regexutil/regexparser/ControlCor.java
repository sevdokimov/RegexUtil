package com.ess.regexutil.regexparser;

import com.ess.regexutil.parsedtext.TextItem;
import com.ess.regexutil.regexparser.RegexParser.RegexParserState;

public class ControlCor extends RegexItem implements IOneSymbol {

	private final char c;
	
	private ControlCor(RegexParserState st, char c) {
		super(st, 3);
		this.c = c;
		hint = "Control character for symbol '" + c + "'\nExample: \\cC (Ctrl+C)\ncontrol charter is (charter ^ 64)\n on this case it is " + SimpleSymbol.getHint((char)(c^64));
		style = rc.getSymbolGroup();
	}

	public static final ItemFactory factory = new ItemFactory() {
		public TextItem tryCreate(RegexParserState st) {
			if (st.get(1) == 'c') {
	            char b = st.get(2);
	            if (b == IRegexParserConst.END_SEQUENCE) {
	                return new ErrorItem(st.getIndex(), 2, "\\c can not be at end of regex");
	            }
	            return new ControlCor(st, b);
			}
			return null;
		}
	};

	public char getSymbol() {
		return c;
	}
	
}
