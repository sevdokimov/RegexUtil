package com.ess.regexutil.regexparser;

import com.ess.regexutil.parsedtext.ITextStyle;
import com.ess.regexutil.parsedtext.StyleData;
import com.ess.regexutil.parsedtext.TextItem;
import com.ess.regexutil.regexparser.RegexParser.RegexParserState;

public class RegexItem extends TextItem {

	protected final static RegexConfig rc = RegexConfig.getInstance();
	
	protected ITextStyle style;
	
	public RegexItem(RegexParserState st, int length) {
		this(st.getIndex(), length);
	}
	
	public RegexItem(int index, int length) {
		super(index, length);
	}

	@Override
	public final void highlight(StyleData sd, int caret) {
		boolean isError = isError();
		if (!isError) {
			if (hasCaret(caret)) {
				if (length > 1)
					paint(sd, rc.getCurrentItem());
				highlightChild(sd);
			}
			highlightInternal(sd, caret);
		} else {
			highlightInternal(sd, caret);
			paint(sd, rc.getError());
		}
	}
	
	protected void highlightInternal(StyleData sd, int caret) {
		if (style != null)
			style.apply(sd, getIndex(), getEnd());
	}
	
	protected void highlightChild(StyleData sd) {
	}
	
}
