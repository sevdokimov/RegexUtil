package com.ess.regexutil.gwt.psi.client;

/**
 * @author Sergey Evdokimov
 */
public abstract class PsiElement {

    private PsiElement parent;
    private PsiElement next;
    private PsiElement prev;

    private final int index;
    private final String text;

    public PsiElement(String text, int index) {
        this.text = text;
        this.index = index;
    }

    public String getText() {
        return text;
    }

    public int getIndex() {
        return index;
    }

    public abstract int getType();
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
}
