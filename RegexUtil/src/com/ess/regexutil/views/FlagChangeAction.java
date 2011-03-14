package com.ess.regexutil.views;

import java.util.regex.Pattern;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import com.ess.regexutil.Activator;
import com.ess.regexutil.regexparser.Flags;
import com.ess.util.RegexConsts;


public class FlagChangeAction extends Action implements IMenuCreator {

	private Menu menu;
	
	private Flags flags;
	
	public FlagChangeAction(Flags flags) {
		this.flags = flags;
		setMenuCreator(this);
		setText("Flags");
		setToolTipText("Flags");
		setImageDescriptor(Activator.getImageDescriptor("icons/text.gif"));
	}
	
	public void dispose() {
		if (menu != null)
			menu.dispose();
	}

	public Menu getMenu(Control parent) {
		if (menu == null) {
			menu = new Menu(parent);
			
			for (int i = 0; i < RegexConsts.flagsData.length; i++) {
				assert RegexConsts.flagsData[i].length == 2;
				final int index = i;
				final MenuItem item = new MenuItem(menu, SWT.CHECK);
				item.addSelectionListener(new SelectionListener() {
					public void widgetDefaultSelected(SelectionEvent e) {
					}
					public void widgetSelected(SelectionEvent e) {
						int flag = (Integer)RegexConsts.flagsData[index][0];
						flags.setFlag(flag, item.getSelection());
					}
				});
				item.setText((String)RegexConsts.flagsData[index][1]);
				item.setSelection(flags.isFlag((Integer)RegexConsts.flagsData[index][0]));
			}
		}
		return menu;
	}

	public Menu getMenu(Menu parent) {
		return null;
	}

}
