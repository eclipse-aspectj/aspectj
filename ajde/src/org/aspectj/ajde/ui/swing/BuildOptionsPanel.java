/* *******************************************************************
 * Copyright (c) 1999-2001 Xerox Corporation, 
 *               2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.ajde.ui.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import org.aspectj.ajde.Ajde;

/**
 * @author Mik Kersten
 */
public class BuildOptionsPanel extends OptionsPanel {

	protected static BuildOptionsPanel INSTANCE = new BuildOptionsPanel();

	private ButtonGroup compilerMode_buttonGroup = new ButtonGroup();
	private TitledBorder titledBorder1;
	private Border border3;
	private Border border4;
//	private TitledBorder titledBorder2;
	private Border border5;
	private Border border1;
	private Border border2;
	private JPanel jPanel3 = new JPanel();
	private BorderLayout borderLayout6 = new BorderLayout();
	private JPanel jPanel4 = new JPanel();
	private JPanel compileOptions_panel1 = new JPanel();
	private JPanel build_panel1 = new JPanel();
//	private JRadioButton normal_radioButton = new JRadioButton();
//	private JRadioButton strict_radioButton = new JRadioButton();
//	private JRadioButton lenient_radioButton = new JRadioButton();
	private BorderLayout borderLayout8 = new BorderLayout();
	private Box options_box1 = Box.createVerticalBox();
	private BorderLayout borderLayout5 = new BorderLayout();
	private JLabel spacer_label = new JLabel();
	private JTextField workingDir_field = new JTextField();
	private JPanel jPanel2 = new JPanel();
	private JPanel jPanel1 = new JPanel();
	private Box options_box = Box.createVerticalBox();
	private JPanel build_panel = new JPanel();
	private JTextField nonStandard_field = new JTextField();
	private JCheckBox pre1_checkBox = new JCheckBox();
	private JCheckBox assertions_checkBox = new JCheckBox();
	private JCheckBox useJavac_checkBox = new JCheckBox();
	private JCheckBox preprocess_checkBox = new JCheckBox();
	private JPanel compileOptions_panel = new JPanel();
	private JLabel workingDir_label = new JLabel();
	private JLabel nonStandard_label = new JLabel();
	private BorderLayout borderLayout4 = new BorderLayout();
	private BorderLayout borderLayout3 = new BorderLayout();
	private BorderLayout borderLayout2 = new BorderLayout();
	private Box fields_box = Box.createVerticalBox();
	private BorderLayout borderLayout1 = new BorderLayout();
	private BorderLayout borderLayout7 = new BorderLayout();

	public BuildOptionsPanel() {
		try {
			jbInit();
			this.setName("AspectJ Build Options");

//			compilerMode_buttonGroup.add(normal_radioButton);
//			compilerMode_buttonGroup.add(strict_radioButton);
//			compilerMode_buttonGroup.add(lenient_radioButton);
			
			preprocess_checkBox.setEnabled(false);
			useJavac_checkBox.setEnabled(false);
			workingDir_field.setEnabled(false);
			workingDir_label.setEnabled(false);
		} catch (Exception e) {
			Ajde.getDefault().getErrorHandler().handleError("Could not initialize GUI.", e);
		}
	}

	public void loadOptions() throws IOException {
		assertions_checkBox.setSelected(
			Ajde.getDefault().getBuildManager().getBuildOptions().getSourceOnePointFourMode()
		);
		preprocess_checkBox.setSelected(
			Ajde.getDefault().getBuildManager().getBuildOptions().getPreprocessMode()
		);
		useJavac_checkBox.setSelected(
			Ajde.getDefault().getBuildManager().getBuildOptions().getUseJavacMode()
		);
		pre1_checkBox.setSelected(
			Ajde.getDefault().getBuildManager().getBuildOptions().getPortingMode()
		);

		nonStandard_field.setText(
			Ajde.getDefault().getBuildManager().getBuildOptions().getNonStandardOptions()
		);
		workingDir_field.setText(
			Ajde.getDefault().getBuildManager().getBuildOptions().getWorkingOutputPath()
		);

//		if (Ajde.getDefault().getBuildManager().getBuildOptions().getStrictSpecMode()) {
//			strict_radioButton.setSelected(true);
//		} else if (Ajde.getDefault().getBuildManager().getBuildOptions().getLenientSpecMode()) {
//			lenient_radioButton.setSelected(true);
//		} else {
//			normal_radioButton.setSelected(true);
//		}
	}

	public void saveOptions() throws IOException {
		AjdeUIManager.getDefault().getBuildOptions().setSourceOnePointFourMode(
			assertions_checkBox.isSelected()
		);
		AjdeUIManager.getDefault().getBuildOptions().setPreprocessMode(
			preprocess_checkBox.isSelected()
		);
		AjdeUIManager.getDefault().getBuildOptions().setUseJavacMode(
			useJavac_checkBox.isSelected()
		);
		AjdeUIManager.getDefault().getBuildOptions().setPortingMode(
			pre1_checkBox.isSelected()
		);

		AjdeUIManager.getDefault().getBuildOptions().setNonStandardOptions(
			nonStandard_field.getText()
		);
		AjdeUIManager.getDefault().getBuildOptions().setWorkingDir(
			workingDir_field.getText()
		);

//		AjdeUIManager.getDefault().getBuildOptions().setStrictSpecMode(strict_radioButton.isSelected());
//		AjdeUIManager.getDefault().getBuildOptions().setLenientSpecMode(lenient_radioButton.isSelected());
	}

	public static BuildOptionsPanel getDefault() {
		return INSTANCE;
	}

