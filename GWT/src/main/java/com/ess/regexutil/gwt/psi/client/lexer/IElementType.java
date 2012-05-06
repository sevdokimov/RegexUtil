package com.ess.regexutil.gwt.psi.client.lexer;

import com.ess.regexutil.gwt.psi.client.LeafPsiElement;
import com.ess.regexutil.gwt.psi.client.PsiElement;

/**
 * @author Sergey Evdokimov
 */
public class IElementType {

  private static int counter = 1;

  private final int myIndex = counter++;

  private final String myDescription;

  public IElementType(String description) {
    myDescription = description;
  }

  public int getIndex() {
    return myIndex;
  }

  public String getDescription() {
    return myDescription;
  }

  public static int getMaxElementTypeIndex() {
    return counter;
  }

  @Override
  public String toString() {
    return myDescription;
  }
}
