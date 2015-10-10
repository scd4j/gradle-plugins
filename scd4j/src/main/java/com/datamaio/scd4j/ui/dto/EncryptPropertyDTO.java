package com.datamaio.scd4j.ui.dto;

/**
 * 
 * @author Mateus M. da Costa
 *
 */
public class EncryptPropertyDTO {
	
	private String property;
	private String propertyValue;
	private String newPassword;
	private String confirmNewPassword;

	public String getProperty() {
		return property;
	}
	public void setProperty(String property) {
		this.property = property;
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
	public String getPropertyValue() {
		return propertyValue;
	}
	public void setPropertyValue(String propertyValue) {
		this.propertyValue = propertyValue;
	}
}
