package com.ess.regexutil.gwt.psi.client;

/**
 * @author Sergey Evdokimov
 */
public class PsiFile extends CompositePsiElement {

  public static final int TYPE = counter++;

  private final String text;

  public PsiFile(String text) {
    super(TYPE);
    this.text = text;
  }

  public String getText() {
    return text;
  }
}
