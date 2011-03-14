package com.ess.regexutil.escaping;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JavaRegexTransfer implements RegexTransfer {

	public static final JavaRegexTransfer instance = new JavaRegexTransfer();
	
	private final String STRING_LITERAL_REGEX = "([^\n\r\"\\\\])|\\\\(?:([btnfr\"'\\\\])|([0-3]?[0-7]{1,2})|u(\\p{XDigit}{4}))";
	private final Pattern CHAR_IN_STRING = Pattern.compile(STRING_LITERAL_REGEX + "|.");
	private final Pattern STRING = Pattern.compile("\\s*(?:=\\s*)?\"((?:" + STRING_LITERAL_REGEX + ")*)\"\\s*(;\\s*)?");

	private final Pattern REGEX_TO_JAVA = Pattern.compile("\\\\[tnrf]|."); 
	
	private JavaRegexTransfer() {
		
	}
	
	public String regexToSource(String regex) {
		Matcher m = REGEX_TO_JAVA.matcher(regex);
		StringBuilder res = new StringBuilder();
		while (m.find()) {
			if (m.group().length() > 1) {
				res.append(m.group());
			} else {
				char a = m.group().charAt(0);
				if (a == '"' || a == '\\')
					res.append('\\');
				res.append(a);
			}
		}
		return res.toString();
	}

	public String sourceToRegex(String source) {
		Matcher m = STRING.matcher(source);
		if (m.matches()) {
			source = m.group(1);
		}
		m = CHAR_IN_STRING.matcher(source);
		StringBuilder res = new StringBuilder();
		while (m.find()) {
			if (m.group(2) != null) {
				char a = m.group(2).charAt(0);
				if (a == 'b') {
					res.append("\\010");
				} else if (a == 't' || a == 'n' || a == 'r' || a == 'f') {
					res.append('\\').append(a);
				} else {
					res.append(a);
				}
			} else if (m.group(3) != null) {
				String str = m.group(3);
				char a = (char)Integer.parseInt(str, 8);
				if (Character.isISOControl(a)) {
					res.append("\\0").append(str);
				} else {
					res.append(a);
				}
			} else if (m.group(4) != null) {
				String str = m.group(4);
				char a = (char)Integer.parseInt(str, 16);
				if (Character.isISOControl(a) || a > 0x009F) {
					res.append(m.group());
				} else {
					res.append(a);
				}
			} else {
				res.append(m.group());
			}
		}
		return res.toString();
	}
	
	
}
