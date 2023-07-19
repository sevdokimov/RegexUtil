package com.ess.regexutil.ideaplugin;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import org.intellij.lang.regexp.AsciiUtil;
import org.intellij.lang.regexp.DefaultRegExpPropertiesProvider;
import org.intellij.lang.regexp.RegExpLanguageHost;
import org.intellij.lang.regexp.UnicodeCharacterNames;
import org.intellij.lang.regexp.psi.RegExpBoundary;
import org.intellij.lang.regexp.psi.RegExpChar;
import org.intellij.lang.regexp.psi.RegExpElement;
import org.intellij.lang.regexp.psi.RegExpGroup;
import org.intellij.lang.regexp.psi.RegExpNamedCharacter;
import org.intellij.lang.regexp.psi.RegExpNamedGroupRef;
import org.intellij.lang.regexp.psi.RegExpNumber;
import org.intellij.lang.regexp.psi.RegExpSimpleClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

/**
 * Copy/paste of {@link com.intellij.psi.impl.JavaRegExpHost}. We have to copy it to avoid a dependency to java.
 */
public class MyJavaRegExpHost implements RegExpLanguageHost {

    protected static final EnumSet<RegExpGroup.Type> SUPPORTED_NAMED_GROUP_TYPES = EnumSet.of(RegExpGroup.Type.NAMED_GROUP);
    private final DefaultRegExpPropertiesProvider myPropertiesProvider;

    private static final int myNumberOfGeneralCategoryProperties = 58;
    private final String[][] myPropertyNames = {
            {"Cn", "Unassigned"},
            {"Lu", "Uppercase letter"},
            {"Ll", "Lowercase letter"},
            {"Lt", "Titlecase letter"},
            {"Lm", "Modifier letter"},
            {"Lo", "Other letter"},
            {"Mn", "Non spacing mark"},
            {"Me", "Enclosing mark"},
            {"Mc", "Combining spacing mark"},
            {"Nd", "Decimal digit number"},
            {"Nl", "Letter number"},
            {"No", "Other number"},
            {"Zs", "Space separator"},
            {"Zl", "Line separator"},
            {"Zp", "Paragraph separator"},
            {"Cc", "Control"},
            {"Cf", "Format"},
            {"Co", "Private use"},
            {"Cs", "Surrogate"},
            {"Pd", "Dash punctuation"},
            {"Ps", "Start punctuation"},
            {"Pe", "End punctuation"},
            {"Pc", "Connector punctuation"},
            {"Po", "Other punctuation"},
            {"Sm", "Math symbol"},
            {"Sc", "Currency symbol"},
            {"Sk", "Modifier symbol"},
            {"So", "Other symbol"},
            {"Pi", "Initial quote punctuation"},
            {"Pf", "Final quote punctuation"},
            {"L", "Letter"},
            {"M", "Mark"},
            {"N", "Number"},
            {"Z", "Separator"},
            {"C", "Control"},
            {"P", "Punctuation"},
            {"S", "Symbol"},
            {"LC", "Letter"},
            {"LD", "Letter or digit"},
            {"L1", "Latin-1"},
            {"all", "All"},
            {"javaLowerCase", },
            {"javaUpperCase", },
            {"javaTitleCase", },
            {"javaAlphabetic", },
            {"javaIdeographic", },
            {"javaDigit", },
            {"javaDefined", },
            {"javaLetter", },
            {"javaLetterOrDigit", },
            {"javaJavaIdentifierStart", },
            {"javaJavaIdentifierPart", },
            {"javaUnicodeIdentifierStart", },
            {"javaUnicodeIdentifierPart", },
            {"javaIdentifierIgnorable", },
            {"javaSpaceChar", },
            {"javaWhitespace", },
            {"javaISOControl", },
            {"javaMirrored", },
            /* end of general category properties */
            {"ASCII", "Ascii"},
            {"Alnum", "Alphanumeric characters"},
            {"Alpha", "Alphabetic characters"},
            {"Blank", "Space and tab characters"},
            {"Cntrl", "Control characters"},
            {"Digit", "Numeric characters"},
            {"Graph", "Printable and visible"},
            {"Lower", "Lowercase Alphabetic"},
            {"Print", "Printable characters"},
            {"Punct", "Punctuation characters"},
            {"Space", "Space characters"},
            {"Upper", "Uppercase alphabetic"},
            {"XDigit", "Hexadecimal digits"},
    };

