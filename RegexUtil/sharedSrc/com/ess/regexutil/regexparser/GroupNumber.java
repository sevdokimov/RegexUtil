package com.ess.regexutil.regexparser;

import com.ess.regexutil.parsedtext.ITextItem;
import com.ess.regexutil.parsedtext.StyleData;
import com.ess.regexutil.regexparser.RegexParser.RegexParserState;

public class GroupNumber extends RegexItem {

	private int groupNumber;
	
	private OpenBracket bracket;
	
	public GroupNumber(RegexParserState st, int length, int groupNumber) {
		super(st, length);
		this.groupNumber = groupNumber;
	}

	@Override
	protected void highlightChild(StyleData sd) {
		if (bracket != null) {
			rc.getChildElement().apply(sd, bracket.getIndex(), bracket.getOtherBracket().getEnd());
		}
	}

	@Override
	public void verify() {
		for (ITextItem item = getPred(); item != null; item = item.getPred()) {
			if (item instanceof OpenBracket) {
				OpenBracket bracket = (OpenBracket) item;
				if (bracket.getGroupNumber() != -1 && !bracket.isError()) {
					if (bracket.getGroupNumber() == groupNumber) {
						this.bracket = bracket;
						hint = "\\i - Match of the capturing group i";
						style = rc.getNumber();
						return;
					}
				}
			}
		}
		hint = "There are no group #" + groupNumber;
		style = rc.getNotUsage();
	}

	public int getGroupNumber() {
		return groupNumber;
	}
	
}
