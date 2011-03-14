package com.ess.regexutil.replacementparser;

import com.ess.regexutil.parsedtext.StyleData;
import com.ess.regexutil.parsedtext.TextItem;
import com.ess.regexutil.regexparser.RegexConfig;

public class RepGroup extends TextItem {

	public RepGroup(int index, int length, int groupNumber) {
		super(index, length);
	}

	@Override
	public void highlight(StyleData sd, int caret) {
		paint(sd, RegexConfig.getInstance().getNumber());
		if (hasCaret(caret)) {
			paint(sd, RegexConfig.getInstance().getCurrentItem());
		}
	}

}
