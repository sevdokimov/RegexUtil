package com.ess.regexutil.swingcontrols;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

public class DefaultCopyPasteAdapter implements CopyPasteAdapter {

    private static final DefaultCopyPasteAdapter instance = new DefaultCopyPasteAdapter();

    public static DefaultCopyPasteAdapter getInstance() {
        return instance;
    }


    public void toClipbord(String text) {
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(text), null);
    }

    public String fromClipbord() {
        try {
            return (String)Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
        } catch (IOException e) {
            return "";
        } catch (UnsupportedFlavorException e) {
            throw new InternalError();
        }
    }
}
