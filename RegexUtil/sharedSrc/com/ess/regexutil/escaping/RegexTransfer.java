package com.ess.regexutil.escaping;

public interface RegexTransfer {
	
	String sourceToRegex(String source);
	
	String regexToSource(String regex);

}
