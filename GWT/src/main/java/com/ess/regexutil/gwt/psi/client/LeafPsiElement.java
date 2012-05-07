package com.ess.regexutil.gwt.psi.client;

import com.ess.regexutil.gwt.psi.client.lexer.IElementType;

/**
 * @author Sergey Evdokimov
 */
public class LeafPsiElement extends PsiElement {

  private int length;

  public LeafPsiElement(IElementType type) {
    super(type);
  }

  @Override
  public int getLength() {
    return length;
  }

  public void setLength(int length) {
    this.length = length;
  }

  @Override
  public void highlight(StyleData styleData) {
    ITextStyle style = type.getTextStyle();
    if (style != null) {
      style.apply(styleData, getIndex(), getEnd());
    }
  }
}
