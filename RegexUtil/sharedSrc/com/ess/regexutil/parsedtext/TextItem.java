package com.ess.regexutil.parsedtext;


public class TextItem implements ITextItem {
	
	private int index;
	protected int length;
	
	protected ITextItem next;
	protected ITextItem pred;
	
	protected String hint;
	
	protected boolean isError;
	
	public TextItem(int index, int length) {
		this.index = index;
		this.length = length;
	}

	public boolean isError() {
		return isError;
	}

	public String getHint() {
		return hint;
	}
	
	public void highlight(StyleData sd, int caret) {
	}

	protected final void paint(StyleData sd, ITextStyle style) {
		style.apply(sd, getIndex(), getEnd());
	}
	
	public int getIndex() {
		return index;
	}
	
	public int getLength() {
		return length;
	}
	
	public int getEnd() {
		return getIndex() + getLength();
	}

	public final boolean hasCaret(int caret) {
		return caret > getIndex() && caret <= getEnd();
	}
	
	public ITextItem getNext() {
		return next;
	}

	public void setNext(ITextItem next) {
		assert this.next == null;
		this.next = next;
	}

	public ITextItem getPred() {
		return pred;
	}

	public void setPred(ITextItem pred) {
		assert this.pred == null;
		this.pred = pred;
	}

	public void verify() {
	}
	
	public boolean canAddGroup(int start, int end) {
		return true;
	}
	
}
