package com.ess.regexutil.views;

import org.eclipse.jface.action.IAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import com.ess.regexutil.controls.ReplaceHilighter;
import com.ess.regexutil.controls.SecondaryEditorHilighter;
import com.ess.regexutil.parsedtext.ITextEditorAdapter;
import com.ess.regexutil.regexparser.Flags;
import com.ess.regexutil.swtadapter.SWTAdapter;

public class ReplacePanel implements IRegexUtilPanel {

	private RegexEditor regexEditor;
	
	private StyledText replacementEditor;
	private SWTAdapter replacementEditorAdapter;
	
	private StyledText textEditor;
	private SWTAdapter textEditorAdapter;
	
	private StyledText resultEditor;

	private Composite panel;
	
	private SecondaryEditorHilighter secondaryEditorHilighter;
	
	public ReplacePanel(Composite parent, Flags flags, IAction[] actions) {
		panel = new Composite(parent, 0);
		
		panel.setLayout(new GridLayout(1, false));

		SashForm regexSashForm = new SashForm(panel, SWT.HORIZONTAL);
		GridData regexSashFormGridData = new GridData();
		regexSashFormGridData.grabExcessHorizontalSpace = true;
		regexSashFormGridData.verticalAlignment = SWT.FILL;
		regexSashFormGridData.horizontalAlignment = SWT.FILL;
		regexSashForm.setLayoutData(regexSashFormGridData);
		
		regexEditor = new RegexEditor(regexSashForm, SWT.BORDER, flags, actions);
		
		replacementEditor = new StyledText(regexSashForm, SWT.BORDER | SWT.SINGLE);
		replacementEditor.setText(DefaultFormValueManager.getInstance().getDefaultReplacement());
		replacementEditor.setFont(new Font(Display.getDefault(), "Courier New", 10, 0));
		
		SashForm resultSashForm = new SashForm(panel, SWT.HORIZONTAL);
		GridData resultSashFormGridData = new GridData();
		resultSashFormGridData.grabExcessHorizontalSpace = true;
		resultSashFormGridData.grabExcessVerticalSpace = true;
		resultSashFormGridData.verticalAlignment = SWT.FILL;
		resultSashFormGridData.horizontalAlignment = SWT.FILL;
		resultSashForm.setLayoutData(resultSashFormGridData);

		// Create text editor
		textEditor = new StyledText(resultSashForm, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		textEditorAdapter = new SWTAdapter(textEditor);
		
		secondaryEditorHilighter = new SecondaryEditorHilighter(
				regexEditor.getHilighter(), 
				textEditorAdapter,
				false
		);
		
		resultEditor = new StyledText(resultSashForm, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		
		replacementEditorAdapter = new SWTAdapter(replacementEditor);
		new ReplaceHilighter(
				textEditorAdapter,
				new SWTAdapter(resultEditor),
				regexEditor.getHilighter(),
				replacementEditorAdapter,
				false);
	}

	public String getReplacement() {
		return replacementEditorAdapter.getText();
	}
	
	public void setMatchMode(boolean matchMode) {
		secondaryEditorHilighter.setMatchMode(matchMode);
	}
	
	public Composite getPanel() {
		return panel;
	}

	public RegexEditor getRegexEditor() {
		return regexEditor;
	}

	public ITextEditorAdapter getTextEditor() {
		return textEditorAdapter;
	}

}
