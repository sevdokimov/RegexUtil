package com.ess.regexutil.gwt.regexEditor.client;

import com.ess.regexutil.gwt.psi.client.lexer.ElementTypes;
import com.ess.regexutil.gwt.regexEditor.client.lexer.RegexCapability;
import com.ess.regexutil.gwt.regexEditor.client.lexer.RegexLexer;
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
        lexer.start(regexText);

        StringBuilder text = new StringBuilder();


        text.append("<br><br>");

        try {
          RegExp r = RegExp.compile(regexText);
          text.append("Success");
        }
        catch (Exception e) {
          text.append(e.getMessage());
        }

        textArea.setHTML(text.toString());
      }
    }));

    RootPanel.get("slot1").add(panel);
  }
}
