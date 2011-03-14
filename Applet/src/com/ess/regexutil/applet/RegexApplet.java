package com.ess.regexutil.applet;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.ess.regexutil.regexparser.Flags;
import com.ess.regexutil.swingcontrols.RegexPanel;
import com.ess.util.RegexConsts;
import com.ess.util.UIThreadUtil;

public class RegexApplet extends JApplet {

	private RegexPanel panel;
	
	@Override
	public void init() {
		SwingUtilities.invokeLater(new Runnable(){
			public void run() {
				panel = new RegexPanel();
                panel.setCopyPasteAdapter(new AppletCopyPasteAdapter(panel));
                setContentPane(panel);

				JMenuBar menuBar = new JMenuBar();

				JMenu menuFlags = new JMenu("Flags");
                for (JMenuItem item : panel.createFlagsMenuItems()) {
                    menuFlags.add(item);
                }
				menuBar.add(menuFlags);

				JMenu menuEdit = new JMenu("Edit");
				for (Action action : panel.getActions()) {
					menuEdit.add(new JMenuItem(action));
				}
				menuBar.add(menuEdit);
				
				JMenu menuAbout = new JMenu("About");
				JMenuItem about = new JMenuItem("About");
				about.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						JOptionPane
								.showMessageDialog(
										RegexApplet.this,
										"<html>Java-applet that helps you test regular expressions with the Sun Java standard API (java.util.regex)<br>(c) 2008 by Sergey Evdokimov (sergey.evdokimov85@gmail.com)</html>",
										"About", JOptionPane.INFORMATION_MESSAGE);
					}
				});
				menuAbout.add(about);
				menuBar.add(menuAbout);

				setJMenuBar(menuBar);

                ToolTipManager.sharedInstance().setDismissDelay(20000);

                Map<String, String> state = new Map<String, String>() {
                    public int size() {
                        throw new UnsupportedOperationException();
                    }
                    public boolean isEmpty() {
                        throw new UnsupportedOperationException();
                    }
                    public boolean containsKey(Object key) {
                        return getParameter((String)key) != null;
                    }
                    public boolean containsValue(Object value) {
                        throw new UnsupportedOperationException();
                    }
                    public String get(Object key) {
                        return getParameter((String)key);
                    }
                    public String put(String key, String value) {
                        throw new UnsupportedOperationException();
                    }
                    public String remove(Object key) {
                        throw new UnsupportedOperationException();
                    }
                    public void putAll(Map<? extends String, ? extends String> m) {
                        throw new UnsupportedOperationException();
                    }
                    public void clear() {
                        throw new UnsupportedOperationException();
                    }
                    public Set<String> keySet() {
                        throw new UnsupportedOperationException();
                    }
                    public Collection<String> values() {
                        throw new UnsupportedOperationException();
                    }
                    public Set<Entry<String, String>> entrySet() {
                        throw new UnsupportedOperationException();
                    }
                };
                panel.restoreState(state);
            }
		});
	}

    public String getAppletInfo() {
        return  "Java-applet that helps you test regular expressions with the Sun Java standard API (java.util.regex)\n " +
                        "(c) 2008 by Sergey Evdokimov (sergey.evdokimov85@gmail.com)";
    }

    public void setRegex(final String regex) {
        SwingUtilities.invokeLater(new Runnable(){
			public void run() {
                panel.setRegex(regex);
			}
		});
	}
	
	public void setFlags(final int flags) {
		SwingUtilities.invokeLater(new Runnable(){
			public void run() {
				panel.getFlags().setFlags(flags);
			}
		});
	}

    public void setMode(final int mode) {
        SwingUtilities.invokeLater(new Runnable(){
            public void run() {
                panel.setMode(mode);
            }
        });
    }

    public void setText(final String text) {
        SwingUtilities.invokeLater(new Runnable(){
            public void run() {
                panel.setText(text);
            }
        });
    }

    public String[] saveState() {
        Map<String, String> map = (Map)UIThreadUtil.get(new UIThreadUtil.UIGetter() {
            public Object get() {
                Map<String, String> res = new HashMap<String, String>();
                panel.saveState(res);
                return res;
            }
        });
        int len = map.size();
        String[] res = new String[len * 2];
        int i = 0;
        for (Map.Entry<String, String> e : map.entrySet()) {
            res[i] = e.getKey();
            res[len + i] = e.getValue();
            i++;
        }
        return res;
    }

}