package com.datamaio.scd4j.ui;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.SwingConstants;

/**
 * Creates a dialog for changing the password
 * 
 * @author Mateus M. da Costa
 */
public class ChangePasswordDialog {
	
	private JPasswordField currentPassword;
	private JPasswordField newPassword;
	private JPasswordField confirmNewPassword;
	private JPanel panel;

	public ChangePasswordDialog() {
		init();
	}

	protected void init() {
		panel = new JPanel(new BorderLayout(5, 5));

		JPanel label = new JPanel(new GridLayout(0, 1, 2, 2));
		label.add(new JLabel("Current Password:", SwingConstants.RIGHT));
		label.add(new JLabel("New Password:", SwingConstants.RIGHT));
		label.add(new JLabel("Confirm New Password:", SwingConstants.RIGHT));
		panel.add(label, BorderLayout.WEST);

		JPanel controls = new JPanel(new GridLayout(0, 1, 2, 2));
		currentPassword = new JPasswordField(15);
		controls.add(currentPassword);
		newPassword = new JPasswordField(15);
		controls.add(newPassword);
		confirmNewPassword = new JPasswordField(15);
		controls.add(confirmNewPassword);

		panel.add(controls, BorderLayout.CENTER);
	}
	
	public ChangePasswordDTO showDialog() {
		JOptionPane.showMessageDialog(null, panel, "Change Password",
				JOptionPane.QUESTION_MESSAGE);
		
		ChangePasswordDTO dto = new ChangePasswordDTO();
		dto.setCurrentPassword(new String(currentPassword.getPassword()));
		dto.setNewPassword(new String(newPassword.getPassword()));
		dto.setConfirmNewPassword(new String(confirmNewPassword.getPassword()));
		
		return dto;
	}
	
	class ChangePasswordDTO {
		private String currentPassword;
		private String newPassword;
		private String confirmNewPassword;

		public String getCurrentPassword() {
			return currentPassword;
		}

		public void setCurrentPassword(String currentPassword) {
			this.currentPassword = currentPassword;
		}

		public String getNewPassword() {
			return newPassword;
		}

		public void setNewPassword(String newPassword) {
			this.newPassword = newPassword;
		}

		public String getConfirmNewPassword() {
			return confirmNewPassword;
		}

		public void setConfirmNewPassword(String confirmNewPassword) {
			this.confirmNewPassword = confirmNewPassword;
		}
	}
}
