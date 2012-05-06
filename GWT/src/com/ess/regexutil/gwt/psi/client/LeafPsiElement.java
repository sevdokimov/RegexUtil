package com.ess.regexutil.gwt.psi.client;

/**
 * @author Sergey Evdokimov
 */
public class LeafPsiElement extends PsiElement {

    private final int type;
    private final int length;

    public LeafPsiElement(String text, int index, int length, int type) {
        super(text, index);
        this.length = length;
        this.type = type;
    }

    @Override
    public int getType() {
        return type;
    }

    @Override
    public int getLength() {
        return length;
    }
}
