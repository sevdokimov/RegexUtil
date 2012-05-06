package com.ess.regexutil.gwt.regexEditor.client;

import com.ess.regexutil.gwt.psi.client.PsiFile;
import com.ess.regexutil.gwt.psi.client.Utils;
import com.ess.regexutil.gwt.psi.client.lexer.ElementTypes;
import com.ess.regexutil.gwt.psi.client.lexer.TokenSet;
import com.ess.regexutil.gwt.psi.client.parser.PsiBuilderImpl;
import com.ess.regexutil.gwt.psi.client.parser.PsiParser;
import com.ess.regexutil.gwt.regexEditor.client.lexer.RegexCapability;
import com.ess.regexutil.gwt.regexEditor.client.lexer.RegexLexer;
import com.ess.regexutil.gwt.regexEditor.client.parser.RegExpParser;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.ui.*;

import java.util.EnumSet;

/**
 * @author Sergey Evdokimov
 */
public class RegexEditor implements EntryPoint {
  public void onModuleLoad() {
    VerticalPanel panel = new VerticalPanel();

    final TextBox regex = new TextBox();
    panel.add(regex);

    final RichTextArea textArea = new RichTextArea();
    panel.add(textArea);

    panel.add(new Button("Parse", new ClickHandler() {
      public void onClick(ClickEvent event) {
        String regexText = regex.getText();

        RegexLexer lexer = new RegexLexer(EnumSet.of(RegexCapability.NESTED_CHARACTER_CLASSES));

        PsiBuilderImpl builder = new PsiBuilderImpl(TokenSet.EMPTY, TokenSet.EMPTY, lexer, regexText);
        PsiParser parser = new RegExpParser();

        PsiFile file = parser.parse(builder);

        String text = Utils.toString(file);

        try {
          RegExp r = RegExp.compile(regexText);
          text += "Success";
        }
        catch (Exception e) {
          text += e.getMessage();
        }

        textArea.setText(text);
      }
    }));

    RootPanel.get("slot1").add(panel);
  }
}
