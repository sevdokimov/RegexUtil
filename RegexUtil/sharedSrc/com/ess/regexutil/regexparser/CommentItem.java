package com.ess.regexutil.regexparser;

import com.ess.regexutil.regexparser.RegexParser.RegexParserState;


public class CommentItem extends RegexItem {

	public CommentItem(RegexParserState st) {
		super(st, st.getText().length() - 1 - st.getIndex());
		hint = "The comment";
		style = rc.getComments();
	}

	@Override
	public boolean canAddGroup(int start, int end) {
		return end <= getIndex();
	}
}
