package com.ess.regexutil.html;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class ToHtmlForm extends JFrame {

	private JTextField regexField;
	private JTextArea resTextArea;
	
	public ToHtmlForm() {
		super("Regex to html converter");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		JPanel main = new JPanel(new BorderLayout());
		main.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

		regexField = new JTextField();
		regexField.setFont(new Font("MonoSpaced", Font.PLAIN, 12));
		main.add(regexField, BorderLayout.PAGE_START);
		
		resTextArea = new JTextArea();
		resTextArea.setFont(new Font("MonoSpaced", Font.PLAIN, 12));
		main.add(new JScrollPane(resTextArea), BorderLayout.CENTER);
		
		JPanel btnPanel = new JPanel();
		JButton convertBtn = new JButton("Convert");
		convertBtn.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				convert();
			}
		});
		btnPanel.add(convertBtn);
		
		
		main.add(btnPanel, BorderLayout.PAGE_END);
		
		setContentPane(main);
		
		getRootPane().setDefaultButton(convertBtn);
		
		setSize(900, 700);
		setLocationRelativeTo(getParent());
	}
	
	private void convert() {
		String regex = regexField.getText();
		String res = HtmlConverter.convert(regex, 0);
		resTextArea.setText(res);
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new ToHtmlForm().setVisible(true);
	}

}
