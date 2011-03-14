package com.ess.regexutil.applet;

import com.ess.regexutil.swingcontrols.CopyPasteAdapter;

import javax.swing.*;

public class AppletCopyPasteAdapter implements CopyPasteAdapter {

    private JPanel panel;

    public AppletCopyPasteAdapter(JPanel panel) {
        this.panel = panel;
    }

    public void toClipbord(String text) {
        CopyDialog.getInstance().show(panel, text);
    }

    public String fromClipbord() {
        return PasteDialog.getInstance().show(panel);
    }
}
