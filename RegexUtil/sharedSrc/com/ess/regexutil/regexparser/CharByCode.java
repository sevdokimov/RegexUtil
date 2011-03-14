package com.ess.regexutil.regexparser;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ess.regexutil.regexparser.RegexParser.RegexParserState;
import com.ess.util.Helper;


public class CharByCode extends RegexItem implements IOneSymbol {

	private static final Pattern charByCodePattern = Pattern.compile("(0[0-3]?[0-7]{1,2})|(?:x([\\p{XDigit}]{2}))|(?:u([\\p{XDigit}]{4}))|([aefnrtv])");
	
	private static final Map<Character, String> hintMap = Helper.createSimpleMap(new Object[]{
			'\t', "The tab character '\\t' ('\\u0009')",
			'\n', "The newline (line feed) character '\\n' ('\\u000A')",
			'\r', "The carriage-return character '\\r' ('\\u000D')",
			'\u000C', "The form-feed character '\\f' ('\\u000C')",
			'\u0007', "The alert (bell) character '\\a' ('\\u0007')",
			'\u001B', "The escape character '\\e' ('\\u001B')",
			' ', "The space ' ' ('\\u0020')",
	});
	
	private static final Map<Character, Character> map = Helper.createSimpleMap(new Object[]{
			'a', '\u0007',
			'e', '\u001B',
			'f', '\u000C',
			'n', '\n',
			'r', '\r',
			't', '\t',
			'v', '\u000B',
	});
	
	private final char symbol;
	
	public static final ItemFactory factory = new ItemFactory() {
		public RegexItem tryCreate(RegexParserState st) {
			Matcher m = charByCodePattern.matcher(st.getText());
	        if (m.find(st.getIndex() + 1) && m.start() == st.getIndex() + 1) {
	            return new CharByCode(st, m);
	        }
	        return null;
		}
	};

	private CharByCode(RegexParserState st, Matcher m) {
		super(st, 0);
		if (m.group(1) != null) {
			symbol = (char)Integer.parseInt(m.group(1), 8);
		} else if (m.group(2) != null) {
			symbol = (char)Integer.parseInt(m.group(2), 16);
		} else if (m.group(3) != null) {
			symbol = (char)Integer.parseInt(m.group(3), 16);
		} else if (m.group(4) != null) {
			symbol = map.get(m.group(4).charAt(0));
		} else {
			throw new InternalError();
		}
		length = m.group().length() + 1;
		
		hint = hintMap.get(symbol);
		if (hint == null) {
			if (!Character.isISOControl(symbol)) {
				hint = "The symbol '" + symbol + "' (\\u" + Helper.getCharHexCode(symbol) + ')';
			} else {
				hint = "The symbol '\\u" + Helper.getCharHexCode(symbol) + '\'';
			}
		}
		style = rc.getSymbolGroup();
	}

	public char getSymbol() {
		return symbol;
	}
	
}
