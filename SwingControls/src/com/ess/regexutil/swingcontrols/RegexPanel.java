package com.ess.regexutil.swingcontrols;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.ess.regexutil.controls.RegexHighlighter;
import com.ess.regexutil.escaping.JavaRegexTransfer;
import com.ess.regexutil.escaping.JavaScriptTransfer;
import com.ess.regexutil.escaping.RegexTransfer;
import com.ess.regexutil.escaping.XmlRegexTransfer;
import com.ess.regexutil.regexparser.Flags;
import com.ess.regexutil.swingadapter.SwingAdapter;
import com.ess.util.RegexConsts;
import com.ess.util.EventListener;

public class RegexPanel extends JPanel {

    public static final String STATE_REGEX = "regex";
    public static final String STATE_MODE = "mode";

    private CopyPasteAdapter copyPasteAdapter = DefaultCopyPasteAdapter.getInstance();

    private Flags flags;
	
	private RegexResultPanel currentSelect;
	
	private SwingAdapter regexAdapter;
	
	private List<Action> actions;

    private JTabbedPane tab;

    private static class Mode {
        public final String title;
        public final String tooltip;
        public final RegexResultPanel panel;

        public Mode(String title, RegexResultPanel panel, String tooltip) {
            this.title = title;
            this.panel = panel;
            this.tooltip = tooltip;
        }
    }

