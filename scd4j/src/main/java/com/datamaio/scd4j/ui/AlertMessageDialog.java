package com.datamaio.scd4j.ui;

import java.awt.Font;
import java.awt.GridLayout;
import java.io.File;
import java.math.BigDecimal;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.datamaio.scd4j.conf.Env;

/** 
 * Class that helps the creation of {@link JOptionPane}
 * 
 * @author Mateus M. da Costa
 */
public class AlertMessageDialog {

	private JPanel panel;

	public AlertMessageDialog(String version, String gradleVersion, String javaVersion, String packName,
			BigDecimal packVersion, Env env, File config, File[] modules) {
		panel = new JPanel();
		panel.setBorder(BorderFactory.createTitledBorder("Running scd4j"));
		panel.setLayout(new GridLayout(4, 1));

		buildVersionInfoPanel(version, gradleVersion, javaVersion, packName, packVersion.toEngineeringString());

		buildEnvironmentPanel(env.getProductionIps(), env.getStagingIps(), env.getTestingIps());

		instalationPanel(config.getName(), getModulesAsString(modules));
		JLabel question = new JLabel("Review the above config. Click YES to procceed and NO to abort: ", SwingConstants.LEFT);
		question.setFont(new Font("Arial", Font.BOLD, 14));
		
		
		panel.add(question);
	}
	
	private String getModulesAsString(File[] modules) {
		StringBuilder module = new StringBuilder();
		module.append("[");
		boolean first = true;
		for(File file : modules) {
			if(!first) {
				module.append(" ,");
				module.append(file.getName());
			}else{
				first = false;
				module.append(file.getName());
			}
		}
		module.append("]");
		return module.toString();
	}

	private void instalationPanel(String config, String modules) {
		JPanel instalation = new JPanel();
		instalation.setBorder(BorderFactory
				.createTitledBorder("Instalation Configuration"));
		panel.add(instalation);
		instalation.setLayout(new GridLayout(2, 2));

		instalation.add(new JLabel("CONFIG FILE   :", SwingConstants.LEFT));
		instalation.add(new JLabel(config, SwingConstants.LEFT));
		instalation.add(new JLabel("MODULE DIRS   :", SwingConstants.LEFT));
		instalation.add(new JLabel(modules, SwingConstants.LEFT));
	}

	private void buildEnvironmentPanel(String production, String staging,
			String testing) {
		JPanel environment = new JPanel();
		environment.setBorder(BorderFactory
				.createTitledBorder("Environment Configuration"));
		panel.add(environment);
		environment.setLayout(new GridLayout(4, 2, 15, 0));

		environment.add(new JLabel("PRODUCTION LIST 	:", SwingConstants.LEFT));
		environment.add(new JLabel(production, SwingConstants.LEFT));
		environment.add(new JLabel("IP STAGIN  LIST  	:", SwingConstants.LEFT));
		environment.add(new JLabel(staging, SwingConstants.LEFT));
		environment.add(new JLabel("IP TESTING LIST  	:", SwingConstants.LEFT));
		environment.add(new JLabel(testing, SwingConstants.LEFT));
		environment.add(new JLabel("IP DESENV  LIST  	:", SwingConstants.LEFT));
		environment.add(new JLabel("[ANY OTHER]", SwingConstants.LEFT));
	}

	private void buildVersionInfoPanel(String version, String gradleVersion, String javaVersion, String packName,
			String packVersion) {
		JPanel versionInfoPanel = new JPanel();
		versionInfoPanel.setBorder(BorderFactory
				.createTitledBorder("Version Info"));
		panel.add(versionInfoPanel);
		versionInfoPanel.setLayout(new GridLayout(5, 2));

		versionInfoPanel
				.add(new JLabel("SCD4J Version :", SwingConstants.LEFT));
		versionInfoPanel.add(new JLabel(version, SwingConstants.LEFT));
		versionInfoPanel
		.add(new JLabel("Gradle Version:", SwingConstants.LEFT));
		versionInfoPanel.add(new JLabel(gradleVersion, SwingConstants.LEFT));
		versionInfoPanel
		.add(new JLabel("Jvm Version   :", SwingConstants.LEFT));
		versionInfoPanel.add(new JLabel(javaVersion, SwingConstants.LEFT));		
		versionInfoPanel
				.add(new JLabel("Pack Name     :", SwingConstants.LEFT));
		versionInfoPanel.add(new JLabel(packName, SwingConstants.LEFT));
		versionInfoPanel
				.add(new JLabel("Pack Version  :", SwingConstants.LEFT));
		versionInfoPanel.add(new JLabel(packVersion, SwingConstants.LEFT));

	}

	/**
	 * Show a popup in a standard dialog box that shows a multi line message
	 */
	public int showConfirmDialog() {
		return JOptionPane.showConfirmDialog(null, panel, "Warning",
				JOptionPane.YES_NO_OPTION);
	}
}
