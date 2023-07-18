package com.ess.regexutil.ideaplugin.utils;

import com.intellij.openapi.util.NlsContexts;
import com.intellij.ui.components.JBLabel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.TestOnly;

import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.util.function.Consumer;

public class JBLableHyprlink extends JBLabel {

    private final Consumer<HyperlinkEvent> listener;

    public JBLableHyprlink(@NotNull @NlsContexts.Label String text, Consumer<HyperlinkEvent> listener) {
        super(text);
        this.listener = listener;
        setCopyable(true);
    }

    @Override
    protected @NotNull HyperlinkListener createHyperlinkListener() {
        return e -> {
            if (e.getEventType() != HyperlinkEvent.EventType.ACTIVATED)
                return;

            listener.accept(e);
        };
    }

    @TestOnly
    public Consumer<HyperlinkEvent> getListener() {
        return listener;
    }
}
