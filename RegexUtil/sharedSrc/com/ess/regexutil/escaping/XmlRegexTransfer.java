package com.ess.regexutil.escaping;

import java.util.Map;
import java.util.Map.Entry;

import com.ess.util.Helper;

public class XmlRegexTransfer implements RegexTransfer {

	public static final XmlRegexTransfer instance = new XmlRegexTransfer();
	
	private Map<Character, String> replace = Helper.createSimpleMap(new Object[]{
			'&', "amp;",
			'<', "lt;",
			'>', "gt;",
			'\'', "apos;",
			'"', "quot;",
	});
	
	private XmlRegexTransfer() {
		
	}
	
	public String regexToSource(String regex) {
		StringBuilder res = new StringBuilder();
		for (int i = 0; i < regex.length(); i++) {
			char a = regex.charAt(i);
			String s = replace.get(a);
			if (s != null) {
				res.append('&').append(s);
			} else {
				res.append(a);
			}
		}
		return res.length() == regex.length() ? regex : res.toString();
	}

	public String sourceToRegex(String source) {
		StringBuilder res = new StringBuilder(source.length());
		for (int i = 0; i < source.length(); i++) {
			char a = source.charAt(i);
			if (a == '&') {
				for (Entry<Character, String> e : replace.entrySet()) {
					if (source.startsWith(e.getValue(), i+1)) {
						res.append(e.getKey());
						i += e.getValue().length();
						a = 0;
						break;
					}
				}
				if (a != 0)
					res.append(a);
			} else {
				res.append(a);
			}
		}
		return res.length() == source.length() ? source : res.toString();
	}

}
