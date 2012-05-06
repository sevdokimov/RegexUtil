package com.ess.regexutil.gwt.psi.client;

import com.ess.regexutil.gwt.psi.client.lexer.ElementTypes;
import com.ess.regexutil.gwt.psi.client.lexer.IElementType;

/**
 * @author Sergey Evdokimov
 */
public class ErrorPsiElement extends PsiElement {

  private String myMessage;

  public ErrorPsiElement(String message) {
    super(ElementTypes.ERROR_ELEMENT);
    myMessage = message;
  }

  public String getMessage() {
    return myMessage;
  }

  @Override
  public int getLength() {
    return 0;
  }
}
