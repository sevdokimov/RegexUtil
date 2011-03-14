package com.ess.regexutil.regexparser;

import java.util.Map;

import com.ess.regexutil.regexparser.RegexParser.RegexParserState;
import com.ess.util.Helper;


public class SpecEscape extends RegexItem {

	private static final Map<Character, String> map = Helper.createSimpleMap(new Object[]{
			'A', "\\A - The beginning of the input",
			'B', "\\B - A non-word boundary",
			'G', "\\G - The end of the previous match",
			'Z', "\\Z - End of input, does not consider last line terminator",
			'b', "\\b - A word boundary",
			'z', "\\z - End of input",
	});
	
	public static final ItemFactory factory = new ItemFactory() {
		public RegexItem tryCreate(RegexParserState st) {
			if (map.containsKey(st.get(1))) {
				return new SpecEscape(st, st.get(1));
			}
			return null;
		}
	};

	public SpecEscape(RegexParserState st, char a) {
		super(st, 2);
		hint = map.get(a);
		style = rc.getSpecSimbol();
	}

}
