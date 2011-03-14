package com.ess.regexutil.regexparser;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ess.regexutil.parsedtext.TextItem;
import com.ess.regexutil.regexparser.RegexParser.RegexParserState;
import com.ess.util.Helper;


public class EscapeP extends RegexItem {

	private static final Pattern PATTERN = Pattern.compile("\\G(?:([A-Za-z])|(?:\\{(([^\\[\\](){}]*?)})?))");
	
	public EscapeP(RegexParserState st, int length, String name) {
		super(st, length);
		
        if (name.startsWith("In")) {
            try {
            	name = name.substring(2);
				Character.UnicodeBlock.forName(name);
				//int blockIndex = Helper.nativeIndexOf(CHAR_BLOCKS, block);
				hint = "\"" + name +"\" Unicode character block\nsee Character.UnicodeBlock";
			} catch (IllegalArgumentException e) {
            	isError = true;
            	hint = "Unknown character block: \"" + name + "\"\n" +
            			"see Character.UnicodeBlock\n" +
            			"see Character.UnicodeBlock.forName(String name)";
			}
        } else {
            if (name.startsWith("Is"))
                name = name.substring(2);
            hint = CATEGORY_MAP.get(name);
            if (hint == null) {
            	isError = true;
            	hint = "Unknown character group: \\p{" + name + '}';
            }
        } 
        style = rc.getSymbolGroup();
	}

	public static final ItemFactory factory = new ItemFactory() {
		public TextItem tryCreate(RegexParserState st) {
			if (st.get(1) != 'p')
				return null;
			
			Matcher m = PATTERN.matcher(st.getText());
			if (!m.find(st.getIndex() + 2)) {
				return new ErrorItem(st.getIndex(), 2, null);
			}
			
			String name = m.group(1);
			if (name == null) {
				if (m.group(2) == null) {
					return new ErrorItem(st.getIndex(), 3, null);
				}
				name = m.group(3);
				if (name == null)
					name = "";
			}
			return new EscapeP(st, 2 + m.group().length(), name);
		}
	};

    /**
     * From Pattern.categoryNames
     */
    private static final Map<String, String> CATEGORY_MAP = Helper.createUnmMap(new Object[] {
	"Cn", "UNASSIGNED",
	"Lu", "Upper case letter",
	"Ll", "Lower case letter",
	"Lt", "General category \"Nl\" in the Unicode specification.\n(see Character.TITLECASE_LETTER)",
	"Lm", "General category \"Lm\" in the Unicode specification.\n(see Character.MODIFIER_LETTER)",
	"Lo", "General category \"Lo\" in the Unicode specification.\n(see Character.OTHER_LETTER)",
	"Mn", "General category \"Mn\" in the Unicode specification.\n(see Character.NON_SPACING_MARK)",
	"Me", "Character.ENCLOSING_MARK",
	"Mc", "COMBINING_SPACING_MARK",
	"Nd", "DECIMAL_DIGIT_NUMBER",
	"Nl", "LETTER_NUMBER",
	"No", "OTHER_NUMBER",
	"Zs", "SPACE_SEPARATOR",
	"Zl", "LINE_SEPARATOR",
	"Zp", "PARAGRAPH_SEPARATOR",
	"Cc", "CNTRL",
	"Cf", "FORMAT",
	"Co", "PRIVATE_USE",
	"Cs", "SURROGATE",
	"Pd", "DASH_PUNCTUATION",
	"Ps", "START_PUNCTUATION",
	"Pe", "END_PUNCTUATION",
	"Pc", "CONNECTOR_PUNCTUATION",
	"Po", "OTHER_PUNCTUATION",
	"Sm", "MATH_SYMBOL",
	"Sc", "CURRENCY_SYMBOL",
	"Sk", "MODIFIER_SYMBOL",
	"So", "OTHER_SYMBOL",
        "L", "Any letter",
        "M", "MARK",
        "N", "NUMBER",
        "Z", "SEPARATOR",
        "C", "CONTROL",
        "P", "PUNCTUATION",
        "S", "SYMBOL",
        "LD", "LETTER_OR_DIGIT",
        "L1", "Latin-1",
        "all", "ALL",
        "ASCII", "ASCII",
        "Alnum", "Alphanumeric characters.",
        "Alpha", "Alphabetic characters.",
        "Blank", "Space and tab characters.",
        "Cntrl", "Control characters.",
        "Digit", "Numeric characters.",
        "Graph", "Characters that are printable and are also visible.\n(A space is printable, but \"not visible, while an `a' is both.)",
        "Lower", "Lower-case alphabetic characters.",
        "Print", "Printable characters (characters that are not control characters.)",
        "Punct", "Punctuation characters (characters that are not letter,\ndigits, control charact ers, or space characters).",
        "Space", "Space characters (such as space, tab, and formfeed, to name a few).",
        "Upper", "Upper-case alphabetic characters.",
        "XDigit", "Characters that are hexadecimal digits.",
        
        "javaLowerCase", "All character which Character.isLowerCase(a) == true",
        "javaUpperCase", "All character which Character.isUpperCase(a) == true",
        "javaTitleCase", "All character which Character.isTitleCase(a) == true",
        "javaDigit", "All character which Character.isDigit(a) == true",
        "javaDefined", "All character which Character.isDefined(a) == true",
        "javaLetter", "All character which Character.isLetter(a) == true",
        "javaLetterOrDigit", "All character which Character.isLetterOrDigit(a) == true",
        "javaJavaIdentifierStart", "All character which Character.isJavaIdentifierStart(a) == true",
        "javaJavaIdentifierPart", "All character which Character.isJavaIdentifierPart(a) == true",
        "javaUnicodeIdentifierStart", "All character which Character.isUnicodeIdentifierStart(a) == true",
        "javaUnicodeIdentifierPart", "All character which Character.isUnicodeIdentifierPart(a) == true",
        "javaIdentifierIgnorable", "All character which Character.isIdentifierIgnorable(a) == true",
        "javaSpaceChar", "All character which Character.isSpaceChar(a) == true",
        "javaWhitespace", "All character which Character.isWhitespace(a) == true",
        "javaISOControl", "All character which Character.isISOControl(a) == true",
        "javaMirrored", "All character which Character.isMirrored(a) == true",
    });
	
}
