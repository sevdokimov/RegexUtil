package com.ess.regexutil.controls;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ess.regexutil.parsedtext.ITextEditorAdapter;
import com.ess.regexutil.parsedtext.ITextItem;
import com.ess.regexutil.parsedtext.ITextStyle;
import com.ess.regexutil.parsedtext.StyleData;
import com.ess.regexutil.parsedtext.TextStyle;
import com.ess.regexutil.regexparser.Bracket;
import com.ess.regexutil.regexparser.CloseBracket;
import com.ess.regexutil.regexparser.GroupNumber;
import com.ess.regexutil.regexparser.OpenBracket;
import com.ess.regexutil.regexparser.ParsedRegex;
import com.ess.regexutil.regexparser.RegexConfig;
import com.ess.util.EventListener;

public class SecondaryEditorHilighter {

	private ITextStyle selectionStyle;

	private ITextEditorAdapter textEditor;

	private ITextEditorAdapter regexEditor;

	private RegexHighlighter highlighter;

	private StyleData sd = new StyleData(100);

	private boolean matchMode;
	
	private boolean isDisable;
	
	public SecondaryEditorHilighter(RegexHighlighter highlighter,
			ITextEditorAdapter textEditor, boolean isDisable)
	{
		this.isDisable = isDisable;
		this.highlighter = highlighter;
		this.textEditor = textEditor;
		regexEditor = highlighter.getRegexEditor();

		textEditor.getListenersList().addListener(new EventListener<String>() {
			public void notify(String event) {
				if (SecondaryEditorHilighter.this.isDisable)
					return;
				if (event != null)
					repaint();
			}
		});
		highlighter.regexChangeListeners.addListener(new EventListener<ParsedRegex>() {
			public void notify(ParsedRegex event) {
				if (SecondaryEditorHilighter.this.isDisable)
					return;

				repaint();
			}
		});

		selectionStyle = new TextStyle(0x00FFFFFF, 0x003399FF, null, null);
	}

	public boolean isMatchMode() {
		return matchMode;
	}

	public void setMatchMode(boolean matchMode) {
		if (this.matchMode == matchMode)
			return;
		
		this.matchMode = matchMode;
		repaint();
	}

	public boolean isDisable() {
		return isDisable;
	}

	public void setDisable(boolean isDisable) {
		if (this.isDisable != isDisable) {
			this.isDisable = isDisable;
			if (!isDisable)
				repaint();
		}
	}

	private void repaint() {
		ParsedRegex parsedRegex = highlighter.getParsedRegex();

		String text = textEditor.getText();

		sd = sd.getInstance(text.length());

        try {
            RegexConfig.getInstance().getTextDefaultStyle().apply(sd, 0, text.length());

            if (!parsedRegex.isError()) {

                Pattern pattern = parsedRegex.getPattern();

                int highLightingGroup = -1;
                int startSel = regexEditor.getStartSelection();
                int endSel = regexEditor.getEndSelection();
                if (startSel == endSel) {
                    int caret = regexEditor.getCaret();
                    ITextItem item = parsedRegex.getItemAtCaretLocation(caret);
                    if (item instanceof OpenBracket || item instanceof CloseBracket) {
                        OpenBracket bracket;
                        if (item instanceof CloseBracket) {
                            bracket = (OpenBracket) ((CloseBracket) item).getOtherBracket();
                        } else {
                            bracket = (OpenBracket) item;
                        }
                        if (bracket.getGroupNumber() != -1) {
                            highLightingGroup = bracket.getGroupNumber();
                        }
                    }
                } else {
                    ITextItem firstItem = parsedRegex.getItem(startSel);
                    if (firstItem != null && firstItem.getIndex() == startSel) {
                        ITextItem lastItem = firstItem;
                        while (lastItem != null && lastItem.getEnd() < endSel) {
                            if (lastItem instanceof Bracket
                                    && ((Bracket) lastItem).isOpen()) {
                                lastItem = ((Bracket) lastItem).getOtherBracket();
                            } else {
                                lastItem = lastItem.getNext();
                            }
                        }

                        if (lastItem != null && lastItem.getEnd() == endSel
                                && parsedRegex.canAddGroup(startSel, endSel)) {
                            highLightingGroup = 1;
                            for (ITextItem item = firstItem.getPred(); item != null; item = item.getPred()) {
                                if (item instanceof OpenBracket
                                        && ((OpenBracket) item).getGroupNumber() != -1) {

                                    highLightingGroup = ((OpenBracket) item).getGroupNumber() + 1;
                                    break;
                                }
                            }

                            String regex = parsedRegex.getRegex();
                            StringBuilder res = new StringBuilder();
                            List<ITextItem> list = parsedRegex.getList();
                            for (ITextItem item : list) {
                                if (item == firstItem)
                                    res.append('(');

                                    if (item instanceof GroupNumber
                                            && ((GroupNumber) item).getGroupNumber() >= highLightingGroup) {

                                        res.append('\\').append(((GroupNumber) item).getGroupNumber() + 1);
                                    } else {
                                        res.append(regex, item.getIndex(), item.getEnd());
                                    }

                                    if (item == lastItem)
                                        res.append(')');
                            }

                            try {
                                pattern = Pattern.compile(res.toString(), parsedRegex.getFlags());
                            } catch (RuntimeException e) {
                                highLightingGroup = -1;
                                System.err.println("Error: " + e);
                            }
                        }
                    }
                }

                Matcher m = pattern.matcher(text);
                if (matchMode) {
                    if (m.matches()) {
                        RegexConfig.getInstance().getMatchedText1().apply(sd, 0, text.length());
                        if (highLightingGroup != -1 && m.group(highLightingGroup) != null) {
                            selectionStyle.apply(sd, m.start(highLightingGroup), m.end(highLightingGroup));
                        }
                    }
                } else {
                    int i = 0;
                    while (m.find()) {
                        ((i & 1) == 0
                                ? RegexConfig.getInstance().getMatchedText1()
                                : RegexConfig.getInstance().getMatchedText2()).apply(sd, m.start(), m.end());

                        if (highLightingGroup != -1 && m.group(highLightingGroup) != null) {
                            selectionStyle.apply(sd, m.start(highLightingGroup), m.end(highLightingGroup));
                        }
                        i++;
                    }
                }
            }
        }
        catch (Throwable e) {
            RegexConfig.getInstance().getError().apply(sd, 0, text.length());
        }

        textEditor.paint(sd);
	}

}
