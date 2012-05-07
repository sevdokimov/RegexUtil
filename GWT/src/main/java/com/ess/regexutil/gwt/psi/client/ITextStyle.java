package com.ess.regexutil.gwt.psi.client;

public interface ITextStyle {

  Integer getBackground();

  Boolean getBold();

  Integer getForeground();

  Boolean getItalic();

  void apply(StyleData sd, int start, int end);
}