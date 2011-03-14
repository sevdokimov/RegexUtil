package com.ess.regexutil.parsedtext;

import com.ess.util.IListenersList;

/**
 * Parsers and highlighters should be ably to work on Swing and SWT components.
 * Implementations of this class should support Swing and SWT platforms.
 * @author Sergey Evdokimov
 */
public interface ITextEditorAdapter {
	
	IListenersList<String> getListenersList();
	
	void setText(String text);
	String getText();
	int getCaret();
	
	int getStartSelection();
	int getEndSelection();
	
	ITextStyle getSelectionStyle();
	
	void paint(StyleData style);
	
	void setTooltipResolver(TooltipResolver tooltipResolver);
	
	public interface TooltipResolver {
		String getTooltip(int pos);
	}
}
