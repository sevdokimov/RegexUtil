package com.ess.regexutil.ideaplugin;

import com.ess.regexutil.swingcontrols.RegexPanel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;

import javax.swing.*;
import java.util.List;

public class RegexpToolWindowFactory implements ToolWindowFactory {
    public void createToolWindowContent(Project project, ToolWindow toolWindow) {
        RegexPanel panel = new RegexPanel();

        List<JTextPane> editors = panel.getEditors();
        for(JTextPane editor : editors){
            editor.setBackground(panel.getBackground().brighter());
            editor.setForeground(panel.getForeground().darker());
            editor.setFont(panel.getFont());
        }

        RegexpStateService.getInstance(project).setPanel(panel);
        
        Content content = ContentFactory.SERVICE.getInstance().createContent(panel, "", false);
        toolWindow.getContentManager().addContent(content);
    }
}
