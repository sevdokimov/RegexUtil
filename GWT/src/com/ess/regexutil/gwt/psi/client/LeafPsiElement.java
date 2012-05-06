package com.ess.regexutil.gwt.psi.client;

/**
 * @author Sergey Evdokimov
 */
public class LeafPsiElement extends PsiElement {

  private int length;

  public LeafPsiElement(int type) {
    super(type);
  }

  @Override
  public int getType() {
    return type;
  }

  @Override
  public int getLength() {
    return length;
  }

  public void setLength(int length) {
    this.length = length;
  }
}
