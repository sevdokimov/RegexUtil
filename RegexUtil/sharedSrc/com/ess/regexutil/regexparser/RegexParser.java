package com.ess.regexutil.regexparser;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import com.ess.regexutil.parsedtext.ITextItem;
import com.ess.regexutil.parsedtext.TextItem;

class RegexParser {

	private static RegexParser instance = new RegexParser();
	
	public static RegexParser getInstance() {
		return instance;
	}

	public class RegexParserState {

		private final String text;
		private int flags;
		private int index;
		private int groupCount;
		
		private RegexParserState(String text, int flags) {
			this.text = text;
			this.flags = flags;
			groupCount = 1;
		}
		
		public char get(int i) {
			return text.charAt(index + i);
		}
		public char get() {
			return text.charAt(index);
		}
		public int getFlags() {
			return flags;
		}
		public int getIndex() {
			return index;
		}
		public String getText() {
			return text;
		}
		public int getGroupCount() {
			return groupCount;
		}
	}

	public List<TextItem> parse(String regex, int flags) {
		if (regex.indexOf(IRegexParserConst.END_SEQUENCE) != -1)
			throw new IllegalArgumentException("Unsupported regex with symbol 0xFFFF");

		List<TextItem> res = new ArrayList<TextItem>(regex.length());
		
		if ((flags & Pattern.LITERAL) != 0) {
			res.add(new LiteralItem(regex));
			return res;
		}
		
		RegexParserState st = new RegexParserState(regex + IRegexParserConst.END_SEQUENCE, flags);
		
        int[] flagStack = new int[regex.length()];
        int top = 0;

        mmm:
        do {
            char a = st.get();
            
            TextItem item;
            switch (a) {
            case '\\':
            	item = getEscapedItem(st);
            	break;
            	
            case '^':
            case '$':
            	item = new SpecSymbol(st);
            	break;
            	
			case IRegexParserConst.END_SEQUENCE:
				break mmm;

			case '|':
				item = new OrSymbol(st);
				break;
				
			case '.':
				item = new Dote(st);
				break;
				
            case '?': 
            case '*':
            case '+':
            	item = new Multeplexor(st);
            	break;
            	
            case '{': // multeplexors
            	item = BraceMulteplexors.factory.tryCreate(st);
            	break;
			
            case '(':
            {
            	OpenBracket openBracket = new OpenBracket(st);
            	if (openBracket.getGroupNumber() != -1)
            		st.groupCount++;

            	flagStack[top++] = st.flags;
            	st.flags = openBracket.changeFlags(st.flags);

            	if (openBracket.isFlagWithoutBracket()) {
            		flagStack[top - 1] = st.flags;
            	}
            	
                item = openBracket;
            } break;

            case '[':
            	item = null;
            	st.flags |= IRegexParserConst.IN_SQR_BRACKET;
            	parseBrackets(res, st);
            	st.flags &= (~IRegexParserConst.IN_SQR_BRACKET);
            	break;
            	
            case ')': {
            	CloseBracket closeBracket = new CloseBracket(st);
            	if (top > 0) {
            		st.flags = flagStack[--top];
            	}
            	item = closeBracket;
            }
            break;
            	
            case '#':
                if ((st.flags & Pattern.COMMENTS) != 0) {
                	item = new CommentItem(st);
                    break;
                }
			default:
				item = new SimpleSymbol(st);
				break;
			}
            
            if (item != null) {
            	st.index += item.getLength();
            	res.add(item);
            }
        } while (true);
        
        int resSize = res.size();
        for (int k = resSize - 1; --k >= 1; ) {
        	TextItem item = res.get(k);
        	item.setPred(res.get(k - 1));
        	item.setNext(res.get(k + 1));
        }
        if (resSize > 1) {
        	res.get(0).setNext(res.get(1));
        	res.get(resSize - 1).setPred(res.get(resSize - 2));
        }
        
        for (int k = 0; k < res.size(); k++) {
        	res.get(k).verify();
        }
        
        postTest(res, regex, flags);
        
		return res;
	}
	
	private void postTest(List<TextItem> res, String regex, int flags) {
        boolean hasRealError;
        try {
			Pattern.compile(regex, flags);
			hasRealError = false;
		} catch (RuntimeException e) {
			hasRealError = true;
		}
		boolean hasOurError = false;
        for (TextItem item : res) {
			if (item.isError()) {
				hasOurError = true;
				break;
			}
		}
		if (hasRealError != hasOurError) {
			System.err.println("Internal error: (ourError: " + hasOurError + " realError: " + hasRealError + ") regex: " + regex);
		}
	}
	
