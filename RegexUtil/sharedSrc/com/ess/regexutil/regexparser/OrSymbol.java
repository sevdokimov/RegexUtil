package com.ess.regexutil.regexparser;

import com.ess.regexutil.parsedtext.ITextItem;
import com.ess.regexutil.parsedtext.StyleData;
import com.ess.regexutil.regexparser.RegexParser.RegexParserState;


public class OrSymbol extends RegexItem {

	private int rightPoint;
	private int leftPoint;
	
	private final String s;
	
	public OrSymbol(RegexParserState st) {
		super(st, 1);
		s = st.getText();
		hint = "U|V - Alternation: U or V\n\n" +
				"First tries to match subexpression U. Falls back and tries to match V if U didn't match.\n\nExamples:\n" +
				"- The expression \"A|B\" applied to text \"BA\" first matches \"B\", then \"A\".\n" +
				"- The expression \"AB|BC|CD\" applied to text \"ABC BC DAB\" matches, in sequence:\n" +
				"  \"AB\" in the first word, the second word \"BC\", \"AB\" at the very end.";
		
		style = rc.getComma();
	}

	
	
	@Override
	protected void highlightChild(StyleData sd) {
		rc.getChildElement().apply(sd, leftPoint, getIndex());
		rc.getChildElement().apply(sd, getEnd(), rightPoint);
	}

	@Override
	public void verify() {
		int rang = 0;
		for (ITextItem item = getPred(); item != null; item = item.getPred()) {
			if (item instanceof OpenBracket && --rang < 0) {
				leftPoint = item.getEnd();
				break;
			} else if (item instanceof CloseBracket) {
				rang++;
			}
		}

		rang = 0;
		for (ITextItem item = getNext(); item != null; item = item.getNext()) {
			if (item instanceof CloseBracket && --rang < 0) {
				rightPoint = item.getIndex();
				break;
			} else if (item instanceof OpenBracket) {
				rang++;
			}
			rightPoint = item.getEnd();
		}
		if (rightPoint == 0) {
			rightPoint = s.length();
		}
	}

	@Override
	public boolean canAddGroup(int start, int end) {
		return (end <= leftPoint
				|| start >= rightPoint 
				|| start >= leftPoint && end <= getIndex()
				|| start <= leftPoint && end >= rightPoint
				|| start >= leftPoint && end >= getIndex()
				|| start >= getEnd() && end <= rightPoint);
	}
	
	
	
}
