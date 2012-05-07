package com.ess.regexutil.gwt.psi.client.lexer;

import com.ess.regexutil.gwt.psi.client.ITextStyle;
import com.ess.regexutil.gwt.psi.client.LeafPsiElement;

/**
 * @author Sergey Evdokimov
 */
public class IElementType {

  private static int counter = 1;

  private final int myIndex = counter++;

  private final String myDescription;

  private final ITextStyle myTextStyle;

  public IElementType(String description) {
    this(description, null);
  }

  public IElementType(String description, ITextStyle style) {
    myDescription = description;
    myTextStyle = style;
  }

  public int getIndex() {
    return myIndex;
  }

  public ITextStyle getTextStyle() {
    return myTextStyle;
  }

  public String getDescription() {
    return myDescription;
  }

  public static int getMaxElementTypeIndex() {
    return counter;
  }

  public LeafPsiElement createLeafElement() {
    return new LeafPsiElement(this);
  }

  @Override
  public String toString() {
    return myDescription;
  }
}
