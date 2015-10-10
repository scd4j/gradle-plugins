package com.datamaio.scd4j.ui;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import com.datamaio.scd4j.ui.dto.ChangePasswordDTO;
import com.datamaio.scd4j.ui.dto.EncryptPropertyDTO;
/**
 * 
 * @author Mateus M. da Costa
 *
 */
public class EncryptPropertyDialog {
	
	private JTextField property;
	private JTextField propertyValue;
	private JPasswordField newPassword;
	private JPasswordField confirmNewPassword;
	private JPanel panel;
	
	
	public EncryptPropertyDialog() {
		init();
	}
	
	
	private void init() {
		panel = new JPanel(new BorderLayout(5, 5));

		JPanel label = new JPanel(new GridLayout(0, 1, 2, 2));
		label.add(new JLabel("Property Name:", SwingConstants.RIGHT));
		label.add(new JLabel("Propert Value:", SwingConstants.RIGHT));
		label.add(new JLabel("New Password:", SwingConstants.RIGHT));
		label.add(new JLabel("Confirm New Password:", SwingConstants.RIGHT));
		panel.add(label, BorderLayout.WEST);

		JPanel controls = new JPanel(new GridLayout(0, 1, 2, 2));
		property = new JTextField(15);
		controls.add(property);
		
		propertyValue = new JTextField(15);
		controls.add(propertyValue);
		
		newPassword = new JPasswordField(15);
		controls.add(newPassword);
		confirmNewPassword = new JPasswordField(15);
		controls.add(confirmNewPassword);

		panel.add(controls, BorderLayout.CENTER);
	}
	
	public EncryptPropertyDTO showDialog() {
		JOptionPane.showMessageDialog(null, panel, "Helper tool to encrypt a property value",
				JOptionPane.QUESTION_MESSAGE);
		
		EncryptPropertyDTO dto = new EncryptPropertyDTO();
		dto.setProperty(property.getText());
		dto.setPropertyValue(propertyValue.getText());
		dto.setNewPassword(new String(newPassword.getPassword()));
		dto.setConfirmNewPassword(new String(confirmNewPassword.getPassword()));
		
		return dto;
	}
}
