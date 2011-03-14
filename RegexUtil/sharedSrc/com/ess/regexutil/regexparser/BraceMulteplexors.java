package com.ess.regexutil.regexparser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ess.regexutil.parsedtext.ITextStyle;
import com.ess.regexutil.parsedtext.StyleData;
import com.ess.regexutil.parsedtext.TextItem;
import com.ess.regexutil.regexparser.RegexParser.RegexParserState;


public class BraceMulteplexors extends RegexItem {

	private static final Pattern inBraces = Pattern.compile("\\G(?:(?:(\\d+)(,(\\d*)?)?)|([^{}()\\[\\]#}]*))\\}([\\+\\?])?"); //$NON-NLS-1$
	
	private static final String[] HINT_MAP = new String[]{
			Messages.get("BraceMulteplexors.1"), //$NON-NLS-1$
			Messages.get("BraceMulteplexors.2"), //$NON-NLS-1$
			Messages.get("BraceMulteplexors.3"), //$NON-NLS-1$
					
			Messages.get("BraceMulteplexors.4"), //$NON-NLS-1$
			Messages.get("BraceMulteplexors.5"), //$NON-NLS-1$
			Messages.get("BraceMulteplexors.6"), //$NON-NLS-1$
							
			Messages.get("BraceMulteplexors.7"), //$NON-NLS-1$
			Messages.get("BraceMulteplexors.8"), //$NON-NLS-1$
			Messages.get("BraceMulteplexors.9"), //$NON-NLS-1$
	};
	
	private enum TYPE {
		EQUALS, FROM, FROM_TO;
	}
	
	private static int compare(String s1, String s2) {
		if (s1.length() == s2.length()) {
			return s1.compareTo(s2);
		}
		return s1.length() - s2.length();
	}
	
	public static final ItemFactory factory = new ItemFactory() {
		public TextItem tryCreate(RegexParserState st) {
			Matcher m = inBraces.matcher(st.getText());
			int i = st.getIndex();
			if (!m.find(i + 1)) {
				return new ErrorItem(i, 1, Messages.get("BraceMulteplexors.10")); //$NON-NLS-1$
			}
			int length = 1 + m.group().length();
			if (m.group(4) != null) {
				return new ErrorItem(i, length, Messages.getF("BraceMulteplexors.Is_not_legal_argument", m.group(4))); //$NON-NLS-1$
			}
			
			String sFrom = m.group(1);
			String sTo = m.group(3);
			
			if (sTo != null && sTo.length() > 0 && compare(sFrom, sTo) > 0 ) {
				return new ErrorItem(i, length, Messages.getF("BraceMulteplexors.first_big", sFrom, sTo)); //$NON-NLS-1$
			}
			
			return new BraceMulteplexors(st, length,
					sTo == null ? TYPE.EQUALS : sTo.length() == 0 ? TYPE.FROM : TYPE.FROM_TO,
					sFrom, 
					sTo, 
					m.group(5) == null ? 0 : m.group(5).charAt(0));
		}
	};

	private final TYPE type;
	private final String s1, s2;
	
	// 0, '+' or '?'
	private final char additionSymbol;
	
	private int hilightStart;
	
	public BraceMulteplexors(RegexParserState st,  int length, TYPE type, String s1, String s2, char additionSymbol) {
		super(st, length);
		this.type = type;
		this.s1 = s1;
		this.s2 = s2;
		this.additionSymbol = additionSymbol;
		
		hint = HINT_MAP[ (additionSymbol == '\u0000' ? 0 : additionSymbol == '?' ? 1 : 2 ) * 3
		                 + (type == TYPE.EQUALS ? 0 : type == TYPE.FROM ? 1 : 2)];
	}

	@Override
	protected void highlightChild(StyleData sd) {
		rc.getChildElement().apply(sd, hilightStart, getIndex());
	}



	@Override
	protected void highlightInternal(StyleData sd, int caret) {
		int i = getIndex();
		
		ITextStyle c = rc.getComma();
		c.apply(sd, i, i + 1);
		i++;

		rc.getNumber().apply(sd, i, i + s1.length());
		i += s1.length();
		
		if (type != TYPE.EQUALS) {
			c.apply(sd, i, i + 1);
			i++;
			if (type == TYPE.FROM_TO) {
				rc.getNumber().apply(sd, i, i + s2.length());
				i += s2.length();
			}
		}
		c.apply(sd, i, i + 1);
		
		if (additionSymbol != 0) {
			c.apply(sd, i, i + 1);
			i++;
		}
	}

	@Override
	public boolean canAddGroup(int start, int end) {
		return (end <= hilightStart
				|| start >= getEnd() 
				|| start >= hilightStart && end <= getIndex()
				|| start <= hilightStart && end >= getEnd());
	}

	@Override
	public void verify() {
		if (Multeplexor.testByApplicable(getPred())) {
			hilightStart = Multeplexor.getPrevIndex(getPred());
		} else {
			hilightStart = getIndex();
		}
	}

}
