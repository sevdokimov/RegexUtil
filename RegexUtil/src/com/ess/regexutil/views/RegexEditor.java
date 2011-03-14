package com.ess.regexutil.views;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;

import com.ess.regexutil.controls.RegexHighlighter;
import com.ess.regexutil.parsedtext.ITextEditorAdapter;
import com.ess.regexutil.regexparser.Flags;
import com.ess.regexutil.swtadapter.SWTAdapter;

public class RegexEditor extends StyledText {

	private final ITextEditorAdapter adapter;
	
	private final RegexHighlighter highlighter;
	
	public RegexEditor(Composite parent, int style, Flags flags, final IAction[] actions) {
		super(parent, SWT.SINGLE | style);
		setFont(new Font(Display.getDefault(), "Courier New", 10, 0));
		adapter = new SWTAdapter(this);
		highlighter = new RegexHighlighter(adapter, flags);
		
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				for (IAction action : actions) {
					manager.add(action);
				}
			}
		});
		Menu menu = menuMgr.createContextMenu(this);
		this.setMenu(menu);
	}

	public ITextEditorAdapter getAdapter() {
		return adapter;
	}

	public RegexHighlighter getHilighter() {
		return highlighter;
	}
	
	
}
