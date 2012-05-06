package com.ess.regexutil.gwt.psi.client;

/**
 * @author Sergey Evdokimov
 */
public class Utils {

  public static String toString(PsiElement element) {
    StringBuilder sb = new StringBuilder();
    toString(sb, 0, element);
    return sb.toString();
  }

  private static void toString(StringBuilder sb, int align, PsiElement element) {
    for (int i = 0; i < align; i++) {
      sb.append(' ');
    }

    sb.append(element.getElementType().getDescription()).append('\n');

    for (PsiElement e = element.getFirstChild(); e != null; e = e.getNext()) {
      toString(sb, align + 2, e);
    }
  }

}
