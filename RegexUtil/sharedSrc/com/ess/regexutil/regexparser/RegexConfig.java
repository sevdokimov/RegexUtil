package com.ess.regexutil.regexparser;

import com.ess.regexutil.parsedtext.ITextStyle;
import com.ess.regexutil.parsedtext.TextStyle;

public class RegexConfig {

    public static void setConfig(RegexConfig config) {
        instance = config;
    }

    private static RegexConfig instance = new RegexConfig();

    private final ITextStyle currentItem = TextStyle.backgroundStyle(0xEBEBEB);

    private final ITextStyle childElement = TextStyle.backgroundStyle(0xC8E7F2);
    private final ITextStyle specSymbol = TextStyle.foregroundStyle(0x787800);
    private final ITextStyle error = TextStyle.foregroundStyle(0xFF0000);

    private final ITextStyle regexDefaultStyle = new TextStyle(0, 0xFFFFFF, false, false);
    private final ITextStyle defaultStyle = new TextStyle(0, 0xFFFFFF, false, false);

    private final ITextStyle textDefaultStyle = new TextStyle(0, 0xFFFFFF, false, false);
    private final ITextStyle splitResultDefaultStyle = new TextStyle(0, 0xFFFFFF, false, false);

    private final ITextStyle splitPartNumberStyle = new TextStyle(0x0000FF, null, null, null);
    private final ITextStyle splitResultStyle = TextStyle.backgroundStyle(0xC8C8C8);

    private final ITextStyle comma = TextStyle.BOLD_STYLE;

    private final ITextStyle escapeComment = TextStyle.ITALIC_STYLE;

    private final ITextStyle number = TextStyle.foregroundStyle(0x0000FF);
    private final ITextStyle notUsage = TextStyle.foregroundStyle(0x646464);
    private final ITextStyle symbolGroup = TextStyle.foregroundStyle(0x009600);

    private final ITextStyle replaced = TextStyle.backgroundStyle(0xC8C8C8);

    private final ITextStyle bracket = TextStyle.backgroundStyle(0x8D9EFC);

    private final ITextStyle matchedText1 = TextStyle.backgroundStyle(0xC8C8FF);
    private final ITextStyle matchedText2 = TextStyle.backgroundStyle(0xC8C8C8);

    private final ITextStyle comments = new TextStyle(0x646464, null, null, true);

    private RegexConfig() {
    }

    public static RegexConfig getInstance() {
        return instance;
    }


    public ITextStyle getCurrentItem() {
        return currentItem;
    }

    public ITextStyle getSpecSimbol() {
        return specSymbol;
    }

    public ITextStyle getChildElement() {
        return childElement;
    }

    public ITextStyle getError() {
        return error;
    }

    public ITextStyle getComma() {
        return comma;
    }

    public ITextStyle getComments() {
        return comments;
    }

    public ITextStyle getNumber() {
        return number;
    }

    public ITextStyle getNotUsage() {
        return notUsage;
    }

    public ITextStyle getSymbolGroup() {
        return symbolGroup;
    }

    public ITextStyle getCurrentBracket() {
        return bracket;
    }

    public ITextStyle getEscapeComment() {
        return escapeComment;
    }

    public ITextStyle getRegexDefaultStyle() {
        return regexDefaultStyle;
    }

    public ITextStyle getTextDefaultStyle() {
        return textDefaultStyle;
    }

    public ITextStyle getSplitResultDefaultStyle() {
        return splitResultDefaultStyle;
    }

    public ITextStyle getSplitPartNumberStyle() {
        return splitPartNumberStyle;
    }

    public ITextStyle getMatchedText1() {
        return matchedText1;
    }

    public ITextStyle getMatchedText2() {
        return matchedText2;
    }

    public ITextStyle getDefaultStyle() {
        return defaultStyle;
    }

    public ITextStyle getReplaced() {
        return replaced;
    }

    public ITextStyle getSplitResultStyle() {
        return splitResultStyle;
    }


    public static class Dark extends RegexConfig {

        private final int BG_PANEL_COLOR = 0x2B2C2D; //Color for edit field

        @Override
        public ITextStyle getCurrentItem() {
            return TextStyle.backgroundStyle(0x595959);
        }

        @Override
        public ITextStyle getChildElement() {
            return TextStyle.backgroundStyle(0x595959);
        }

        @Override
        public ITextStyle getRegexDefaultStyle() {
            return new TextStyle(0xFFFFFF, BG_PANEL_COLOR, false, false);
        }

        @Override
        public ITextStyle getDefaultStyle() {
            return new TextStyle(0xFFFFFF, BG_PANEL_COLOR, false, false);
        }

        @Override
        public ITextStyle getTextDefaultStyle() {
            return new TextStyle(0xFFFFFF, BG_PANEL_COLOR, false, false);
        }

        @Override
        public ITextStyle getSplitResultDefaultStyle() {
            return new TextStyle(0xFFFFFF, BG_PANEL_COLOR, false, false);
        }

        @Override
        public ITextStyle getSymbolGroup() {
            return TextStyle.foregroundStyle(0x72c972);
        }

        @Override
        public ITextStyle getSplitPartNumberStyle() {
            return new TextStyle(0x6e89c9, null, null, null);
        }

        @Override
        public ITextStyle getNumber() {
            return new TextStyle(0x8c8cff, null, null, null);
        }

        @Override
        public ITextStyle getMatchedText1() {
            return TextStyle.backgroundStyle(0x656545);
        }

        @Override
        public ITextStyle getMatchedText2() {
            return TextStyle.backgroundStyle(0x50508F);
        }

        @Override
        public ITextStyle getReplaced() {
            return TextStyle.backgroundStyle(0x787878);
        }

        @Override
        public ITextStyle getSplitResultStyle() {
            return TextStyle.backgroundStyle(0x787878);
        }
    }
}
