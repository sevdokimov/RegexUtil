package com.ess.regexutil.views;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import com.ess.regexutil.Activator;
import com.ess.util.ListenersList;


public class ModeChangeAction extends Action implements IMenuCreator {

	private static final Mode DEFAULT_MODE = Mode.Find;

	public final ListenersList<Mode> modeChangeListeners = new ListenersList<Mode>();
	
	public enum Mode {
		Find("Find sequence"), Matche("Match complete text"), Split("Split"), Replace("Replace");
		
		private final String text;

		private Mode(String name) {
			this.text = name;
		}

		public String getText() {
			return text;
		}
	}
	
	private Menu menu;
	
	private Mode currentMode = DEFAULT_MODE;
	
	public ModeChangeAction() {
		setMenuCreator(this);
		setText("Mode");
		setToolTipText("Mode");
		setImageDescriptor(Activator.getImageDescriptor("icons/mode.gif"));
	}
	
	public void dispose() {
		if (menu != null)
			menu.dispose();
	}

	public void setCurrentMode(Mode currentMode) {
		if (this.currentMode != currentMode) {
			this.currentMode = currentMode;
			modeChangeListeners.send(currentMode);
		}
	}
	
	public void setCurrentModeById(int currentMode) {
		Mode[] modes = Mode.values();
		Mode mode;
		if (currentMode < 0 || currentMode >= modes.length) {
			mode = DEFAULT_MODE;
		} else {
			mode = modes[currentMode];
		}
		setCurrentMode(mode);
	}

	public Menu getMenu(Control parent) {
		if (menu == null) {
			menu = new Menu(parent);
			
			for (Mode mode : Mode.values()) {
				final Mode fMode = mode;
				final MenuItem item = new MenuItem(menu, SWT.RADIO);
				item.setText(mode.getText());
				item.setSelection(mode == currentMode);
				item.addSelectionListener(new SelectionListener() {
					public void widgetDefaultSelected(SelectionEvent e) {
					}
					public void widgetSelected(SelectionEvent e) {
						if (item.getSelection()) {
							setCurrentMode(fMode);
						}
					}
				});
			}
		}
		return menu;
	}

	public Mode getCurrentMode() {
		return currentMode;
	}

	public Menu getMenu(Menu parent) {
		return null;
	}

}
