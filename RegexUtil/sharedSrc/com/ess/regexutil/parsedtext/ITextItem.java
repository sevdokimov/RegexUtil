package com.ess.regexutil.parsedtext;

public interface ITextItem {

	String getHint();

	void highlight(StyleData sd, int caret);

	int getIndex();

	int getLength();

	int getEnd();
	
	boolean hasCaret(int caret);
	
	boolean isError();
	
	ITextItem getNext();
	ITextItem getPred();

	boolean canAddGroup(int start, int end);
}