package com.ess.regexutil.ideaplugin;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ex.ToolWindowManagerEx;
import com.intellij.ui.content.Content;
import com.intellij.util.xmlb.annotations.Attribute;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@State(
        name = "regexState",
        storages = @Storage(StoragePathMacros.CACHE_FILE)
)
public class RegexPanelStateService implements PersistentStateComponent<RegexPanelStateService.State> {

    private final Project project;

    private State state;

    public void restoreState(RegexpTesterPanel panel) {
        ApplicationManager.getApplication().assertIsDispatchThread();

        if (state == null)
            return;

        panel.loadState(state);

        state = null;
    }


    public RegexPanelStateService(Project project) {
        this.project = project;
    }

    @Nullable
    public RegexPanelStateService.State getState() {
        if (state != null)
            return state;

        ToolWindow toolWindow = ToolWindowManagerEx.getInstanceEx(project).getToolWindow(RegexpToolWindowFactory.TOOL_WINDOW_ID);
        if (toolWindow == null)
            return null;

        if (toolWindow.getContentManager().getContentCount() != 1)
            return null;

        Content content = toolWindow.getContentManager().getContent(0);
        RegexpTesterPanel panel = (RegexpTesterPanel) content.getComponent();

        State res = new State();
        panel.saveState(res);

        return res;
    }

    public void loadState(@NotNull RegexPanelStateService.State state) {
        this.state = state;
    }

    @Override
    public void noStateLoaded() {
        state = new State();
        state.regexp = "(19|20)\\d\\d([- /.])(0[1-9]|1[012])\\2(0[1-9]|[12][0-9]|3[01])";
        state.text = "1900-01-01 2007/08/13 1900.01.01 1900 01 01 1900-01.01 1900 13 01 1900 02 31";
    }

    public static class State {
        @Attribute
        private String regexp;
        @Attribute
        private String text;
        @Attribute
        private int flags;
        @Attribute
        private RegexpTesterPanel.MatchType matchType;

        public String getRegexp() {
            return unescape(regexp);
        }

        public void setRegexp(String regexp) {
            this.regexp = escape(regexp);
        }

        public String getText() {
            return unescape(text);
        }

        public void setText(String text) {
            this.text = escape(text);
        }

        public int getFlags() {
            return flags;
        }

        public void setFlags(int flags) {
            this.flags = flags;
        }

        public RegexpTesterPanel.MatchType getMatchType() {
            return matchType;
        }

        public void setMatchType(RegexpTesterPanel.MatchType matchType) {
            this.matchType = matchType;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof State)) return false;
            State state = (State) o;
            return flags == state.flags && Objects.equals(regexp, state.regexp) && Objects.equals(text, state.text) && matchType == state.matchType;
        }

        @Override
        public int hashCode() {
            return Objects.hash(regexp);
        }

        static String escape(@Nullable String s) {
            if (s == null)
                return null;

            return s.replace("|", "||").replace("$", "|d");
        }

        static String unescape(String s) {
            if (s == null)
                return null;

            Matcher matcher = Pattern.compile("\\|[d|]").matcher(s);
            return matcher.replaceAll(r -> {
                char c = s.charAt(r.end() - 1);
                if (c == 'd')
                    return "\\$";

                assert c == '|';
                return "|";
            });
        }
    }
}
