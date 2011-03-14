package com.ess.regexutil.swtadapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;

import com.ess.regexutil.parsedtext.ITextEditorAdapter;
import com.ess.regexutil.parsedtext.ITextStyle;
import com.ess.regexutil.parsedtext.StyleData;
import com.ess.regexutil.parsedtext.TextStyle;
import com.ess.util.IListenersList;
import com.ess.util.ListenersList;

public class SWTAdapter implements ITextEditorAdapter {

	private final ListenersList<String> changeListeners = new ListenersList<String>();

	private final StyledText editor;

	private String text;

	private int caret;

	private Point selection;

	private boolean hasFocus;

	private TooltipResolver tooltipResolver;
	
	private static final Map<Integer, Color> colorMap = new HashMap<Integer, Color>();

	public SWTAdapter(final StyledText editor) {
		this.editor = editor;
		text = editor.getText();
		caret = editor.getCaretOffset();
		selection = editor.getSelection();
		editor.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
				hasFocus = true;
				someChange();
			}

			public void focusLost(FocusEvent e) {
				hasFocus = false;
				someChange();
			}
		});
		editor.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.character == 1) {
					editor.setSelection(0, editor.getText().length());
				}
				someChange();
			}
		});
		editor.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				someChange();
			}
		});
		editor.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				someChange();
			}
		});
		editor.addSelectionListener(new SelectionListener(){
			public void widgetDefaultSelected(SelectionEvent arg0) {
				someChange();
			}
			public void widgetSelected(SelectionEvent arg0) {
				someChange();
			}
		});
		editor.addMouseMoveListener(new MouseMoveListener() {
			public void mouseMove(MouseEvent e) {
				if (tooltipResolver == null)
					return;
				try {
					// Hilighter.this.regexEditor.getBorderWidth();
					int pos = editor.getOffsetAtLocation(new Point(e.x - editor.getBorderWidth(), e.y));
					editor.setToolTipText(tooltipResolver.getTooltip(pos));
				} catch (IllegalArgumentException e1) {
					// Ignore
				}
			}
		});
	}

	private static Color getColor(Integer rgb) {
		Color res = colorMap.get(rgb);
		if (res == null) {
			res = new Color(Display.getDefault(), (rgb >> 16) & 0xFF,
					(rgb >> 8) & 0xFF, (rgb & 0xFF));
			colorMap.put(rgb, res);
		}
		return res;
	}

	public int getCaret() {
		return caret;
	}

	public int getEndSelection() {
		return selection.y;
	}

	public int getStartSelection() {
		return selection.x;
	}

	public String getText() {
		return text;
	}

	private int innerGetCaret() {
		if (hasFocus)
			return editor.getCaretOffset();
		else
			return -1;
	}

	private void someChange() {
		String event = null;
		if (!editor.getText().equals(text)) {
			text = editor.getText();
			event = text;
		}
		int newCaret = innerGetCaret();
		Point newSelection = editor.getSelection();
		if (event != null || caret != newCaret || !selection.equals(newSelection)) {
			caret = newCaret;
			selection = newSelection;
			changeListeners.safeSend(event);
		}
	}

	public void paint(StyleData data) {
		int length = text.length();
		ArrayList<StyleRange> styles = new ArrayList<StyleRange>(length);

		int i = 0;
		StyleRange style;
		while (i < length) {
			style = new StyleRange(0, 0, null, null, SWT.NORMAL);
			style.background = getColor(data.background[i]);
			style.foreground = getColor(data.foreground[i]);
			style.fontStyle = (data.italic[i] ? SWT.ITALIC : 0)
					| (data.bold[i] ? SWT.BOLD : 0);

			style.start = i;

			int k;
			for (k = i + 1; k < length && data.isEquals(i, k); k++)
				;
			style.length = k - i;
			i = k;
			styles.add(style);
		}
		editor.setStyleRanges(styles.toArray(new StyleRange[styles.size()]));
	}

	public IListenersList<String> getListenersList() {
		return changeListeners;
	}

	public static int colorToInt(Color c) {
		return (c.getRed() << 16) | (c.getGreen() << 8) | c.getBlue();
	}

	public ITextStyle getSelectionStyle() {
		return new TextStyle(
				colorToInt(editor.getSelectionForeground()), 
						colorToInt(editor.getSelectionBackground()), null,
				null);
	}

	public void setText(String text) {
		editor.setText(text);
	}

	public void setTooltipResolver(final TooltipResolver tooltipResolver) {
		this.tooltipResolver = tooltipResolver;
	}

}
