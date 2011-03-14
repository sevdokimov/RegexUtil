package com.ess.regexutil.views;

import org.eclipse.jface.contentassist.IContentAssistSubjectControl;
import org.eclipse.jface.text.AbstractDocument;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IEventConsumer;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;

public class StyleTextContentAssistSubjectControl implements IContentAssistSubjectControl {

	private final StyledText textEditor;
	
	private final IDocument document;
	
	public StyleTextContentAssistSubjectControl(StyledText textEditor) {
		this.textEditor = textEditor;
		document = new AbstractDocument() {
			
		};
	}

	public void addKeyListener(KeyListener keyListener) {
		textEditor.addKeyListener(keyListener);
	}

	public boolean addSelectionListener(SelectionListener selectionListener) {
		textEditor.addSelectionListener(selectionListener);
		return true;
	}

	public boolean appendVerifyKeyListener(VerifyKeyListener verifyKeyListener) {
		textEditor.addVerifyKeyListener(verifyKeyListener);
		return true;
	}

	public int getCaretOffset() {
		textEditor.getCaretOffset();
		return 0;
	}

	public Control getControl() {
		return textEditor;
	}

	public IDocument getDocument() {
		return null;
	}

	public String getLineDelimiter() {
		return textEditor.getLineDelimiter();
	}

	public int getLineHeight() {
		return textEditor.getLineHeight();
	}

	public Point getLocationAtOffset(int offset) {
		return textEditor.getLocationAtOffset(offset);
	}

	public Point getSelectedRange() {
		return textEditor.getSelectionRange();
	}

	public Point getWidgetSelectionRange() {
		Point selection = textEditor.getSelection();
		selection.y -= selection.x;
		return selection;
	}

	public boolean prependVerifyKeyListener(VerifyKeyListener verifyKeyListener) {
		return false;
	}

	public void removeKeyListener(KeyListener keyListener) {
		textEditor.removeKeyListener(keyListener);
	}

	public void removeSelectionListener(SelectionListener selectionListener) {
		textEditor.removeSelectionListener(selectionListener);
	}

	public void removeVerifyKeyListener(VerifyKeyListener verifyKeyListener) {
		textEditor.removeVerifyKeyListener(verifyKeyListener);
	}

	public void revealRange(int offset, int length) {
		textEditor.setSelection(new Point(offset, offset+length));
	}

	public void setEventConsumer(IEventConsumer eventConsumer) {
	}

	public void setSelectedRange(int offset, int length) {
		textEditor.setSelectionRange(offset, length);
	}

	public boolean supportsVerifyKeyListener() {
		return true;
	}

}
