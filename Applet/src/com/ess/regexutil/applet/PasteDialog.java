package com.ess.regexutil.applet;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.*;

public class PasteDialog extends JDialog {

	private static PasteDialog instance;
	
	private JTextField textEditor;
	
	private PasteDialog() {
		super((JFrame)null, "Insert regex", true);
		setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
		
		setResizable(false);

		JPanel main = new JPanel();
		main.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
		main.add(new JLabel("Press Ctrl+V to insert regex", JLabel.CENTER));
		main.add(Box.createVerticalStrut(5));

        textEditor = new JTextField(40);
        main.add(textEditor);

        KeyListener l = new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				if (e.getKeyChar() != 22 || e.getModifiers() != KeyEvent.CTRL_MASK) {
					e.consume();
				}
				PasteDialog.this.setVisible(false);
			}
        };
        addKeyListener(l);
        textEditor.addKeyListener(l);
        
        getContentPane().add(main);
        pack();
	}
	
    public String show(Component parent) {
        textEditor.setText("");
        
        setLocationRelativeTo(parent);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                textEditor.requestFocus();
            }
        });
        setVisible(true);
        
        String res = textEditor.getText();
        if (res.length() == 0)
        	res = null;
        return res;
    }
	
    public static PasteDialog getInstance() {
		if (instance == null)
			instance = new PasteDialog();
		return instance;
	}

}
