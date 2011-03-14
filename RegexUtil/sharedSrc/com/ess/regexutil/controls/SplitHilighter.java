package com.ess.regexutil.controls;

import java.util.Arrays;

import com.ess.regexutil.parsedtext.ITextEditorAdapter;
import com.ess.regexutil.parsedtext.ITextStyle;
import com.ess.regexutil.parsedtext.StyleData;
import com.ess.regexutil.regexparser.ParsedRegex;
import com.ess.regexutil.regexparser.RegexConfig;
import com.ess.util.EmptyArrays;
import com.ess.util.EventListener;

public class SplitHilighter {
	private final ITextEditorAdapter textEditor;
	private final ITextEditorAdapter resultEditor;
	private final RegexHighlighter highlighter;
	
	private String[] splittedText;
	
	private StyleData sd = new StyleData(100);
	
	private boolean isDisable;
	
	public SplitHilighter(ITextEditorAdapter textEditor, ITextEditorAdapter resultEditor, RegexHighlighter highlighter, boolean isDisable) {
		this.textEditor = textEditor;
		this.resultEditor = resultEditor;
		this.highlighter = highlighter;
		this.isDisable = isDisable;
		
		highlighter.regexChangeListeners.addListener(new EventListener<ParsedRegex>(){
			public void notify(ParsedRegex event) {
				if (event != null && !SplitHilighter.this.isDisable) {
					resplit();
				}
			}
		});
		textEditor.getListenersList().addListener(new EventListener<String>(){
			public void notify(String event) {
				if (event != null && !SplitHilighter.this.isDisable) {
					resplit();
				}
			}
		});
	}
	
	public boolean isDisable() {
		return isDisable;
	}

	public void setDisable(boolean isDisable) {
		if (this.isDisable == isDisable)
			return;
		this.isDisable = isDisable;
		if (isDisable)
			resplit();
	}

	private void resplit() {

		ParsedRegex parsedRegex = highlighter.getParsedRegex();
		String[] newSplittedText;
		if (parsedRegex.isError()) {
			newSplittedText = EmptyArrays.STRINGS;
		} else {
            try {
                newSplittedText = parsedRegex.getPattern().split(textEditor.getText());
            }
            catch (Throwable e) {
                String res = "Exception thrown: " + e;

                sd = sd.getInstance(res.length());
                RegexConfig.getInstance().getError().apply(sd, 0, res.length());

                resultEditor.setText(res);
                resultEditor.paint(sd);
                
                return;
            }
        }
		
		if (Arrays.equals(splittedText, newSplittedText))
			return;
		
		int numberLength = String.valueOf(newSplittedText.length - 1).length();
		
		splittedText = newSplittedText;
		
		sd = sd.getInstance(textEditor.getText().length() * (5 + numberLength + 2 + 1));
		
		ITextStyle defaultStyle = RegexConfig.getInstance().getSplitResultDefaultStyle();
		ITextStyle partNumberStyle = RegexConfig.getInstance().getSplitPartNumberStyle();
		ITextStyle resultStyle = RegexConfig.getInstance().getSplitResultStyle();
		
		StringBuilder res = new StringBuilder();
		int iLength = 1;
		int iLimit = 10;
		for (int i = 0; i < newSplittedText.length; i++) {
			int partStart = res.length(); 
			res.append("part ");
			
			if (i == iLimit) {
				iLimit *= 10;
				iLength++;
			}
			
			for (int k = iLength; k < numberLength; k++)
				res.append(' ');
			res.append(i);
			res.append(": ");
			int textStart = res.length();
			res.append(newSplittedText[i]);
			res.append("\r\n");
			
			defaultStyle.apply(sd, partStart, res.length());
			partNumberStyle.apply(sd, partStart, textStart);
			resultStyle.apply(sd, textStart, res.length());
		}
		resultEditor.setText(res.toString());
		resultEditor.paint(sd);
	}
}
