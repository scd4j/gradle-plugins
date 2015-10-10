package com.datamaio.scd4j.ui;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.SwingConstants;

public class ReadPasswordDialog {
	
	private JPasswordField currentPassword;
	private JPanel panel;
	public ReadPasswordDialog() {
		init();
	}

	protected void init() {
		panel = new JPanel(new BorderLayout(5, 5));

		JPanel label = new JPanel(new GridLayout(0, 1, 2, 2));
		label.add(new JLabel("Provide the password to decrypt properties:", SwingConstants.RIGHT));
		panel.add(label, BorderLayout.WEST);

		JPanel controls = new JPanel(new GridLayout(0, 1, 2, 2));
		currentPassword = new JPasswordField(15);
		controls.add(currentPassword);

		panel.add(controls, BorderLayout.CENTER);
	}
	
	public String showDialog() {
		JOptionPane.showMessageDialog(null, panel, "Password",
				JOptionPane.QUESTION_MESSAGE);
		
		return new String(currentPassword.getPassword());
	}

}
