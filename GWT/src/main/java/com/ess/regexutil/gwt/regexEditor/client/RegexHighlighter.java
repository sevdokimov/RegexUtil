package com.ess.regexutil.gwt.regexEditor.client;

import com.ess.regexutil.gwt.psi.client.PsiFile;
import com.ess.regexutil.gwt.psi.client.StyleData;
import com.ess.regexutil.gwt.psi.client.Utils;
import com.ess.regexutil.gwt.psi.client.lexer.Lexer;
import com.ess.regexutil.gwt.psi.client.lexer.TokenSet;
import com.ess.regexutil.gwt.psi.client.parser.PsiBuilderImpl;
import com.ess.regexutil.gwt.psi.client.parser.PsiParser;
import com.ess.regexutil.gwt.regexEditor.client.lexer.RegexCapability;
import com.ess.regexutil.gwt.regexEditor.client.lexer.RegexLexer;
import com.ess.regexutil.gwt.regexEditor.client.parser.RegExpParser;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.user.client.ui.RichTextArea;

import java.util.EnumSet;

/**
 * @author Sergey Evdokimov
 */
public class RegexHighlighter {

  private final RichTextArea myTextArea;

  private String myLastContent = "";
  private PsiFile file = new PsiFile("");

  public RegexHighlighter(RichTextArea textArea) {
    myTextArea = textArea;

    contentMayBeChanged();

    textArea.addKeyDownHandler(new KeyDownHandler() {
      @Override
      public void onKeyDown(KeyDownEvent event) {
        contentMayBeChanged();
      }
    });

    textArea.addKeyUpHandler(new KeyUpHandler() {
      @Override
      public void onKeyUp(KeyUpEvent event) {
        contentMayBeChanged();
      }
    });

    textArea.addKeyPressHandler(new KeyPressHandler() {
      @Override
      public void onKeyPress(KeyPressEvent event) {
        contentMayBeChanged();
      }
    });
  }

  private void contentMayBeChanged() {
    String text = myTextArea.getText();
    text = text.replaceAll("\r", "");

    if (myLastContent.equals(text)) return;

    String s = null;
    try {
      Lexer lexer = new RegexLexer(EnumSet.noneOf(RegexCapability.class));
      PsiBuilderImpl builder = new PsiBuilderImpl(TokenSet.EMPTY, TokenSet.EMPTY, lexer, text);
      PsiParser parser = new RegExpParser();

      file = parser.parse(builder);
      StyleData data = new StyleData(text.length() + 1);
      data.clear(0x00FFFFFF, 0);
      file.highlight(data);

      s = Utils.toHtml(text, data);
    }
    catch (RuntimeException e) {
      e.printStackTrace();
    }

    myLastContent = text;

    myTextArea.setHTML(s);
  }

}
