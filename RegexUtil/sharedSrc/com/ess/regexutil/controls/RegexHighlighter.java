package com.ess.regexutil.controls;

import com.ess.regexutil.parsedtext.ITextEditorAdapter;
import com.ess.regexutil.parsedtext.ITextItem;
import com.ess.regexutil.parsedtext.StyleData;
import com.ess.regexutil.parsedtext.ITextEditorAdapter.TooltipResolver;
import com.ess.regexutil.regexparser.Flags;
import com.ess.regexutil.regexparser.ParsedRegex;
import com.ess.regexutil.regexparser.RegexConfig;
import com.ess.util.EventListener;
import com.ess.util.ListenersList;


@SuppressWarnings("unchecked")
public class RegexHighlighter {
	
	public final ListenersList<ParsedRegex> regexChangeListeners = new ListenersList<ParsedRegex>();
	
	private ParsedRegex parsedRegex;
	
	private ITextEditorAdapter editor;
	private Flags flags;
	
	private StyleData sd = new StyleData(100);
	
	public RegexHighlighter(ITextEditorAdapter editor, Flags flags) {
		this.editor = editor;
		this.flags = flags;
		flags.changeListeners.addListener(new EventListener<Integer>(){
			public void notify(Integer event) {
				reparsing();
			}
		});
		
		editor.getListenersList().addListener(new EventListener<String>() {
			public void notify(String event) {
				if (event != null) {
					reparsing();
				} else {
					repaint(true);
				}
			}
		});
		
		editor.setTooltipResolver(new TooltipResolver() {
			public String getTooltip(int pos) {
				ITextItem item = parsedRegex.getItem(pos);
				return item == null ? null : item.getHint();
			}
		});
		
		reparsing();
	}
	
	private void repaint(boolean needSend) {
		int length = editor.getText().length();
		sd = sd.getInstance(length);
		RegexConfig.getInstance().getRegexDefaultStyle().apply(sd, 0, length);
		parsedRegex.paint(sd, editor.getCaret());
		editor.paint(sd);
		if (needSend)
			regexChangeListeners.send();
	}

	private void reparsing() {
		parsedRegex = new ParsedRegex(editor.getText(), flags.getFlags());
		repaint(false);
		regexChangeListeners.send(parsedRegex);
	}

	public ParsedRegex getParsedRegex() {
		return parsedRegex;
	}

	public ITextEditorAdapter getRegexEditor() {
		return editor;
	}

}
