package com.ess.regexutil.gwt.psi.client;

import com.ess.regexutil.gwt.psi.client.lexer.IElementType;

/**
 * @author Sergey Evdokimov
 */
public class PsiFile extends CompositePsiElement {

  public static final IElementType TYPE = new IElementType("FILE");

  private final String text;

  public PsiFile(String text) {
    super(TYPE);
    this.text = text;
  }

  @Override
  public PsiFile getFile() {
    return this;
  }

  public String getText() {
    return text;
  }

  @Override
  public int getIndex() {
    return 0;
  }

  @Override
  public String toString() {
    return "FILE: " + text;
  }
}
