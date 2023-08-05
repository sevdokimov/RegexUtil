package com.ess.regexutil.ideaplugin.utils;

import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class DocumentChangeListener implements DocumentListener {

    private final Consumer<DocumentEvent> onChange;

    public DocumentChangeListener(@NotNull Consumer<DocumentEvent> onChange) {
        this.onChange = onChange;
    }

    @Override
    public void documentChanged(@NotNull DocumentEvent event) {
        onChange.accept(event);
    }
}
