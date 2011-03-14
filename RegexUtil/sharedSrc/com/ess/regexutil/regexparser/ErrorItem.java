package com.ess.regexutil.regexparser;

import com.ess.regexutil.regexparser.RegexParser.RegexParserState;


public class ErrorItem extends RegexItem {

	public ErrorItem(RegexParserState st, int length, String hint) {
		this(st.getIndex(), length, hint);
	}
	
	public ErrorItem(int i, int length, String hint) {
		super(i, length);
		this.hint = hint;
		isError = true;
	}

}
