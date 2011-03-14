package com.ess.regexutil.swingcontrols;

import javax.swing.JPanel;

import com.ess.regexutil.controls.RegexHighlighter;

import java.util.Map;

public abstract class RegexResultPanel extends JPanel {
	
	protected final RegexHighlighter highlighter;
	
	public RegexResultPanel(RegexHighlighter highlighter) {
		this.highlighter = highlighter;
	}

	public void setFocus() {
		
	}
	
	public void lostFocus() {
		
	}
	
	public abstract String getText();
	public abstract void setText(String s);

    public void saveState(Map<String, String> res) {
        res.put("text", getText());
    }

    public void restoreState(Map<String, String> state) {
        String text = state.get("text");
        if (text == null)
            text = "";
        setText(text);
    }

}
