package com.ess.regexutil.swingadapter;

import com.ess.regexutil.parsedtext.ITextEditorAdapter;
import com.ess.regexutil.parsedtext.ITextStyle;
import com.ess.regexutil.parsedtext.StyleData;
import com.ess.regexutil.parsedtext.TextStyle;
import com.ess.util.Helper;
import com.ess.util.IListenersList;
import com.ess.util.ListenersList;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;

public class SwingAdapter implements ITextEditorAdapter {

	private final ListenersList<String> changeListeners = new ListenersList<String>();
	
	private final JTextPane editor;
	
	private String text;
	private int caret;
	private int selStart;
	private int selEnd;
	
	private boolean isUpdateInSwingQueue;
	private boolean hasFocus;
	
	private TooltipResolver tooltipResolver;
	
	private String placeholder;
	private boolean isPlaceholderActive;
	
	private Style placeholderStyle;
	
	private boolean isPaint;
	
	private Runnable updateRun = new Runnable() {
		public void run() {
			isUpdateInSwingQueue = false;
			if (isPlaceholderActive)
				return;

			
			String newText = editor.getText();
			
			int oldSelStart = selStart;
			int oldSelEnd = selEnd;
			
			selStart = getStartSelection();
			selEnd = getEndSelection();
			
			String event = null;
			if (!newText.equals(text)) {
				text = newText;
				event = text;
			}
			int newCaret = getCaret();
			if (event != null 
					|| caret != newCaret
					|| (! ((oldSelStart == oldSelEnd && selStart == selEnd) || (oldSelStart == selStart && oldSelEnd == selEnd) ))) {
				caret = newCaret;
                
				changeListeners.send(event);
			}
			trySetPlaceholder();
		}
	};

	public SwingAdapter(boolean isOneLine) {
		if (isOneLine) {
			editor = new OneLineJTextPane() {
				
				@Override
				public String getToolTipText(MouseEvent event) {
					if (tooltipResolver == null)
						return null;
					try {
						int pos = getPosition(editor, event.getPoint());
						if (pos != -1) {
							String s = tooltipResolver.getTooltip(pos);
							if (s != null)
								return Helper.toHtmlDocument(s);
						}
					} catch (BadLocationException e) {
						// Ignore
					}
					return null;
				}

                @Override
                public String getText() {
                    String res = super.getText();
                    if (res == null) {
                        res = "";
                    }
                    return res;
                }
            };
		} else {
			editor = new JTextPane() {
				@Override
				public String getToolTipText(MouseEvent event) {
					if (tooltipResolver == null)
						return null;
					try {
						int pos = getPosition(editor, event.getPoint());
						if (pos != -1) {
							String s = tooltipResolver.getTooltip(pos);
							if (s != null)
								return Helper.toHtmlDocument(s);
						}
					} catch (BadLocationException e) {
						// Ignore
					}
					return null;
				}

                @Override
                public String getText() {
                    String res = super.getText();
                    if (res == null) {
                        res = "";
                    }
                    return res;
                }
			};
		}
		
		StyledDocument doc = editor.getStyledDocument(); 
		Style defStyle = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
		placeholderStyle = doc.addStyle(null, defStyle);
		StyleConstants.setForeground(placeholderStyle, new Color(0x00999999));
		StyleConstants.setItalic(placeholderStyle, true);
		
		ToolTipManager.sharedInstance().registerComponent(editor);
		
		text = editor.getText();
		caret = editor.getCaretPosition();
		
		editor.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent arg0) {
				hasFocus = true;
				if (isPlaceholderActive) {
					isPlaceholderActive = false;
					editor.setText("");
				}
				someChange();
			}

