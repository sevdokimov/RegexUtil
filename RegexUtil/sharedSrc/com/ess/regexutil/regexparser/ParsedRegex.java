package com.ess.regexutil.regexparser;

import java.util.List;
import java.util.regex.Pattern;

import com.ess.regexutil.parsedtext.ITextItem;
import com.ess.regexutil.parsedtext.ParsedText;
import com.ess.regexutil.parsedtext.TextItem;

public class ParsedRegex extends ParsedText {

	private final String regex;
	private final int flags;
	
	private int groupCount = 1;
	
	private Pattern pattern;
	
	public ParsedRegex(String regex, int flags) {
		List<TextItem> list = RegexParser.getInstance().parse(regex, flags);
		setList(list);
		
		if (!isError) {
			try {
				Pattern.compile(regex, flags);
			} catch (Throwable e) {
				isError = true;
			}
		}
		
		if (!isError) {
			for (int i = list.size(); --i >= 0; ) {
				ITextItem item = list.get(i);
				if (item instanceof OpenBracket) {
					int groupCount = ((OpenBracket)item).getGroupNumber();
					if (groupCount != -1) {
						this.groupCount = groupCount + 1;
						break;
					}
				}
			}
		} else {
			groupCount = 0;
		}
		this.regex = regex;
		this.flags = flags;
	}

	public int getGroupCount() {
		return groupCount;
	}

	public Pattern getPattern() {
		if (isError)
			return null;
		
		if (pattern == null)
			pattern = Pattern.compile(regex, flags);
		return pattern;
	}
	
	public int getFlags() {
		return flags;
	}

	public String getRegex() {
		return regex;
	}
	
}
