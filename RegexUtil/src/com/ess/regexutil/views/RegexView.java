package com.ess.regexutil.views;


import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.osgi.framework.BundleContext;

import com.ess.regexutil.Activator;
import com.ess.regexutil.escaping.JavaRegexTransfer;
import com.ess.regexutil.escaping.JavaScriptTransfer;
import com.ess.regexutil.escaping.RegexTransfer;
import com.ess.regexutil.escaping.XmlRegexTransfer;
import com.ess.regexutil.regexparser.Flags;
import com.ess.regexutil.views.ModeChangeAction.Mode;
import com.ess.util.EventListener;


/**
 * @TODO find unusage
 * @TODO examples
 * 
 * - copy as hilighted string
 * - auto complite
 */

public class RegexView extends ViewPart {

	private FindAndMatchePanel findAndMatchePanel;
	
	private SplitPanel splitPanel;
	private ReplacePanel replacePanel;
	
	private IRegexUtilPanel currentPanel;

	private final Flags flags = new Flags();
	
	private StackLayout layout;
	
	private Clipboard clipboard;
	
	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	@Override
	public void createPartControl(Composite parent) {
		clipboard = new Clipboard(Display.getDefault());

		layout = new StackLayout();
		parent.setLayout(layout);
		IToolBarManager toolBarManager = getViewSite().getActionBars().getToolBarManager();
		
		final ModeChangeAction modeAction = new ModeChangeAction();
		toolBarManager.add(modeAction);
		modeAction.modeChangeListeners.addListener(new EventListener<Mode>() {
			public void notify(Mode mode) {
				setMode(mode);
			}
		});
		
		FlagChangeAction flagAction = new FlagChangeAction(flags);
		toolBarManager.add(flagAction);
		
		IAction actionCopySL = new CopyAction(JavaRegexTransfer.instance, "Copy For Java-String (escape '\\')");
		IAction actionPasteSL = new PasteAction(JavaRegexTransfer.instance, "Paste From Java-String (unescape '\\')");
		
		IAction actionCopyXml = new CopyAction(XmlRegexTransfer.instance, "Copy For XML (escape '>', '&', ...)");
		IAction actionPasteXml = new PasteAction(XmlRegexTransfer.instance, "Paste From XML (unescape '>', '&', ...)");

		IAction actionCopyJS = new CopyAction(JavaScriptTransfer.instance, "Copy For JavaScript");
		IAction actionPasteJS = new PasteAction(JavaScriptTransfer.instance, "Paste From JavaScript");

		
		toolBarManager.add(new Separator());
		toolBarManager.add(actionPasteSL);
		toolBarManager.add(actionCopySL);
		toolBarManager.add(new Separator());
		toolBarManager.add(createAboutAction());
		
		
		IAction[] actions = new IAction[] {
				actionCopySL,
				actionPasteSL,
				actionCopyXml,
				actionPasteXml,
				actionCopyJS,
				actionPasteJS,
		};
		
		findAndMatchePanel = new FindAndMatchePanel(parent, flags, actions);
		splitPanel = new SplitPanel(parent, flags, actions);
		replacePanel = new ReplacePanel(parent, flags, actions);
		
		setMode(modeAction.getCurrentMode());
		modeAction.setCurrentModeById(DefaultFormValueManager.getInstance().getDefaultMode());
		currentPanel.getRegexEditor().setText(DefaultFormValueManager.getInstance().getDefaultRegex());
		currentPanel.getTextEditor().setText(DefaultFormValueManager.getInstance().getDefaultText());
		
		Activator.getDefault().stopListeners.addListener(new EventListener<BundleContext>(){
			public void notify(BundleContext event) {
				DefaultFormValueManager.getInstance().save(
						currentPanel.getRegexEditor().getAdapter().getText(),
						currentPanel.getTextEditor().getText(),
						replacePanel.getReplacement(),
						modeAction.getCurrentMode().ordinal());
			}
		});
	}

	private void setMode(Mode mode) {
		switch (mode) {
		case Find:
			setPane(findAndMatchePanel);
			findAndMatchePanel.setMatchMode(false);
			break;
			
		case Matche:
			setPane(findAndMatchePanel);
			findAndMatchePanel.setMatchMode(true);
			break;
			
		case Split:
			setPane(splitPanel);
			break;
			
		case Replace:
			setPane(replacePanel);
			break;
			
		};
	}
	
	private IAction createAboutAction() {
		IAction res = new Action() {
			@Override
			public void run() {
				MessageDialog.openInformation(getViewSite().getShell()
						,"About Regex Util"
						,"http://myregexp.com/\n\n(c) 2007 by Sergey Evdokimov (sergey.evdokimov85@gmail.com)");
			}
		};
		res.setToolTipText("About");
		res.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
				getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
		return res;
	}
	
	private void setPane(IRegexUtilPanel panel) {
		if (panel == currentPanel)
			return;
		
		if (currentPanel != null) {
			panel.getRegexEditor().setText(currentPanel.getRegexEditor().getText());
			currentPanel.getRegexEditor().setText("");
			panel.getTextEditor().setText(currentPanel.getTextEditor().getText());
			currentPanel.getTextEditor().setText("");
		}
		currentPanel = panel;
		layout.topControl = panel.getPanel();
		panel.getPanel().getParent().layout();
	}
	
	/**
	 * Passing the focus request to the viewer's control.
	 */
	@Override
	public void setFocus() {
	//	viewer.getControl().setFocus();
	}
	
	private class PasteAction extends Action {

		private final RegexTransfer tr;
		
		public PasteAction(RegexTransfer tr, String name) {
			this.tr = tr;
			setText(name);
			setToolTipText(name);

			setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
					getImageDescriptor(ISharedImages.IMG_TOOL_PASTE));
		}
		
		@Override
		public void run() {
			StyledText regexEditor = currentPanel.getRegexEditor();
			String text = regexEditor.getText();
			Point sel = regexEditor.getSelection();
			String data = (String)clipboard.getContents(TextTransfer.getInstance());
			data = tr.sourceToRegex(data);
			regexEditor.setText(text.substring(0, sel.x) + data + text.substring(sel.y));
			int caret = sel.x + data.length();
			regexEditor.setSelection(caret, caret);
		}
	}
	
	private class CopyAction extends Action {

		private final RegexTransfer tr;
		
		public CopyAction(RegexTransfer tr, String name) {
			this.tr = tr;
			setText(name);
			setToolTipText(name);

			setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
					getImageDescriptor(ISharedImages.IMG_TOOL_COPY));
		}
		
		@Override
		public void run() {
			StyledText regexEditor = currentPanel.getRegexEditor();
			String text = regexEditor.getSelectionText();
			if (text.length() == 0)
				text = regexEditor.getText();
			
			text = tr.regexToSource(text);
			clipboard.setContents(new Object[]{text}, new Transfer[] {TextTransfer.getInstance()});
		}
	}

}