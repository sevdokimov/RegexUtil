package com.ess.regexutil.regexparser;

import com.ess.regexutil.regexparser.RegexParser.RegexParserState;

public class SqrOpenBracket extends Bracket {

	private final boolean not;
	
	public SqrOpenBracket(RegexParserState st) {
		super(st, 1, '[', true);
		if (st.get(1)  == '^') {
			length = 2;
			not = true;
		} else {
			not = false;
		}
	}

	@Override
	protected void setOtherBracket(Bracket otherBracket) {
		super.setOtherBracket(otherBracket);
		hint = not ?
				"Excluded character set\n\n" +
				"Matches a single character that is not one of the excluded characters.\n\nExamples:\n" +
				"The expression \"[^ecl]\" matches \"o\" and \"d\" in text \"cold\".\n" +
				"The expression \"[a-z&&[^ecl]]\" matches any character from a to z, excluding e, c, and l."
				:
				"Character set\n\n" +
				"Matches a single character out of the set.\n\nExample:\n" +
				"The expression \"[ecl]\" matches \"c\" and \"l\" in text \"cold\".\n" +
				"The expression \"[a-z&&[^ecl]]\" matches any character from a to z, excluding e, c, and l.";
	}



	@Override
	public boolean canAddGroup(int start, int end) {
		return !isError() && (
					end <= getIndex()
					|| start >= otherBracket.getEnd()
					|| start <= getIndex() && end >= otherBracket.getEnd()
				);
	}

}
