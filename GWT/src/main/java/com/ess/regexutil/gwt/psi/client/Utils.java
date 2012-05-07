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

  private static void appendColor(StringBuilder sb, int color) {
    String hex = Integer.toHexString(color);
    for (int i = hex.length(); i < 6; i++) {
      sb.append('0');
    }
    sb.append(hex);
  }

  public static String toHtml(String text, StyleData data) {
    StringBuilder sb = new StringBuilder();

    int length = text.length();

    int i = 0;

    while (i < length) {
      int k;
      for (k = i + 1; k < length && data.isEquals(i, k); k++) {
      }

      boolean tagExists = false;

      if (data.background[i] != 0x00FFFFFF) {
        if (!tagExists) {
          sb.append("<font style=\"");
          tagExists = true;
        }

        sb.append("background: #");
        appendColor(sb, data.background[i]);
        sb.append(';');
      }

      if (data.foreground[i] != 0x00FFFFFF) {
        if (!tagExists) {
          sb.append("<font style=\"");
          tagExists = true;
        }

        sb.append("color: #");
        appendColor(sb, data.foreground[i]);
        sb.append(';');
      }

      if (data.bold[i]) {
        if (!tagExists) {
          sb.append("<font style=\"");
          tagExists = true;
        }

        sb.append("font-weight: bold;");
      }

      if (data.italic[i]) {
        if (!tagExists) {
          sb.append("<font style=\"");
          tagExists = true;
        }

        sb.append("font-style: italic;");
      }

      if (tagExists) {
        sb.append("\">").append(text, i, k).append("</font>");
      }
      else {
        sb.append(text, i, k);
      }

      i = k;
    }

    return sb.toString();
  }
}
