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

  public String getText() {
    return text;
  }
}
