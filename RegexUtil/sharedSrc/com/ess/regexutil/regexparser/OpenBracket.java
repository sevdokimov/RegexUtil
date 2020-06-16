package com.ess.regexutil.regexparser;

import com.ess.regexutil.regexparser.RegexParser.RegexParserState;
import com.ess.util.Helper;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class OpenBracket extends Bracket {

	private final static Pattern PATTERN = Pattern.compile("\\G(?:" +
			"[=!>]" +
			"|<[=!]" +
			"|<[a-zA-Z][a-zA-Z0-9]*>" +
			"|([imsduUcx]*)(?:-([imsduUcx]*))?(:|\\)" +
			"))"); //$NON-NLS-1$
	
	private static final Map<Character, Integer> FLAGS_CODE = Helper.createSimpleMap(new Object[]{
			'd', Pattern.UNIX_LINES,
			'i', Pattern.CASE_INSENSITIVE,
			'x', Pattern.COMMENTS,
			'm', Pattern.MULTILINE,
			's', Pattern.DOTALL,
			'u', Pattern.UNICODE_CASE,
			'U', Pattern.UNICODE_CHARACTER_CLASS,
			'c', 0, // TODO
	});
	
	private int addFlags;
	private int removeFlags;
	
	private int groupNumber;
	
	private boolean flagWithoutBracket;
	
	public OpenBracket(RegexParserState st) {
		super(st, 1, '(', true); 
        if (st.get(1) == '?') {
            Matcher m = PATTERN.matcher(st.getText());
            if (m.find(st.getIndex() + 2)) {
                int length = m.group().length() + 2;
                
                addFlags = convertFlags(m.group(1));
                removeFlags = convertFlags(m.group(2));

                if (")".equals(m.group(3))) { //$NON-NLS-1$
                	length--;
                	flagWithoutBracket = true;
                }
                this.length = length;
            }
            groupNumber = -1;
        } else {
        	groupNumber = st.getGroupCount();
        }
	}

	@Override
	protected void setOtherBracket(Bracket otherBracket) {
		super.setOtherBracket(otherBracket);
		hint = (groupNumber == -1
				? flagWithoutBracket 
					? Messages.get("OpenBracket.OpenBracket_FlagModification")
					: Messages.get("OpenBracket.OpenBracket.Non_capturing_group") //$NON-NLS-1$ //$NON-NLS-2$
				: Messages.get("OpenBracket.OpenBracket.capturing_group_n") + groupNumber); //$NON-NLS-1$
	}

	public boolean isFlagWithoutBracket() {
		return flagWithoutBracket;
	}

	public int getGroupNumber() {
		return groupNumber;
	}

	private int convertFlags(String s) {
		int res = 0;
		if (s != null) {
			for (int i = s.length(); --i >= 0; ) {
				res |= FLAGS_CODE.get(s.charAt(i));
			}
		}
		return res;
	}
	
	public int changeFlags(int flags) {
		return (flags | addFlags) & (~removeFlags);
	}

}