	private void jbInit() throws Exception {
		titledBorder1 =
			new TitledBorder(
				BorderFactory.createEtchedBorder(Color.white, new Color(156, 156, 158)),
				"ajc Options");
		border3 =
			BorderFactory.createCompoundBorder(
				new TitledBorder(
					BorderFactory.createEtchedBorder(Color.white, new Color(156, 156, 158)),
					"ajc Options"),
				BorderFactory.createEmptyBorder(5, 5, 5, 5));
		border4 =
			BorderFactory.createEtchedBorder(Color.white, new Color(156, 156, 158));
//		titledBorder2 =
//			new TitledBorder(
//				BorderFactory.createEtchedBorder(Color.white, new Color(156, 156, 158)),
//				"ajc Strictness Mode");
//		border5 =
//			BorderFactory.createCompoundBorder(
//				titledBorder2,
//				BorderFactory.createEmptyBorder(5, 5, 5, 5));
//		border1 =
//			BorderFactory.createCompoundBorder(
//				titledBorder2,
//				BorderFactory.createEmptyBorder(5, 5, 5, 5));
		border2 =
			BorderFactory.createCompoundBorder(
				titledBorder1,
				BorderFactory.createEmptyBorder(5, 5, 5, 5));
		titledBorder1.setTitle("ajc Options");
		titledBorder1.setTitleFont(new java.awt.Font("Dialog", 0, 11));
//		titledBorder2.setTitleFont(new java.awt.Font("Dialog", 0, 11));
		this.setLayout(borderLayout6);
		compileOptions_panel1.setLayout(borderLayout8);
		build_panel1.setLayout(borderLayout5);
		build_panel1.setFont(new java.awt.Font("Dialog", 0, 11));
		build_panel1.setBorder(border1);
		build_panel1.setMaximumSize(new Dimension(2147483647, 109));
//		normal_radioButton.setFont(new java.awt.Font("Dialog", 0, 11));
//		normal_radioButton.setText("Normal");
//		strict_radioButton.setText(
//			"Be extra strict in interpreting the Java specification");
//		strict_radioButton.setFont(new java.awt.Font("Dialog", 0, 11));
//		lenient_radioButton.setText(
//			"Be lenient in interpreting the Java specification");
//		lenient_radioButton.setFont(new java.awt.Font("Dialog", 0, 11));
		spacer_label.setText("   ");
		workingDir_field.setFont(new java.awt.Font("SansSerif", 0, 11));
		workingDir_field.setMinimumSize(new Dimension(200, 21));
		workingDir_field.setPreferredSize(new Dimension(210, 21));
		jPanel2.setLayout(borderLayout3);
		jPanel1.setLayout(borderLayout2);
		build_panel.setLayout(borderLayout4);
		build_panel.setBorder(border2);
		nonStandard_field.setFont(new java.awt.Font("SansSerif", 0, 11));
		nonStandard_field.setMinimumSize(new Dimension(100, 21));
		nonStandard_field.setPreferredSize(new Dimension(210, 21));
		pre1_checkBox.setText(
			"Signal warnings for pre-1.0 language use to ease porting");
		pre1_checkBox.setToolTipText("");
		pre1_checkBox.setFont(new java.awt.Font("Dialog", 0, 11));
		assertions_checkBox.setFont(new java.awt.Font("Dialog", 0, 11));
		assertions_checkBox.setText("Support assertions from 1.4 Java specification");
		useJavac_checkBox.setText("Use javac to generate .class files");
		useJavac_checkBox.setFont(new java.awt.Font("Dialog", 0, 11));
		preprocess_checkBox.setFont(new java.awt.Font("Dialog", 0, 11));
		preprocess_checkBox.setToolTipText("");
		preprocess_checkBox.setText("Only preprocess and generate .java source files");
		compileOptions_panel.setLayout(borderLayout1);
		nonStandard_label.setText("Non-standard compiler options:");
		nonStandard_label.setFont(new java.awt.Font("Dialog", 0, 11));
		nonStandard_label.setPreferredSize(new Dimension(100, 16));
		nonStandard_label.setToolTipText("");
		jPanel3.setLayout(borderLayout7);
		workingDir_label.setFont(new java.awt.Font("Dialog", 0, 11));
		workingDir_label.setPreferredSize(new Dimension(150, 16));
		workingDir_label.setText("Working directory (for preprocess and use-javac): ");
		this.add(jPanel3, BorderLayout.NORTH);
		jPanel2.add(workingDir_label, BorderLayout.CENTER);
		jPanel2.add(workingDir_field, BorderLayout.EAST);
		fields_box.add(jPanel1, null);
		fields_box.add(jPanel2, null);
		jPanel1.add(nonStandard_label, BorderLayout.CENTER);
		jPanel1.add(nonStandard_field, BorderLayout.EAST);
		compileOptions_panel.add(options_box, BorderLayout.NORTH);
		compileOptions_panel.add(fields_box, BorderLayout.SOUTH);
		options_box.add(assertions_checkBox, null);
		options_box.add(preprocess_checkBox, null);
		options_box.add(useJavac_checkBox, null);
		//options_box.add(pre1_checkBox, null);
		options_box.add(spacer_label, null);
		jPanel3.add(build_panel, BorderLayout.CENTER);
		build_panel.add(compileOptions_panel, BorderLayout.NORTH);
		jPanel3.add(build_panel1, BorderLayout.SOUTH);
		build_panel1.add(compileOptions_panel1, BorderLayout.NORTH);
		compileOptions_panel1.add(options_box1, BorderLayout.NORTH);
//		options_box1.add(normal_radioButton, null);
//		options_box1.add(lenient_radioButton, null);
//		options_box1.add(strict_radioButton, null);
		this.add(jPanel4, BorderLayout.CENTER);
	}
}
