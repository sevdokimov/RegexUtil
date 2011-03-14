package com.ess.regexutil.applet;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.*;

public class CopyDialog extends JDialog {

	private static CopyDialog instance; 
	
	private JTextField textEditor;
	
	private CopyDialog() {
		super((JFrame)null, "Coping regex", true);
		setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
		
		setResizable(false);

		JPanel main = new JPanel();
		main.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
		main.add(new JLabel("Press Ctrl+C to copy regex", JLabel.CENTER));
		main.add(Box.createVerticalStrut(5));
		
        textEditor = new JTextField(40);
        textEditor.setEditable(false);
        textEditor.setBackground(Color.WHITE);
		
        main.add(textEditor);
        
        KeyListener l = new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				CopyDialog.this.setVisible(false);
			}
        };
        addKeyListener(l);
        textEditor.addKeyListener(l);
        
        getContentPane().add(main);
    }

    public void show(Component parent, String text) {
        textEditor.setText(text);
        textEditor.setSelectionStart(0);
        textEditor.setSelectionEnd(text.length());
        pack();
        setLocationRelativeTo(parent);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                textEditor.requestFocus();
            }
        });
        setVisible(true);
    }

    public static CopyDialog getInstance() {
		if (instance == null)
			instance = new CopyDialog();
		return instance;
	}
	
}
