package com.ess.regexutil.gwt.psi.client;

import com.ess.regexutil.gwt.psi.client.lexer.IElementType;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Sergey Evdokimov
 */
public abstract class PsiElement {

  protected static int counter = 1;

  protected PsiElement parent;
  protected PsiElement next;
  protected PsiElement prev;

  private int cachedIndex = -1;

  protected final IElementType type;

  protected PsiElement(IElementType type) {
    this.type = type;
  }

  public int getIndex() {
    if (cachedIndex == -1) {
      int res = parent == null ? 0 : parent.getIndex();

      if (prev != null) {
        res += prev.getIndex() + prev.getLength();
      }

      cachedIndex = res;
    }
    return cachedIndex;
  }

  public IElementType getElementType() {
    return type;
  }

  public abstract int getLength();

  public PsiElement getNext() {
    return next;
  }

  public PsiElement getPrev() {
    return prev;
  }

  public void setPrev(PsiElement prev) {
    assert this.prev == null;
    this.prev = prev;
  }

  public void setNext(PsiElement next) {
    assert this.next == null;
    this.next = next;
  }

  public PsiElement getParent() {
    return parent;
  }

  public void setParent(PsiElement parent) {
    this.parent = parent;
  }

  public PsiElement getFirstChild() {
    return null;
  }

  public PsiElement getLastChild() {
    return null;
  }

  public List<PsiElement> getChildren() {
    List<PsiElement> res = new ArrayList<PsiElement>();

    for (PsiElement e = getFirstChild(); e != null; e = e.getNext()) {
      res.add(e);
    }

    return res;
  }

  @Override
  public String toString() {
    if (cachedIndex == -1) {
      return type.toString();
    }

    return type.toString() + " " + cachedIndex;
  }
}
