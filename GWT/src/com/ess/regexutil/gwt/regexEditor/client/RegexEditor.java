package com.ess.regexutil.gwt.regexEditor.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;

/**
 * @author Sergey Evdokimov
 */
public class RegexEditor implements EntryPoint {
    public void onModuleLoad() {
        final RichTextArea textArea = new RichTextArea();
        textArea.setHTML("as<b>dada!!</b>");

        VerticalPanel panel = new VerticalPanel();

        panel.add(textArea);

        panel.add(new Button("Get Html", new ClickHandler() {
            public void onClick(ClickEvent event) {
                Window.alert(textArea.getHTML());
            }
        }));

        panel.add(new Button("red", new ClickHandler() {
            public void onClick(ClickEvent event) {
                RichTextArea.Formatter formatter = textArea.getFormatter();
                formatter.setForeColor("red");
            }
        }));

        RootPanel.get("slot1").add(panel);

    }
}
