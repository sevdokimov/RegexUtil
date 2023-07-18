package com.ess.regexutil.ideaplugin;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

public class RegexpToolWindowFactory implements ToolWindowFactory, DumbAware {

    public static final String TOOL_WINDOW_ID = "RegexTester";

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        toolWindow.setTitle("Regex Tester");

        RegexpTesterPanel panel = new RegexpTesterPanel(project);

        project.getService(RegexPanelStateService.class).restoreState(panel);
        
        Content content = ApplicationManager.getApplication().getService(ContentFactory.class).createContent(panel, "", false);

        Disposer.register(content, panel);

        toolWindow.getContentManager().addContent(content);
    }
}
