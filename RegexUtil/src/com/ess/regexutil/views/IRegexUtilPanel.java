package com.ess.regexutil.views;

import org.eclipse.swt.widgets.Composite;

import com.ess.regexutil.parsedtext.ITextEditorAdapter;

public interface IRegexUtilPanel {

	RegexEditor getRegexEditor();
	ITextEditorAdapter getTextEditor();
	
	Composite getPanel();
	
}
