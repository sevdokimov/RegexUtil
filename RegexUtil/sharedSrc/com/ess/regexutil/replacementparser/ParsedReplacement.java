package com.ess.regexutil.replacementparser;

import java.util.List;

import com.ess.regexutil.controls.RegexHighlighter;
import com.ess.regexutil.parsedtext.ITextItem;
import com.ess.regexutil.parsedtext.ParsedText;

public class ParsedReplacement extends ParsedText {
	
	private final String replacement; 
	
	public ParsedReplacement(RegexHighlighter highlighter, String replacement) {
		List<ITextItem> list = ReplacementParser.getInstance().parse(highlighter, replacement);
		setList(list);
		this.replacement = replacement;
	}

	public String getReplacement() {
		return replacement;
	}
	
}
