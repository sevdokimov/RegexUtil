package com.ess.regexutil.gwt.regexEditor.client;

import com.ess.regexutil.gwt.psi.client.ITextStyle;
import com.ess.regexutil.gwt.psi.client.TextStyle;

public class RegexConfig {

  private static final RegexConfig instance = new RegexConfig();

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
}
