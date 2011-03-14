package com.ess.regexutil.regexparser;

import com.ess.regexutil.parsedtext.StyleData;
import com.ess.regexutil.regexparser.RegexParser.RegexParserState;


public class EscapeComment extends RegexItem {

	private final boolean endToE;
	
	public EscapeComment(RegexParserState st, int length, boolean endToE) {
		super(st, length);
		this.endToE = endToE;
		hint = "All characters between \\Q and the next \\E are taken literally and are not interpreted.\n\nExample:\n" +
				"The expression \"\\Qnew int[] {42}\\E;\" matches text \"new int[] {42}\".";
	}

	public static final ItemFactory factory = new ItemFactory() {
		public RegexItem tryCreate(RegexParserState st) {
			if (st.get(1) != 'Q')
				return null;
			int end = st.getText().indexOf("\\E", st.getIndex() + 2);
			if (end == -1) {
				return new EscapeComment(st, st.getText().length() - st.getIndex() - 1, false);
			}
			return new EscapeComment(st, end + 2 - st.getIndex(), true);
		}
	};

	@Override
	protected void highlightInternal(StyleData sd, int caret) {
		rc.getComma().apply(sd, getIndex(), getIndex() + 2);
		
		int endText = getEnd();
		if (endToE) {
			int end = getEnd();
			rc.getComma().apply(sd, end - 2, end);
		}
		paint(sd, rc.getEscapeComment());
	}

}