	@SuppressWarnings("unchecked")
	private void parseBrackets(List res, RegexParserState st) {
		SqrOpenBracket openBracket = new SqrOpenBracket(st);
		st.index += openBracket.getLength();
		
		res.add(openBracket);
		
		if (st.get() == ']') {
			res.add(new SimpleSymbol(st));
			st.index++;
		}
		
		int state = 0;
		ITextItem item;
		mmm:
		do {
			char a = st.get();
			switch (a) {
			case '\\':
				item = getEscapedItem(st);
				if (item instanceof GroupNumber || item instanceof SpecEscape) {
					item = new ErrorItem(st, item.getLength(), "This item may not be in \"[..]\"");
				}
				break;
				
			case ']':
				item = new SqrCloseBracket(st);
				break;
				
            case IRegexParserConst.END_SEQUENCE:
            	break mmm;
            	
            case '[':
            	item = null;
            	parseBrackets(res, st);
            	break;
            	
            case '#':
                item = (st.flags & Pattern.COMMENTS) != 0 
                		? new CommentItem(st)
                		: new SimpleSymbol(st);
                break;
                
            case '&':
            	item = (st.get(1) == '&') ? new AndInBracket(st) : new SimpleSymbol(st);
            	break;
            		
            default:
				item = new SimpleSymbol(st);
				break;
			}
			
            if (item != null) {
            	
            	switch (state) {
            	case 0:
            		if (item instanceof IOneSymbol) {
            			state = 1;
            		}
            		break;
            		
            	case 1:
            		if (!(item instanceof IOneSymbol)) {
            			state = 0;
            		} else if (item instanceof SimpleSymbol && ((SimpleSymbol)item).getSymbol() == '-') {
            			state = 2;
            		}
            		break;

            	case 2:
            		if (!(item instanceof IOneSymbol)) {
            			if (item instanceof EscapeComment) {
            				
            			} else {
            				if (!(item instanceof SqrCloseBracket)) {
                				SimpleSymbol minus = (SimpleSymbol) res.remove(res.size() - 1);
                				st.index = minus.getIndex();
                				res.add(new ErrorItem(st, 1, null));
                				st.index++;
            				}
            			}
            		} else {
            			SimpleSymbol minus = (SimpleSymbol) res.remove(res.size() - 1);
            			IOneSymbol firstItem = (IOneSymbol) res.remove(res.size() - 1);
            			IOneSymbol secondItem = (IOneSymbol) item;
            			st.index = firstItem.getIndex();
            			if (firstItem.getSymbol() > secondItem.getSymbol()) {
            				item = new ErrorItem(
            						st, 
            						minus.getLength() + firstItem.getLength() + secondItem.getLength(),
            						null);
            			} else {
            				item = new Interval(st, firstItem, minus, secondItem);
            			}
            			st.index = item.getIndex();
            		}
            		state = 0;
            	}
            	
            	st.index += item.getLength();
            	res.add(item);
            }
		} while (!(item instanceof SqrCloseBracket));
	}
	
	private static final ItemFactoryList ESCAPED_ITEM_FACTORY_LIST = new ItemFactoryList(new ItemFactory[] {
			SimpleGroupItem.factory,
			SpecEscape.factory,
			CharByCode.factory,
			ControlCor.factory,
			EscapeP.factory,
			EscapeComment.factory,
			EscapedChar.factory,
	});
	
	private TextItem getEscapedItem(RegexParserState st) {
		char a = st.get(1);
		
        if (a == IRegexParserConst.END_SEQUENCE)
            return new ErrorItem(st, 1, "Illegal/unsupported escape sequence");

		if (a >= '1' && a <= '9') {
			int group = a - '0';
			if (st.groupCount < group) {
				return new ErrorItem(st, 2, "There are not group #" + a);
			}
			
	        int k = st.getIndex() + 1;
	        do {
	        	a = st.getText().charAt(++k);
	        	if (a < '0' || a > '9')
	        		break;
	        	int newGroup = group*10 + a - '0';
	        	if (st.groupCount - 1 < newGroup)
	        		break;
	        	group = newGroup;
	        } while (true);
	        
	        return new GroupNumber(st, k - st.index, group);
		}

        TextItem res = ESCAPED_ITEM_FACTORY_LIST.create(st);
        if (res != null)
        	return res;
        
		return new ErrorItem(st, 2, "Illegal/unsupported escape sequence: \\" + a);
	}
	
}
