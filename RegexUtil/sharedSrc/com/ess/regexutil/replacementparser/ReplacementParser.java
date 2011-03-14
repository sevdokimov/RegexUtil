package com.ess.regexutil.replacementparser;

import java.util.ArrayList;
import java.util.List;

import com.ess.regexutil.controls.RegexHighlighter;
import com.ess.regexutil.parsedtext.ITextItem;
import com.ess.regexutil.parsedtext.TextItem;
import com.ess.regexutil.regexparser.ErrorItem;

public class ReplacementParser {
	
	private static final ReplacementParser instance = new ReplacementParser();

	public static ReplacementParser getInstance() {
		return instance;
	}
	
	private ReplacementParser() {
		
	}
	
	public List<ITextItem> parse(RegexHighlighter highlighter, String replacement) {
		ArrayList<ITextItem> res = new ArrayList<ITextItem>();
		int groupCount = highlighter.getParsedRegex().getGroupCount();
		int i = 0;
		while (i < replacement.length()) {
			TextItem item;
			char a = replacement.charAt(i);
			if (a == '\\') {
				if (i + 1 == replacement.length()) {
					item = new ErrorItem(i, 1, null);
				} else {
					item = new EscapedSymbol(i, replacement);
				}
			} else if (a == '$') {
				int k = i + 1;
                if (k == replacement.length() || replacement.charAt(k) < '0' || replacement.charAt(k) > '9') {
                	item = new ErrorItem(i , 1, null); // @TODO
                } else {
                    int refNum = (int)replacement.charAt(k) - '0';
                	k++;
                	// Capture the largest legal group string
                	do {
                		if (k >= replacement.length())
                			break;
                		
                		int nextDigit = replacement.charAt(k) - '0';
                		if (nextDigit < 0 || nextDigit > 9) // not a number
                			break;
                			
                		int newRefNum = (refNum * 10) + nextDigit;
                		if (groupCount < newRefNum)
                			break;

                		refNum = newRefNum;
                		k++;
                	} while (true);
            		if (groupCount <= refNum)
            			item = new ErrorItem(i , k - i, "There are no group #" + refNum); // @TODO
            		else
            			item = new RepGroup(i, k - i, refNum);
                }
			} else {
				int k;
				for ( k = i + 1; k < replacement.length(); k++) {
					char c = replacement.charAt(k);
					if (c == '\\' || c == '$')
						break;
				}
				item = new TextItem(i, k - i);
			}
			res.add(item);
			i += item.getLength();
		}
		return res;
	}
		
	
}
