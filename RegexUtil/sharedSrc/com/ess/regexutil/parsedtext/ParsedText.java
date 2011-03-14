package com.ess.regexutil.parsedtext;

import java.util.Collections;
import java.util.List;

@SuppressWarnings("unchecked")
public class ParsedText {
	
	private List<ITextItem> list;
	private List<ITextItem> unmList;
	
	protected boolean isError;
	
	protected void setList(List list) {
		this.list = list;
		unmList = Collections.unmodifiableList(list);
		
		isError = false;
        for (int i = 0; i < list.size(); i++) {
        	if (((ITextItem)list.get(i)).isError()) {
        		isError = true;
        		break;
        	}
        }
	}
	
	public boolean isError() {
		return isError;
	}

	public void paint(StyleData sd, int caret) {
		for (ITextItem item : list) {
			item.highlight(sd, caret);
		}
	}
	
	public ITextItem getItemAtCaretLocation(int caret) {
		for (ITextItem item : list) {
			if (item.hasCaret(caret))
				return item;
		}
		return null;
	}
	
	public ITextItem getItem(int index) {
		if (index == -1)
			return null;
		for (ITextItem item : list) {
			if (index < item.getLength())
				return item;
			index -= item.getLength();
		}
		return null;
	}
	
	public List<ITextItem> getList() {
		return unmList;
	}
	
	public boolean canAddGroup(int start, int end) {
		for (ITextItem item : list) {
			if (!item.canAddGroup(start, end)) {
				return false;
			}
		}
		return true;
	}
}