    public MyJavaRegExpHost() {
        myPropertiesProvider = DefaultRegExpPropertiesProvider.getInstance();
    }

    @Override
    public boolean supportsInlineOptionFlag(char flag, PsiElement context) {
        switch (flag) {
            case 'i': // case-insensitive matching
            case 'd': // Unix lines mode
            case 'm': // multiline mode
            case 's': // dotall mode
            case 'u': // Unicode-aware case folding
            case 'x': // whitespace and comments in pattern
            case 'U': // Enables the Unicode version of Predefined character classes and POSIX character classes
                    return true;
            default: return false;
        }
    }

    public boolean characterNeedsEscaping(char c) {
        return false;
    }

    /**
     * @since 2023.1
     */
    public boolean characterNeedsEscaping(char c, boolean isInClass) {
        return false;
    }

    @Override
    public boolean supportsNamedCharacters(RegExpNamedCharacter namedCharacter) {
        return hasAtLeastJdkVersion(namedCharacter);
    }

    @Override
    public boolean supportsPerl5EmbeddedComments() {
        return false;
    }

    @Override
    public boolean supportsPossessiveQuantifiers() {
        return true;
    }

    @Override
    public boolean supportsPythonConditionalRefs() {
        return false;
    }

    @Override
    public boolean supportsNamedGroupSyntax(RegExpGroup group) {
        return group.getType() == RegExpGroup.Type.NAMED_GROUP && hasAtLeastJdkVersion(group);
    }

    @Override
    public boolean supportsNamedGroupRefSyntax(RegExpNamedGroupRef ref) {
        return ref.isNamedGroupRef() && hasAtLeastJdkVersion(ref);
    }

    @NotNull
    @Override
    public EnumSet<RegExpGroup.Type> getSupportedNamedGroupTypes(RegExpElement context) {
        if (!hasAtLeastJdkVersion(context)) {
            return EMPTY_NAMED_GROUP_TYPES;
        }
        return SUPPORTED_NAMED_GROUP_TYPES;
    }

