package com.ess.regexutil.swingcontrols;

import java.awt.BorderLayout;
import java.awt.Font;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import com.ess.regexutil.controls.RegexHighlighter;
import com.ess.regexutil.controls.ReplaceHilighter;
import com.ess.regexutil.controls.SecondaryEditorHilighter;
import com.ess.regexutil.swingadapter.SwingAdapter;

public class ReplacePanel extends RegexResultPanel {

	private SwingAdapter replacement;
	private SwingAdapter text;
	private SwingAdapter result;
	
	private SecondaryEditorHilighter sh;
	private ReplaceHilighter rh;
	
	public ReplacePanel(RegexHighlighter highlighter) {
		super(highlighter);
		setLayout(new BorderLayout());
		
		JPanel replacementPanel = new JPanel(new BorderLayout());
		replacementPanel.setBorder(BorderFactory.createEmptyBorder(3,3,3,3));
		replacementPanel.add(new JLabel("Replacement:"), BorderLayout.LINE_START);
		replacement = new SwingAdapter(true);
		replacement.getEditor().setFont(new Font("MonoSpaced", Font.PLAIN, 13));
		replacementPanel.add(new JScrollPane(replacement.getEditor(),JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);
		add(replacementPanel, BorderLayout.PAGE_START);
		
		text = new SwingAdapter(false);
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
		rh = new ReplaceHilighter(text, result, highlighter, replacement, true);
	}
	
	@Override
	public void lostFocus() {
		sh.setDisable(true);
		rh.setDisable(true);
	}

	@Override
	public void setFocus() {
		sh.setDisable(false);
		rh.setDisable(false);
	}

	@Override
	public String getText() {
		return text.getText();
	}

	@Override
	public void setText(String s) {
		text.setText(s);
	}

    public void saveState(Map<String, String> res) {
        super.saveState(res);
        res.put("replacement", replacement.getText());
    }

    public void restoreState(Map<String, String> state) {
        super.restoreState(state);
        String replacement = state.get("replacement");
        if (replacement == null)
            replacement = "";
        this.replacement.setText(replacement);
    }
}
