package com.ess.regexutil.ideaplugin;

import com.ess.regexutil.swingcontrols.*;
import com.intellij.openapi.components.*;
import com.intellij.openapi.project.*;
import com.intellij.openapi.util.*;
import com.intellij.openapi.wm.*;
import java.util.*;

import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jdom.*;
import org.jetbrains.annotations.*;

public class RegexUtilComponent implements ProjectComponent, JDOMExternalizable {

    private RegexPanel panel;

    private Project project;

    public RegexUtilComponent(Project project) {
        panel = new RegexPanel();
        this.project = project;
        panel.setRegex("(19|20)\\d\\d([- /.])(0[1-9]|1[012])\\2(0[1-9]|[12][0-9]|3[01])");
        panel.setText("1900-01-01 2007/08/13 1900.01.01 1900 01 01 1900-01.01 1900 13 01 1900 02 31");
    }

    public void initComponent() {
    }

    public void disposeComponent() {
    }

    @NotNull
    public String getComponentName() {
        return "RegexUtilComponent";
    }

    public void projectOpened() {
        ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(project);
        ToolWindow toolWindow = toolWindowManager.registerToolWindow("Regex Tester", true, ToolWindowAnchor.BOTTOM);
        Content content = ContentFactory.SERVICE.getInstance().createContent(panel, "", false);
        toolWindow.getContentManager().addContent(content);
    }

    public void projectClosed() {
        ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(project);
        toolWindowManager.unregisterToolWindow("Regex Tester");
    }

    public void readExternal(Element element) throws InvalidDataException {
        Map<String, String> state = new HashMap<String, String>();
        for (Attribute a : (List<Attribute>)element.getAttributes()) {
            state.put(a.getName(), a.getValue());
        }
        panel.restoreState(state);
    }

    public void writeExternal(Element element) throws WriteExternalException {
        Map<String, String> state = new HashMap<String, String>();
        panel.saveState(state);
        for (Map.Entry<String, String> e : state.entrySet()) {
            element.setAttribute(e.getKey(), e.getValue());
        }
    }
}
