package com.ess.regexutil.regexparser;

import java.util.Map;

import com.ess.regexutil.parsedtext.ITextItem;
import com.ess.regexutil.parsedtext.StyleData;
import com.ess.regexutil.regexparser.RegexParser.RegexParserState;
import com.ess.util.Helper;

public class Multeplexor extends RegexItem {

	private final char additionSymbol; 
	private final char symbol; 
	
	private int hilightStart;
	
	private static final String[] HINT_MAP = new String[] {
		"? - Greedy match 0 or 1 times",
		"* - Greedy match 0 or more times",
		"+ - Greedy match 1 or more times",

		"?? - Lazy match 0 or 1 times",
		"*? - Lazy match 0 or more times",
		"+? - Lazy match 1 or more times",

		"?+ - Possessive match 0 or 1 times (no backtracking)",
		"*+ Possessive match 0 or more times (no backtracking)",
		"++ - Possessive match 1 or more times (no backtracking)",
	};

	public Multeplexor(RegexParserState st) {
		super(st, 1);
		symbol = st.get();
		char a = st.get(1);
		if (a == '+' || a == '?') {
			additionSymbol = a;
			length = 2;
		} else {
			additionSymbol = 0;
		}
		style = rc.getComma();
	}

	public static int getPrevIndex(ITextItem pred) {
		if (pred instanceof Bracket) {
			ITextItem other = ((Bracket)pred).getOtherBracket();
			return other == null ? pred.getIndex() : other.getIndex();
		} else {
			return pred.getIndex();
		}
	}
	
	@Override
	public void verify() {
		if (testByApplicable(getPred())) {
			hilightStart = getPrevIndex(getPred());
			hint = HINT_MAP[(additionSymbol == '\u0000' ? 0 : additionSymbol == '?' ? 1 : 2) * 3
			                + symbol == '?' ? 0 : symbol == '*' ? 1 : 2];
		} else {
			isError = true;
		}
	}

	
	
	@Override
	protected void highlightChild(StyleData sd) {
		rc.getChildElement().apply(sd, hilightStart, getIndex());
	}

	@Override
	public boolean canAddGroup(int start, int end) {
		return (end <= hilightStart
				|| start >= getEnd() 
				|| start >= hilightStart && end <= getIndex()
				|| start <= hilightStart && end >= getEnd());
	}

	public static boolean testByApplicable(ITextItem pred) {
		if (pred == null)
			return false;
		if (pred instanceof CloseBracket) {
			OpenBracket openBracket = (OpenBracket) ((CloseBracket)pred).getOtherBracket();
			return openBracket == null || !openBracket.isFlagWithoutBracket();
		}
		return canBeAfterMap.get(pred.getClass()).booleanValue();
	}
	
	public static final Map<Class<ITextItem>, Boolean> canBeAfterMap = Helper.createUnmMap(new Object[]{
			BraceMulteplexors.class, Boolean.FALSE,
			CharByCode.class, Boolean.TRUE,
			ControlCor.class, Boolean.TRUE,
			Dote.class, Boolean.TRUE,
			ErrorItem.class, Boolean.TRUE,
			EscapeComment.class, Boolean.TRUE,
			LiteralItem.class, Boolean.TRUE,
			EscapedChar.class, Boolean.TRUE,
			EscapeP.class, Boolean.TRUE,
			GroupNumber.class, Boolean.TRUE,
			Multeplexor.class, Boolean.FALSE,
			OpenBracket.class, Boolean.FALSE,
			OrSymbol.class, Boolean.FALSE,
			SimpleGroupItem.class, Boolean.TRUE,
			SimpleSymbol.class, Boolean.TRUE,
			SpecEscape.class, Boolean.FALSE,
			SpecSymbol.class, Boolean.FALSE,
			SqrCloseBracket.class, Boolean.TRUE,
	});
	
}