    public RegexPanel() {
		super(new CardLayout(4, 4));


        editorPanels = new ArrayList<JTextPane>();

        // Create regex panel
        regexAdapter = new SwingAdapter(false);
		JTextPane regexEditor = regexAdapter.getEditor();
		regexEditor.setFont(new Font("MonoSpaced", Font.PLAIN, 13));

		flags = new Flags();
		RegexHighlighter highlighter = new RegexHighlighter(regexAdapter, flags);
		
		createActions();
		
		final JPopupMenu popupMenu = new JPopupMenu();
		for (Action action : actions) {
			popupMenu.add(new JMenuItem(action));
		}
		regexEditor.addMouseListener(new MouseAdapter(){
			@Override
			public void mouseClicked(MouseEvent e) {
				if (SwingUtilities.isRightMouseButton(e) && e.getClickCount() == 1) {
					popupMenu.show(regexAdapter.getEditor(), e.getX(), e.getY());
				}
			}
		});

        JPanel regexPanel = new JPanel(new BorderLayout());
        regexPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 3, 0));
        regexPanel.add(createFlagPanel(), BorderLayout.PAGE_START);
        JScrollPane regexScrollPane = new JScrollPane(regexEditor);
        regexScrollPane.setPreferredSize(new Dimension(regexScrollPane.getPreferredSize().width, 50));
        regexPanel.add(regexScrollPane, BorderLayout.CENTER);

        FindAndMatchPanel findPanel = new FindAndMatchPanel(highlighter, false);
        editorPanels.add(findPanel.getEditor());

        FindAndMatchPanel matchPanel = new FindAndMatchPanel(highlighter, true);
        editorPanels.add(matchPanel.getEditor());

        SplitPanel splitPanel = new SplitPanel(highlighter);
        editorPanels.add(splitPanel.getEditor());
        editorPanels.add(splitPanel.getResultEditor());

        ReplacePanel replacePanel = new ReplacePanel(highlighter);
        editorPanels.add(replacePanel.getEditor());
        editorPanels.add(replacePanel.getResultEditor());
        editorPanels.add(replacePanel.getReplacementEditor());

        // Create Text tab
        Mode[] modes = new Mode[]{
                new Mode("Find", findPanel, "Find all subsequence in text."),
                new Mode("Match", matchPanel, "Attempts to match the text against the regexp."),
                new Mode("Split", splitPanel, null),
                new Mode("Replace", replacePanel, null),
        };

        tab = new JTabbedPane();
        tab.setMinimumSize(new Dimension(200, 70));
        for (int i = 0; i < modes.length; i++) {
            Mode mode = modes[i];
            tab.add(mode.title, mode.panel);
            tab.setToolTipTextAt(i, mode.tooltip);
        }
        tab.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				setSelected((RegexResultPanel)tab.getSelectedComponent());
			}
		});

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        split.setContinuousLayout(true);
        split.setTopComponent(regexPanel);
        split.setBottomComponent(tab);
        split.setBorder(null);

        add(split, "split");
        
        setSelected((RegexResultPanel)tab.getSelectedComponent());
        regexEditor.requestFocus();
    }

    public void setCopyPasteAdapter(CopyPasteAdapter copyPasteAdapter) {
        this.copyPasteAdapter = copyPasteAdapter;
    }

    public List<JTextPane> getEditors() {
        return editorPanels;
    }

    private void addCopyPasteActions(final RegexTransfer transfer, String copyText, String pasteText) {
		AbstractAction copyAction = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				String text = regexAdapter.getText();
				if (regexAdapter.getSelStart() != regexAdapter.getSelEnd()) {
					text = text.substring(regexAdapter.getSelStart(), regexAdapter.getSelEnd());
				}
                text = transfer.regexToSource(text);
                copyPasteAdapter.toClipbord(text);
			}
		};
		copyAction.putValue(Action.NAME, copyText);

		AbstractAction pasteAction = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				String text = copyPasteAdapter.fromClipbord();
				if (text != null) {
					text = transfer.sourceToRegex(text);
                    int selStart = regexAdapter.getEditor().getSelectionStart();
                    int selEnd = regexAdapter.getEditor().getSelectionEnd();
                    String oldText = regexAdapter.getText();
					regexAdapter.setText(oldText.substring(0, selStart) + text + oldText.substring(selEnd));
					regexAdapter.getEditor().setCaretPosition(selStart + text.length());
				}
			}
		};
		pasteAction.putValue(Action.NAME, pasteText);
		
		actions.add(copyAction);
		actions.add(pasteAction);
	}
	
	private void createActions() {
		actions = new ArrayList<Action>();
		addCopyPasteActions(JavaRegexTransfer.instance, "Copy regex for Java-Source (escape '\\')", "Paste regex from Java-String (unescape '\\')");
		addCopyPasteActions(XmlRegexTransfer.instance, "Copy regex for XML (escape '>', '&', ...)", "Paste regex from XML (unescape '>', '&', ...)");
		addCopyPasteActions(JavaScriptTransfer.instance, "Copy regex for JavaScript", "Paste regex from JavaScript");
	}
	
	public List<Action> getActions() {
		return actions;
	}

    private void setSelected(RegexResultPanel sel) {
		if (currentSelect != null) {
			currentSelect.lostFocus();
			sel.setText(currentSelect.getText());
		}
			
		currentSelect = sel;
		sel.setFocus();
	}
	
	public Flags getFlags() {
		return flags;
	}
	
	public void setRegex(String regex) {
		regexAdapter.setText(regex);
	}

    public void setText(String text) {
        currentSelect.setText(text);
    }

    public String getText() {
        return currentSelect.getText();
    }

    public String getRegex() {
        return regexAdapter.getText();
    }

    public int getMode() {
        return tab.getSelectedIndex();
    }
    
    public void setMode(int mode) {
        tab.setSelectedIndex(mode);
    }

    public void saveState(Map<String, String> res) {
        res.put(STATE_MODE, String.valueOf(tab.getSelectedIndex()));
        res.put(STATE_REGEX, regexAdapter.getText());
        res.put("flags", String.valueOf(flags.getFlags()));
        currentSelect.saveState(res);
    }

    public void restoreState(Map<String, String> state) {
        String regex = state.get("regex");
        if (regex == null)
            regex = "";
        regexAdapter.setText(regex);

        String sFlags = state.get("flags");
        int flags = sFlags == null ? 0 : Integer.parseInt(sFlags);
        this.flags.setFlags(flags);

        String sMode = state.get("mode");
        if (sMode != null) {
            tab.setSelectedIndex(Integer.parseInt(sMode));
        }
        currentSelect.restoreState(state);
    }

    public JMenuItem[] createFlagsMenuItems() {
        JMenuItem[] items = new JMenuItem[RegexConsts.flagsData.length];
        for (int i = 0; i < RegexConsts.flagsData.length; i++) {
            assert RegexConsts.flagsData[i].length == 2;

            final JMenuItem item = new JCheckBoxMenuItem();
            final int flag = (Integer) RegexConsts.flagsData[i][0];
            item.setText((String) RegexConsts.flagsData[i][1]);
            item.setSelected(flags.isFlag(flag));
            item.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    flags.setFlag(flag, item.isSelected());
                }
            });
            flags.changeListeners.addListener(new EventListener<Integer>() {
                public void notify(Integer event) {
                    boolean sel = flags.isFlag(flag);
                    if (sel != item.isSelected())
                        item.setSelected(sel);
                }
            });
            items[i] = item;
        }
        return items;
    }

    private JPanel createFlagPanel() {
        JPanel flagsPanel = new JPanel();
        flagsPanel.setLayout(new BoxLayout(flagsPanel, BoxLayout.X_AXIS));
        JLabel regexTitleLabel = new JLabel("Regular expression");
        regexTitleLabel.setVerticalAlignment(JLabel.BOTTOM);
        flagsPanel.add(regexTitleLabel);
        flagsPanel.add(Box.createHorizontalGlue());
        flagsPanel.add(createFlagCheckbox(Pattern.CASE_INSENSITIVE, "Case-insensitive (?i)","" +
                "<html>" +
                "<h4>Enables case-insensitive matching.</h4>" +
                "By default, case-insensitive matching assumes that only characters in the US-ASCII charset are being matched.<br>" +
                "Unicode-aware case-insensitive matching can be enabled by specifying the <i>Unicode Case</i> flag in conjunction with this flag.<br>" +
                "<br>Case-insensitive matching can also be enabled via the embedded flag expression&nbsp;\"(?i)\"." +
                "</html>"));

        flagsPanel.add(createFlagCheckbox(Pattern.MULTILINE, "Multiline (?m)", "" +
                "<html>" +
                "<h4>Enables multiline mode.</h4>" +
                "In multiline mode the expressions \"^\" and \"$\" match just after or just before, respectively, a line<br>" +
                "terminator or the end of the input sequence.  By default these expressions only match at the beginning<br>" +
                "and the end of the entire input sequence.<br>" +
                "<br>Multiline mode can also be enabled via the embedded flag expression&nbsp;\"(?m)\"" +
                "</html>"));
        flagsPanel.add(createFlagCheckbox(Pattern.DOTALL, "Dot All Mode (?s)", "" +
                "<html>" +
                "<h4>Enables dotall mode.</h4>" +
                "In dotall mode, the expression \".\" matches any character, including a line terminator.<br>" +
                "By default this expression does not match line terminators.<br>" +
                "<br> Dotall mode can also be enabled via the embedded flag expression&nbsp;\"(?s)\".<br>" +
                "(The \"s\" is a mnemonic for \"single-line\" mode, which is what this is called in Perl.)" +
                "</html>"));

        final JButton allFlagsBtn = new JButton("...");
        allFlagsBtn.setToolTipText("Show all regexp flags");
        allFlagsBtn.setPreferredSize(new Dimension(30, 16));
        JPanel ppp = new JPanel();
        ppp.add(allFlagsBtn);
        ppp.setMaximumSize(new Dimension(50, 1000));

        final JPopupMenu flagsPopupMenu = new JPopupMenu();
        for (JMenuItem item : createFlagsMenuItems()) {
            flagsPopupMenu.add(item);
        }
        allFlagsBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Dimension size = flagsPopupMenu.getPreferredSize();
                flagsPopupMenu.show(allFlagsBtn, allFlagsBtn.getWidth() - size.width, allFlagsBtn.getHeight());
            }
        });

        flagsPanel.add(ppp);
        return flagsPanel;
    }

    private JCheckBox createFlagCheckbox(final int flag, String text, String tooltip) {
        final JCheckBox res = new JCheckBox(text, flags.isFlag(flag));
        res.setToolTipText(tooltip);
        res.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                flags.setFlag(flag, res.isSelected());
            }
        });

        flags.changeListeners.addListener(new EventListener<Integer>() {
            public void notify(Integer event) {
                boolean sel = flags.isFlag(flag);
                if (sel != res.isSelected())
                    res.setSelected(sel);
            }
        });
        return res;
    }

}
