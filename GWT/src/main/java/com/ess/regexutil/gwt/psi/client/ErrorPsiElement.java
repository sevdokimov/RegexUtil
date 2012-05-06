package com.ess.regexutil.gwt.psi.client;

import com.ess.regexutil.gwt.psi.client.lexer.ElementTypes;

/**
 * @author Sergey Evdokimov
 */
public class ErrorPsiElement extends CompositePsiElement {

  private String myMessage;

  public ErrorPsiElement(String message) {
    super(ElementTypes.ERROR_ELEMENT);
    myMessage = message;
  }

  public String getMessage() {
    return myMessage;
  }
}
