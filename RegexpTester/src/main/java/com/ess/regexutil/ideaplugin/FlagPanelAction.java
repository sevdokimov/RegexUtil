package com.ess.regexutil.ideaplugin;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.ex.CheckboxAction;
import com.intellij.openapi.actionSystem.ex.ComboBoxAction;
import com.intellij.openapi.project.DumbAware;
import com.intellij.ui.components.panels.NonOpaquePanel;
import com.intellij.ui.scale.JBUIScale;
import com.intellij.util.ui.JBUI;
import kotlin.Triple;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.IntConsumer;
import java.util.regex.Pattern;

public class FlagPanelAction extends ComboBoxAction implements DumbAware {

    private static final List<Triple<String, Character, Integer>> FLAGS = List.of(
            new Triple<>("Case-insensitive", 'i', Pattern.CASE_INSENSITIVE),
            new Triple<>("Dotall", 's', Pattern.DOTALL),
            new Triple<>("Multiline", 'm', Pattern.MULTILINE),

            new Triple<>("Unix lines", 'd', Pattern.UNIX_LINES),
            new Triple<>("Whitespace and comments", 'x', Pattern.COMMENTS),
            new Triple<>("Unicode-aware case", 'u', Pattern.UNICODE_CASE),
            new Triple<>("Canonical equivalence", null, Pattern.CANON_EQ),
            new Triple<>("Unicode character classes", 'U', Pattern.UNICODE_CHARACTER_CLASS)
    );

    private int flags = 0;

    private final List<IntConsumer> flagsListeners = new CopyOnWriteArrayList<>();

    public FlagPanelAction() {

    }

    @NotNull
    @Override
    public JComponent createCustomComponent(@NotNull final Presentation presentation, @NotNull String place) {
        ComboBoxButton button = new FlagButton(presentation);
        NonOpaquePanel panel = new NonOpaquePanel(new BorderLayout());
        Border border = JBUI.Borders.emptyLeft(3);

        panel.setBorder(border);
        panel.add(button);
        return panel;
    }

    public void addListener(@NotNull IntConsumer listener) {
        flagsListeners.add(listener);
    }

    public void removeListener(@NotNull IntConsumer listener) {
        flagsListeners.remove(listener);
    }

    private void notifyFlagChanged() {
        for (IntConsumer listener : flagsListeners) {
            listener.accept(flags);
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Presentation presentation = e.getPresentation();

        presentation.setDescription("Flags used in the regular expression");

        StringBuilder html = new StringBuilder();
        html.append("<html><body>Flags ");

        if (flags == 0) {
            html.append("<i>none</i>");
        } else {
            html.append("(<b>");

            for (Triple<String, Character, Integer> flag : FLAGS) {
                if ((flags & flag.getThird()) != 0) {
                    if (flag.getSecond() != null) {
                        html.append(flag.getSecond());
                    } else {
                        html.append(flag.getFirst().charAt(0));
                    }
                }
            }

            html.append("</b>)");
        }

        html.append("</body></html>");
        
        presentation.setText(html.toString());
    }

    @Override
    protected @NotNull DefaultActionGroup createPopupActionGroup(JComponent button) {
        final DefaultActionGroup allActionsGroup = new DefaultActionGroup();

        for (Triple<String, Character, Integer> flag : FLAGS) {
            Integer flagCode = flag.getThird();

            String title = flag.getFirst();
            if (flag.getSecond() != null)
                title += " (?" + flag.getSecond() + ')';

            allActionsGroup.add(new FlagAction(title, flagCode));
        }

        return allActionsGroup;
    }

    public int getFlags() {
        return flags;
    }

    public void setFlags(int flags) {
        if (this.flags != flags) {
            this.flags = flags;
            notifyFlagChanged();
        }
    }

    public class FlagButton extends ComboBoxButton {

        public FlagButton(@NotNull Presentation presentation) {
            super(presentation);
        }

        @Override
        public Dimension getPreferredSize() {
            Dimension d = super.getPreferredSize();
            d.width = Math.max(d.width, JBUIScale.scale(145));
            return d;
        }
    }

    private class FlagAction extends CheckboxAction implements DumbAware {
        private final Integer flagCode;

        public FlagAction(String title, Integer flagCode) {
            super(title);
            this.flagCode = flagCode;
        }

        @Override
        public boolean isSelected(@NotNull AnActionEvent e) {
            return (flags & flagCode) != 0;
        }

        @Override
        public void setSelected(@NotNull AnActionEvent e, boolean state) {
            int oldState = flags;

            if (state) {
                flags |= flagCode;
            } else {
                flags &= ~flagCode;
            }

            if (oldState != flags)
                notifyFlagChanged();
        }
    }
}
