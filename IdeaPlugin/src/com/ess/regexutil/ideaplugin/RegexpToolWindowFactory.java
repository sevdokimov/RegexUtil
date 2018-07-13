package com.ess.regexutil.ideaplugin;

import com.ess.regexutil.regexparser.RegexConfig;
import com.ess.regexutil.swingcontrols.RegexPanel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.openapi.wm.impl.ToolWindowImpl;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;

import javax.swing.*;
import java.util.List;
import java.awt.*;

public class RegexpToolWindowFactory implements ToolWindowFactory {
    public void createToolWindowContent(Project project, ToolWindow toolWindow) {

        boolean isDarkTheme = isColorDark(((ToolWindowImpl) toolWindow).getContentUI().getBackground());

        if (isDarkTheme) {
            RegexConfig.setConfig(new RegexConfig.Dark());
        }

        RegexPanel panel = new RegexPanel();

        if (isDarkTheme) {
            List<JTextPane> editors = panel.getEditors();
            for (JTextPane editor : editors) {
                editor.setBackground(panel.getBackground().darker());
                editor.setForeground(panel.getForeground().darker());
                editor.setFont(panel.getFont());
            }
        }

        RegexpStateService.getInstance(project).setPanel(panel);

        Content content = ContentFactory.SERVICE.getInstance().createContent(panel, "", false);
        toolWindow.getContentManager().addContent(content);
    }

    private boolean isColorDark(Color cl) {
        double darkness = 1 - (0.299 * cl.getRed() + 0.587 * cl.getGreen() + 0.114 * cl.getBlue()) / 255;
        return (darkness < 0.5);
    }
}
