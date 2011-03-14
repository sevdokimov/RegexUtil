package com.ess.regexutil.regexparser;

import com.ess.regexutil.parsedtext.TextItem;

public class LiteralItem extends TextItem {

	public LiteralItem(String text) {
		super(0, text.length());
		hint = "The Literal flag is specified so the pattern is treated as a sequence of literal characters.";
	}

}
