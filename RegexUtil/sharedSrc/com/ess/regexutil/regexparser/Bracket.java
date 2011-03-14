package com.ess.regexutil.regexparser;

import com.ess.regexutil.parsedtext.ITextItem;
import com.ess.regexutil.parsedtext.StyleData;
import com.ess.regexutil.regexparser.RegexParser.RegexParserState;


public abstract class Bracket extends RegexItem {

	protected Bracket otherBracket;
	
	protected final char type;
	protected final boolean isOpen;
	
	protected Bracket(RegexParserState st, int length, char type, boolean isOpen) {
		super(st, length);
		this.type = type;
		this.isOpen = isOpen;
		
		isError = true;
		hint = getErrorHint();
	}

	public String getErrorHint() {
		return Messages.get("Bracket.PairedBracketNotFound"); //$NON-NLS-1$
	}
	
	@Override
	protected void highlightInternal(StyleData sd, int caret) {
		paint(sd, rc.getComma());
		if (otherBracket != null) {
			if (hasCaret(caret)) {
				paint(sd, rc.getCurrentBracket());
				otherBracket.paint(sd, rc.getCurrentBracket());
			}
		}
	}

	public boolean isOpen() {
		return isOpen;
	}

	public char getType() {
		return type;
	}

	public Bracket getOtherBracket() {
		return otherBracket;
	}

	protected void setOtherBracket(Bracket otherBracket) {
		this.otherBracket = otherBracket;
		isError = false;
		hint = null;
	}

	@Override
	public boolean canAddGroup(int start, int end) {
		return (!isOpen() ||
				end <= getIndex()
				|| start >= otherBracket.getEnd() 
				|| start >= getEnd() && end <= otherBracket.getIndex()
				|| start <= getIndex() && end >= otherBracket.getEnd());
	}

	@Override
	public void verify() {
		if (isOpen())
			return;
		
		for (ITextItem item = getPred(); item != null; item = item.getPred()) {
			if (item instanceof Bracket) {
				Bracket br = (Bracket) item;
				if (br.isOpen()) {
					if (br.getType() == getType() && br.getOtherBracket() == null) {
						setOtherBracket(br);
						br.setOtherBracket(this);
					}
					return;
				} else {
					if (br.isError())
						return;
					else
						item = br.getOtherBracket();
				}
			}
		}
	}

}
