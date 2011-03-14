package com.ess.regexutil.escaping;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JavaScriptTransfer implements RegexTransfer {

	public static final JavaScriptTransfer instance = new JavaScriptTransfer();
	
	private JavaScriptTransfer() {
	
	}
	
	public String regexToSource(String regex) {
		return regex.replaceAll("(^|[^\\\\])/", "$1\\\\/");
	}

	public String sourceToRegex(String source) {
		Matcher m = Pattern.compile("/(.*[^\\\\])/", Pattern.DOTALL).matcher(source);
		if (m.matches()) {
			return m.group(1);
		} else {
			return source;
		}
	}

}
