package com.ess.regexutil.swingadapter;

import javax.swing.BorderFactory;
import javax.swing.JTextPane;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BoxView;
import javax.swing.text.ComponentView;
import javax.swing.text.EditorKit;
import javax.swing.text.Element;
import javax.swing.text.IconView;
import javax.swing.text.LabelView;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;

public class OneLineJTextPane extends JTextPane {
	
	public OneLineJTextPane() {
		setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 6));
	}
	
    protected EditorKit createDefaultEditorKit() {
        return new StyledEditorKit() {
            StyledViewFactory f = new StyledViewFactory();
            public ViewFactory getViewFactory() {
                return f;
            }
        };
    }

//    protected DocumentFilter getDocumentFilter() {
//        return new OneLineFilter();
//    }
//
//    protected class OneLineFilter extends DocumentFilter {
//        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
//            if (!string.equals("\n") && !string.equals("\t"))
//                fb.insertString(offset, string.replace('\n', ' ').replace('\r', ' '), attr);
//        }
//        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
//            if (!text.equals("\n") && !text.equals("\t"))
//                fb.replace(offset, length, text.replace('\n', ' ').replace('\r', ' '), attrs);
//        }
//    }

    private static class StyledViewFactory implements ViewFactory {
        public View create(Element elem) {
            String kind = elem.getName();
            if (kind != null) {
                if (kind.equals(AbstractDocument.ContentElementName)) {
                    return new LabelView(elem);
                } else if (kind.equals(AbstractDocument.ParagraphElementName)) {
                    return new BoxView(elem, View.X_AXIS) {
                        protected View createRow() {
                            return null;
                        }
                    };
                } else if (kind.equals(AbstractDocument.SectionElementName)) {
                    return new BoxView(elem, View.X_AXIS);
                } else if (kind.equals(StyleConstants.ComponentElementName)) {
                    return new ComponentView(elem);
                } else if (kind.equals(StyleConstants.IconElementName)) {
                    return new IconView(elem);
                }
            }
            // default to text display
            return new LabelView(elem);
        }
    }

	
}
