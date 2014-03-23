package com.ess.regexutil.ideaplugin;

import com.ess.regexutil.swingcontrols.RegexPanel;
import com.intellij.openapi.components.*;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

@State(
        name = "regexState",
        storages = @Storage( file = StoragePathMacros.WORKSPACE_FILE)
)
public class RegexpStateService implements PersistentStateComponent<RegexpStateService.State> {

    private final State stateHolder = new State();
    private final Map<String, String> state = stateHolder.map;
    
    private RegexPanel panel;
    
    public RegexpStateService() {
        state.put(RegexPanel.STATE_REGEX, "(19|20)\\d\\d([- /.])(0[1-9]|1[012])\\2(0[1-9]|[12][0-9]|3[01])");
        state.put("text", "1900-01-01 2007/08/13 1900.01.01 1900 01 01 1900-01.01 1900 13 01 1900 02 31");
    }

    public static RegexpStateService getInstance(@NotNull Project project) {
        return ServiceManager.getService(project, RegexpStateService.class);
    }
    
    @Nullable
    public RegexPanel getPanel() {
        return panel;
    }

    public void setPanel(@Nullable RegexPanel panel) {
        this.panel = panel;
        if (panel != null) {
            panel.restoreState(state);
        }
    }
    
    @Nullable
    public RegexpStateService.State getState() {
        if (panel != null) {
            panel.saveState(state);
        }
        return stateHolder;
    }

    public void loadState(RegexpStateService.State state) {
        Map<String, String> tmp = new HashMap<String, String>(state.map);
        
        this.state.clear(); // cleaning of this.state may clean state , so we save state to tmp.
        this.state.putAll(tmp);

        if (panel != null) {
            panel.restoreState(tmp);
        }
    }

    public static class State {
        public Map<String, String> map = new HashMap<String, String>();
    }
}
