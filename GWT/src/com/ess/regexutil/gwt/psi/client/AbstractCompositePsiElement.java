package com.ess.regexutil.gwt.psi.client;

/**
 * @author Sergey Evdokimov
 */
public abstract class AbstractCompositePsiElement extends PsiElement {

    public AbstractCompositePsiElement(String text, int index) {
        super(text, index);
    }

    @Override
    public int getType() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int getLength() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
