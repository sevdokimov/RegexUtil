package com.ess.regexutil.regexparser;

import java.util.Map;

import com.ess.regexutil.regexparser.RegexParser.RegexParserState;
import com.ess.util.Helper;


public class SimpleGroupItem extends RegexItem {

	private static final Map<Character, String> map = Helper.createSimpleMap(new Object[]{
			'D', "\\D - Not a digit [^0-9]",
			'S', "\\S - A non-whitespace character: [^ \\t\\n\\x0B\\f\\r]",
			'W', "\\W - A non-word character: [^a-zA-Z_0-9]",
			'd', "\\d - A digit [0-9]",
			's', "\\s - A whitespace character: [ \\t\\n\\x0B\\f\\r]",
			'w', "\\w - A word character: [a-zA-Z_0-9]",
	});
	
	public static final ItemFactory factory = new ItemFactory() {
		public RegexItem tryCreate(RegexParserState st) {
			if (map.containsKey(st.get(1))) {
				return new SimpleGroupItem(st, st.get(1));
			}
			return null;
		}
	};
	
	public SimpleGroupItem(RegexParserState st, char a) {
		super(st, 2);
		hint = map.get(a);
		style = rc.getSymbolGroup();
	}

}
