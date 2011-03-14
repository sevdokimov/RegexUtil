package com.ess.regexutil.swingcontrols;

import java.awt.BorderLayout;
import java.awt.Font;

import javax.swing.JScrollPane;

import com.ess.regexutil.controls.RegexHighlighter;
import com.ess.regexutil.controls.SecondaryEditorHilighter;
import com.ess.regexutil.swingadapter.SwingAdapter;

public class FindAndMatchPanel extends RegexResultPanel {

	private SecondaryEditorHilighter sh;
	
	private SwingAdapter textAdapter;
	
	public FindAndMatchPanel(RegexHighlighter highlighter, boolean match) {
		super(highlighter);
		
		setLayout(new BorderLayout());
		textAdapter = new SwingAdapter(false);
		//textAdapter.setPlaceholder("Text");
		textAdapter.getEditor().setFont(new Font("MonoSpaced", Font.PLAIN, 12));
		add(new JScrollPane(textAdapter.getEditor()), BorderLayout.CENTER);
		
		sh = new SecondaryEditorHilighter(highlighter, textAdapter, true);
		sh.setMatchMode(match);
	}

	@Override
	public void lostFocus() {
		sh.setDisable(true);
	}

	@Override
	public void setFocus() {
		sh.setDisable(false);
	}

	@Override
	public String getText() {
		return textAdapter.getText();
	}

	@Override
	public void setText(String s) {
		textAdapter.setText(s);
	}

}
