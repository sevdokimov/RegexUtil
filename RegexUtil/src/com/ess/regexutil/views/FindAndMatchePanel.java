package com.ess.regexutil.views;

import org.eclipse.jface.action.IAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;

import com.ess.regexutil.controls.SecondaryEditorHilighter;
import com.ess.regexutil.parsedtext.ITextEditorAdapter;
import com.ess.regexutil.regexparser.Flags;
import com.ess.regexutil.swtadapter.SWTAdapter;

public class FindAndMatchePanel implements IRegexUtilPanel {

	private RegexEditor regexEditor;
	
	private StyledText textEditor;
	private ITextEditorAdapter textEditorAdapter;

	private Composite panel;
	
	private SecondaryEditorHilighter secondaryEditorHilighter;
	
	public FindAndMatchePanel(Composite parent, Flags flags, IAction[] actions) {
		panel = new Composite(parent, 0);
		
		Layout layout = new GridLayout(1, false);
		panel.setLayout(layout);

		Label regexEditorLabel = new Label(panel, 0);
		regexEditorLabel.setText("Regular Expression");

		// Create regex editor
		regexEditor = new RegexEditor(panel, SWT.BORDER, flags, actions);

		GridData regexEditorGridData = new GridData();
		regexEditorGridData.grabExcessHorizontalSpace = true;
		regexEditorGridData.horizontalAlignment = SWT.FILL;
		regexEditor.setLayoutData(regexEditorGridData);
		
		// Create text editor
		textEditor = new StyledText(panel, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);

		GridData textEditorGridData = new GridData();
		textEditorGridData.grabExcessHorizontalSpace = true;
		textEditorGridData.grabExcessVerticalSpace = true;
		textEditorGridData.verticalAlignment = SWT.FILL;
		textEditorGridData.horizontalAlignment = SWT.FILL;

		textEditor.setLayoutData(textEditorGridData);
		textEditorAdapter = new SWTAdapter(textEditor);
		
		secondaryEditorHilighter = new SecondaryEditorHilighter(
				regexEditor.getHilighter(), 
				textEditorAdapter,
				false
		);
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
