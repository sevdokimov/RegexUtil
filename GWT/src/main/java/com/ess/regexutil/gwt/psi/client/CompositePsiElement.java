package com.ess.regexutil.gwt.psi.client;

import com.ess.regexutil.gwt.psi.client.lexer.IElementType;

/**
 * @author Sergey Evdokimov
 */
public class CompositePsiElement extends PsiElement {

  private PsiElement firstChild;
  private PsiElement lastChild;

  private int cachedLength = -1;

  public CompositePsiElement(IElementType type) {
    super(type);
  }

  @Override
  public int getLength() {
    if (cachedLength == -1) {
      int res = 0;

      for (PsiElement e = firstChild; e != null; e = e.next) {
        res += e.getLength();
      }

      cachedLength = res;
    }

    return cachedLength;
  }

  public void addElement(PsiElement element) {
    element.parent = this;
    element.prev = lastChild;
    element.next = null;

    if (lastChild != null) {
      lastChild.next = element;
    }
    else {
      firstChild = element;
    }

    lastChild = element;
  }

  @Override
  public PsiElement getFirstChild() {
    return firstChild;
  }

  @Override
  public PsiElement getLastChild() {
    return lastChild;
  }

  @Override
  public void highlight(StyleData styleData) {
    for (PsiElement e = getFirstChild(); e != null; e = e.getNext()) {
      e.highlight(styleData);
    }
  }
}