    @Override
    public boolean isValidGroupName(String name, @NotNull RegExpGroup group) {
        for (int i = 0, length = name.length(); i < length; i++) {
            final char c = name.charAt(i);
            if (!AsciiUtil.isLetterOrDigit(c)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean supportsExtendedHexCharacter(RegExpChar regExpChar) {
        return regExpChar.getUnescapedText().charAt(1) == 'x' && hasAtLeastJdkVersion(regExpChar);
    }

    @Override
    public boolean supportsBoundary(RegExpBoundary boundary) {
        switch (boundary.getType()) {
            case UNICODE_EXTENDED_GRAPHEME: return hasAtLeastJdkVersion(boundary);
            case RESET_MATCH: return false;
            case LINE_START:
            case LINE_END:
            case WORD:
            case NON_WORD:
            case BEGIN:
            case END:
            case END_NO_LINE_TERM:
            case PREVIOUS_MATCH:
            default:
                return true;
        }
    }

    @Override
    public boolean supportsSimpleClass(RegExpSimpleClass simpleClass) {
        switch (simpleClass.getKind()) {
            case UNICODE_LINEBREAK: case HORIZONTAL_SPACE: case NON_HORIZONTAL_SPACE: case NON_VERTICAL_SPACE:
                    return hasAtLeastJdkVersion(simpleClass);
            case VERTICAL_SPACE:
                // is vertical tab before jdk 1.8
                    return true;
            case UNICODE_GRAPHEME:
                return hasAtLeastJdkVersion(simpleClass);
            case XML_NAME_START: case NON_XML_NAME_START: case XML_NAME_PART: case NON_XML_NAME_PART:
                return false;
            default:
                return true;
        }
    }

    @Override
    public boolean supportsLiteralBackspace(RegExpChar aChar) {
        return false;
    }

    private static boolean hasAtLeastJdkVersion(PsiElement element) {
        return true;
    }

    @Override
    public boolean isValidPropertyName(@NotNull String name) {
        return isScriptProperty(name) || isBlockProperty(name) || isCategoryProperty(name);
    }

    private static boolean isScriptProperty(@NotNull String propertyName) {
        return "script".equalsIgnoreCase(propertyName) || "sc".equalsIgnoreCase(propertyName);
    }

    private static boolean isBlockProperty(@NotNull String propertyName) {
        return "block".equalsIgnoreCase(propertyName) || "blk".equalsIgnoreCase(propertyName);
    }

    private static boolean isCategoryProperty(@NotNull String propertyName) {
        return "general_category".equalsIgnoreCase(propertyName) || "gc".equalsIgnoreCase(propertyName);
    }

    @Override
    public boolean isValidPropertyValue(@NotNull String propertyName, @NotNull String value) {
        if (isScriptProperty(propertyName)) {
            return isValidUnicodeScript(value);
        }
        else if (isBlockProperty(propertyName)) {
            return isValidUnicodeBlock(value);
        }
        else if (isCategoryProperty(propertyName)) {
            return isValidGeneralCategory(value);
        }
        return false;
    }

    public boolean isValidGeneralCategory(String value) {
        for (int i = 0; i < myNumberOfGeneralCategoryProperties; i++) {
            if (value.equals(myPropertyNames[i][0])) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isValidCategory(@NotNull String category) {
        if (category.startsWith("In")) {
            return isValidUnicodeBlock(category.substring(2));
        }
        if (category.startsWith("Is")) {
            category = category.substring(2);
            if (isValidProperty(category)) return true;

            // Unicode properties and scripts available since JDK 1.7
            category = StringUtil.toUpperCase(category);

            switch (category) { // see java.util.regex.UnicodeProp
                // 4 aliases
                case "WHITESPACE": case "HEXDIGIT": case "NONCHARACTERCODEPOINT": case "JOINCONTROL":
                    return true;
                case "ALPHABETIC": case "LETTER": case "IDEOGRAPHIC": case "LOWERCASE": case "UPPERCASE": case "TITLECASE":
                        case "WHITE_SPACE": case "CONTROL": case "PUNCTUATION": case "HEX_DIGIT": case "ASSIGNED": case "NONCHARACTER_CODE_POINT":
                        case "DIGIT": case "ALNUM": case "BLANK": case "GRAPH": case "PRINT": case "WORD": case "JOIN_CONTROL": return true;
                default:
                    return isValidUnicodeScript(category);
            }
        }
        return isValidProperty(category);
    }

    private boolean isValidProperty(@NotNull String category) {
        for (String[] name : myPropertyNames) {
            if (name[0].equals(category)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isValidUnicodeBlock(@NotNull String category) {
        try {
            return Character.UnicodeBlock.forName(category) != null;
        }
        catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static boolean isValidUnicodeScript(@NotNull String category) {
        try {
            return Character.UnicodeScript.forName(category) != null;
        }
        catch (IllegalArgumentException ignore) {
            return false;
        }
    }

    @Override
    public boolean isValidNamedCharacter(RegExpNamedCharacter namedCharacter) {
        return UnicodeCharacterNames.getCodePoint(namedCharacter.getName()) >= 0;
    }

    @Override
    public Lookbehind supportsLookbehind(@NotNull RegExpGroup lookbehindGroup) {
        return Lookbehind.FINITE_REPETITION;
    }

    @Override
    public Integer getQuantifierValue(@NotNull RegExpNumber number) {
        try {
            return Integer.valueOf(number.getUnescapedText());
        }
        catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public String[] @NotNull [] getAllKnownProperties() {
        return myPropertyNames;
    }

    @Nullable
    @Override
    public String getPropertyDescription(@Nullable String name) {
        if (StringUtil.isEmptyOrSpaces(name)) {
            return null;
        }
        for (String[] stringArray : myPropertyNames) {
            if (stringArray[0].equals(name)) {
                return stringArray.length > 1 ? stringArray[1] : stringArray[0];
            }
        }
        return null;
    }

    @Override
    public String[] @NotNull [] getKnownCharacterClasses() {
        return myPropertiesProvider.getKnownCharacterClasses();
    }

    public boolean belongsToConditionalExpression(@NotNull PsiElement psiElement) {
        return false;
    }
}
