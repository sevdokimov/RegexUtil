package com.ess.regexutil.controls;

import java.awt.Point;
import java.util.ArrayList;
import java.util.regex.Matcher;

import com.ess.regexutil.parsedtext.ITextEditorAdapter;
import com.ess.regexutil.parsedtext.ITextStyle;
import com.ess.regexutil.parsedtext.StyleData;
import com.ess.regexutil.regexparser.ParsedRegex;
import com.ess.regexutil.regexparser.RegexConfig;
import com.ess.regexutil.replacementparser.ParsedReplacement;
import com.ess.util.EventListener;

public class ReplaceHilighter {
	
	private final ITextEditorAdapter textEditor;
	private final ITextEditorAdapter replacementEditor;
	private final ITextEditorAdapter resultEditor;
	private final RegexHighlighter highlighter;
	
	private ParsedReplacement parsedReplacement;
	
	private ArrayList<Point> bounds = new ArrayList<Point>();
	
	private StyleData sd = new StyleData(100);
	
	private boolean isDisable;
	
	public ReplaceHilighter(ITextEditorAdapter textEditor, ITextEditorAdapter resultEditor, RegexHighlighter highlighter, ITextEditorAdapter replacementEditor, boolean isDisable) {
		this.textEditor = textEditor;
		this.resultEditor = resultEditor;
		this.highlighter = highlighter;
		this.replacementEditor = replacementEditor;
		this.isDisable = isDisable;
		
		reparceReplacement();
		
		highlighter.regexChangeListeners.addListener(new EventListener<ParsedRegex>(){
			public void notify(ParsedRegex event) {
				if (ReplaceHilighter.this.isDisable)
					return;
				if (event != null) {
					reparceReplacement();
					rereplace();
				}
			}
		});
		textEditor.getListenersList().addListener(new EventListener<String>(){
			public void notify(String event) {
				if (ReplaceHilighter.this.isDisable)
					return;

				if (event != null) {
					rereplace();
				}
			}
		});
		replacementEditor.getListenersList().addListener(new EventListener<String>(){
			public void notify(String event) {
				if (ReplaceHilighter.this.isDisable)
					return;

				if (event != null) {
					reparceReplacement();
					rereplace();
				} else {
					repaintReplacement();
				}
			}
		});
	}
	
	public boolean isDisable() {
		return isDisable;
	}

	public void setDisable(boolean isDisable) {
		if (this.isDisable != isDisable) {
			this.isDisable = isDisable;
			if (!isDisable) {
				reparceReplacement();
				rereplace();
			}
		}
	}

	private void reparceReplacement() {
		parsedReplacement = new ParsedReplacement(highlighter, replacementEditor.getText());
		repaintReplacement();
	}
	
	private void repaintReplacement() {
		String replacement = replacementEditor.getText();
		sd = sd.getInstance(replacement.length());
		RegexConfig.getInstance().getDefaultStyle().apply(sd, 0, replacement.length());
		parsedReplacement.paint(sd, replacementEditor.getCaret());
		replacementEditor.paint(sd);
	}
	
	private void rereplace() {
		
		if (parsedReplacement.isError() || highlighter.getParsedRegex().isError()) {
			resultEditor.setText("");
			return;
		}
		
		String replacement = replacementEditor.getText();

        try {
            Matcher m = highlighter.getParsedRegex().getPattern().matcher(textEditor.getText());

            StringBuffer res = new StringBuffer();

            int lastGroupEnd = 0;
            while (m.find()) {
                int bufferEnd = res.length();
                m.appendReplacement(res, replacement);
                bounds.add(new Point(bufferEnd + (m.start() - lastGroupEnd), res.length()));
                lastGroupEnd = m.end();
            }
            m.appendTail(res);

            sd = sd.getInstance(res.length());
            RegexConfig.getInstance().getDefaultStyle().apply(sd, 0, res.length());

            ITextStyle replacedStyle = RegexConfig.getInstance().getReplaced();
            for (Point p : bounds) {
                replacedStyle.apply(sd, p.x, p.y);
            }
            bounds.clear();

            resultEditor.setText(res.toString());
            resultEditor.paint(sd);
        }
        catch (Throwable e) {
            String res = "Exception thrown: " + e;

            sd = sd.getInstance(res.length());
            RegexConfig.getInstance().getError().apply(sd, 0, res.length());

            resultEditor.setText(res);
            resultEditor.paint(sd);
        }
    }
}
