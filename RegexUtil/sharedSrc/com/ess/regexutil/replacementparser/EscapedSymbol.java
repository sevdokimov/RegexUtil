package com.ess.regexutil.replacementparser;

import com.ess.regexutil.parsedtext.StyleData;
import com.ess.regexutil.parsedtext.TextItem;
import com.ess.regexutil.regexparser.RegexConfig;
import com.ess.regexutil.regexparser.SimpleSymbol;

public class EscapedSymbol extends TextItem {
	
	private boolean notNeed;
	
	public EscapedSymbol(int index, String s) {
		super(index, 2);
		char a = s.charAt(index + 1);
		if (a != '\\' && a != '$') {
			notNeed = true;
			hint = SimpleSymbol.getHint(a) + " (escaping is not necessarily)";
		} else {
			hint = SimpleSymbol.getHint(a);
		}
	}

	@Override
	public void highlight(StyleData sd, int caret) {
		if (notNeed) {
			RegexConfig.getInstance().getNotUsage().apply(sd, getIndex(), getEnd());
		}
		if (hasCaret(caret)) {
			paint(sd, RegexConfig.getInstance().getCurrentItem());
		}
	}
	
}
