package com.ess.regexutil.swingcontrols;

import java.awt.BorderLayout;
import java.awt.Font;

import javax.swing.*;

import com.ess.regexutil.controls.RegexHighlighter;
import com.ess.regexutil.controls.SecondaryEditorHilighter;
import com.ess.regexutil.controls.SplitHilighter;
import com.ess.regexutil.swingadapter.SwingAdapter;

public class SplitPanel extends RegexResultPanel {

	private SwingAdapter text;
	private SwingAdapter result;
	
	private SecondaryEditorHilighter sh;
	
	private SplitHilighter splitH;
	
	public SplitPanel(RegexHighlighter highlighter) {
		super(highlighter);
		setLayout(new BorderLayout());
		
		text = new SwingAdapter(false);
		//text.setPlaceholder("Text for split");
		text.getEditor().setFont(new Font("MonoSpaced", Font.PLAIN, 12));
		result = new SwingAdapter(false);
		result.getEditor().setEditable(false);
		result.getEditor().setFont(new Font("MonoSpaced", Font.PLAIN, 12));
		
		JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true);
		split.setLeftComponent(new JScrollPane(text.getEditor()));
		split.setRightComponent(new JScrollPane(result.getEditor()));
		split.setResizeWeight(0.5);
		add(split, BorderLayout.CENTER);
		
		sh = new SecondaryEditorHilighter(highlighter, text, true);
		
		splitH = new SplitHilighter(text, result, highlighter, true);
	}

	public JTextPane getEditor() {
		return text.getEditor();
	}

	public JTextPane getResultEditor() {
		return result.getEditor();
	}

	@Override
	public void lostFocus() {
		sh.setDisable(true);
		splitH.setDisable(true);
	}

	@Override
	public void setFocus() {
		sh.setDisable(false);
		splitH.setDisable(false);
	}

	@Override
	public String getText() {
		return text.getText();
	}

	@Override
	public void setText(String s) {
		text.setText(s);
	}
	
}
