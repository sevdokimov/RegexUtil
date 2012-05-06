package com.ess.regexutil.gwt.psi.client;

/**
 * @author Sergey Evdokimov
 */
public abstract class PsiElement {

  protected static int counter = 1;

  protected PsiElement parent;
  protected PsiElement next;
  protected PsiElement prev;

  private int cachedIndex = -1;

  protected final int type;

  protected PsiElement(int type) {
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

  public int getType() {
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
}
