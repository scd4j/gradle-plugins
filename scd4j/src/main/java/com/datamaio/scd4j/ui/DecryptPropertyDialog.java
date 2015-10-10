package com.datamaio.scd4j.ui;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import com.datamaio.scd4j.ui.dto.EncryptPropertyDTO;

/**
 * 
 * @author Mateus M. da Costa
 *
 */
public class DecryptPropertyDialog {
	private JTextField property;
	private JPasswordField confirmNewPassword;
	private JPanel panel;
	
	
	public DecryptPropertyDialog() {
		init();
	}
	
	
	private void init() {
		panel = new JPanel(new BorderLayout(5, 5));

		JPanel label = new JPanel(new GridLayout(0, 1, 2, 2));
		label.add(new JLabel("Provide the encrypted Property Value:", SwingConstants.RIGHT));
		label.add(new JLabel("Password:", SwingConstants.RIGHT));
		panel.add(label, BorderLayout.WEST);

		JPanel controls = new JPanel(new GridLayout(0, 1, 2, 2));
		property = new JTextField(15);
		controls.add(property);
		
		confirmNewPassword = new JPasswordField(15);
		controls.add(confirmNewPassword);

		panel.add(controls, BorderLayout.CENTER);
	}
	
	public EncryptPropertyDTO showDialog() {
		JOptionPane.showMessageDialog(null, panel, "Helper tool to decrypt an existing property value",
				JOptionPane.QUESTION_MESSAGE);
		
		EncryptPropertyDTO dto = new EncryptPropertyDTO();
		dto.setProperty(property.getText());
		dto.setConfirmNewPassword(new String(confirmNewPassword.getPassword()));
		
		return dto;
	}
}
