package com.ess.regexutil.gwt.psi.client;

import java.util.Arrays;

public class StyleData {

  public final boolean[] italic;
  public final boolean[] bold;

  public final int[] foreground;
  public final int[] background;

  public StyleData(int length) {
    italic = new boolean[length];
    bold = new boolean[length];
    foreground = new int[length];
    background = new int[length];
  }

  private int getLength() {
    return italic.length;
  }

  public boolean isEquals(int i1, int i2) {
    return (foreground[i1] == foreground[i2])
           && (background[i1] == background[i2])
           && bold[i1] == bold[i2]
           && italic[i1] == italic[i2];
  }

  public void clear(int defaultBackground, int defaultForeground) {
    clear(getLength(), defaultBackground, defaultForeground);
  }

  public void clear(int length, int defaultBackground, int defaultForeground) {
    Arrays.fill(italic, 0, length, false);
    Arrays.fill(bold, 0, length, false);
    Arrays.fill(foreground, 0, length, defaultForeground);
    Arrays.fill(background, 0, length, defaultBackground);
  }

  public boolean isEquals(StyleData sd, int start, int end) {
    for (int i = start; i < end; i++) {
      if (
        sd.foreground[i] != foreground[i]
        || sd.background[i] != background[i]
        || sd.bold[i] != bold[i]
        || sd.italic[i] != italic[i]
        ) {
        return false;
      }
    }
    return true;
  }
}
