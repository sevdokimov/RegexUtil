package com.ess.regexutil.regexparser;

import com.ess.regexutil.parsedtext.TextItem;
import com.ess.regexutil.regexparser.RegexParser.RegexParserState;


interface ItemFactory {
	TextItem tryCreate(RegexParserState st);
}

class ItemFactoryList {
	private final ItemFactory[] factorys;

	public ItemFactoryList(final ItemFactory[] factorys) {
		this.factorys = factorys;
	}
	
	public TextItem create(RegexParserState st) {
		for (ItemFactory factory : factorys) {
			TextItem res = factory.tryCreate(st);
			if (res != null)
				return res;
		}
		return null;
	}
}