			public void focusLost(FocusEvent arg0) {
				hasFocus = false;
				someChange();
			}
		});
		editor.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
				someChange();
            }

		});
		editor.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent arg0) {
				someChange();
			}
			
		});
		
		editor.getDocument().addDocumentListener(new DocumentListener(){
			public void changedUpdate(DocumentEvent e) {
				someChange();
			}

			public void insertUpdate(DocumentEvent e) {
				someChange();
			}

			public void removeUpdate(DocumentEvent e) {
				someChange();
			}
		});
	}

	private void trySetPlaceholder() {
		if (!hasFocus && !isPlaceholderActive && editor.getDocument().getLength() == 0 && placeholder != null) {
			isPlaceholderActive = true;
			editor.setText(placeholder);
			editor.getStyledDocument().setCharacterAttributes(0, editor.getDocument().getLength(), placeholderStyle, true); 
		}
	}
	
	public String getPlaceholder() {
		return placeholder;
	}

	public void setPlaceholder(String placeholder) {
		if (this.placeholder != null)
			throw new IllegalArgumentException();
		this.placeholder = placeholder;
		trySetPlaceholder();
	}



	private int getPosition(JTextPane editor, Point p) throws BadLocationException {
		int width = editor.getFontMetrics(editor.getFont()).charWidth('G');
		
		int length = editor.getText().length();
		for (int i = 0; i < length; i++) {
			 Rectangle r = editor.modelToView(i);
			 if (p.x < r.x && p.y < r.y)
				 return -1;
			 r.width = width;
			 if (r.contains(p))
				 return i;
		}
		
		return -1;
	}
	
	public JTextPane getEditor() {
		return editor;
	}

	private void someChange() {
		if (isUpdateInSwingQueue || isPlaceholderActive || isPaint)
			return;
		isUpdateInSwingQueue = true;
		SwingUtilities.invokeLater(updateRun);
	}

	public int getCaret() {
		return hasFocus ? editor.getCaretPosition() : -1;
	}

	public int getStartSelection() {
		if (hasFocus)
			return editor.getSelectionStart();
		else
			return -1;
	}

	public int getEndSelection() {
		if (hasFocus)
			return editor.getSelectionEnd();
		else
			return -1;
	}

	public IListenersList<String> getListenersList() {
		return changeListeners;
	}

	public ITextStyle getSelectionStyle() {
		return new TextStyle(editor.getSelectedTextColor().getRGB(), editor.getSelectionColor().getRGB(), null, null);
	}

	public String getText() {
		return isPlaceholderActive ? "" : editor.getText();
	}

	public int getSelStart() {
		return isPlaceholderActive ? 0 : selStart;
	}

	public int getSelEnd() {
		return isPlaceholderActive ? 0 : selEnd;
	}

	public void paint(StyleData data) {
		if (isPlaceholderActive)
			return;
		
		isPaint = true;
		
		String text = editor.getText();
		int length = text.length();
		
		StyledDocument doc = editor.getStyledDocument(); 
		
		int i = 0;
		Style defStyle = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
		
		int rCountAll = 0;
		while (i < length) {
			Style style = doc.addStyle(null, defStyle);
			StyleConstants.setBackground(style, new Color(data.background[i]));
			StyleConstants.setForeground(style, new Color(data.foreground[i]));
			StyleConstants.setItalic(style, data.italic[i]);
			StyleConstants.setBold(style, data.bold[i]);
			
			int rCount = (text.charAt(i) == '\r') ? 1 : 0;
			int k;
			for (k = i + 1; k < length && data.isEquals(i, k); k++) {
				if (text.charAt(k) == '\r')
					rCount++;
			}
			
			doc.setCharacterAttributes(i - rCountAll, k - i - rCount, style, true);
			rCountAll += rCount;
			i = k;
		}
		
		isPaint = false;
	}

	public void setText(String text) {
        if (isPlaceholderActive) {
			isPlaceholderActive = false;
		} else {
            if (editor.getText().equals(text))
                return;
        }
		editor.setText(text);
	}

	public void setTooltipResolver(TooltipResolver tooltipResolver) {
		this.tooltipResolver = tooltipResolver;
	}

}
