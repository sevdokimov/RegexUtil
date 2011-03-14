package com.ess.util;

import java.util.regex.Pattern;

import com.ess.regexutil.escaping.JavaRegexTransfer;
import com.ess.regexutil.escaping.JavaScriptTransfer;
import com.ess.regexutil.escaping.RegexTransfer;
import com.ess.regexutil.escaping.XmlRegexTransfer;

public class RegexConsts {
	
	public static final Object[][] flagsData = new Object[][]{
		{Pattern.CASE_INSENSITIVE, "Case-insensitive (?i)"},
		{Pattern.DOTALL, "Dot all mode (?s)"},
		{Pattern.UNIX_LINES, "Unix lines (?d)"},
		{Pattern.COMMENTS, "Comments (?x)"},
		{Pattern.MULTILINE, "Multiline (?m)"},
		{Pattern.LITERAL, "Literal"},
		{Pattern.UNICODE_CASE, "Unicode-case (?u)"},
		{Pattern.CANON_EQ, "Canonical"},
	};
	
	private RegexConsts() {
		
	}
}
