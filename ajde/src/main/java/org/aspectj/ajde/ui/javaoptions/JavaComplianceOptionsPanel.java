/********************************************************************
 * Copyright (c) 2007 Contributors. All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://eclipse.org/legal/epl-v10.html
 *
 * Contributors: IBM Corporation - initial API and implementation
 * 				 Helen Hawkins   - initial version (bug 148190)
 *******************************************************************/
package org.aspectj.ajde.ui.javaoptions;

import java.awt.BorderLayout;
import java.awt.Color;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import org.aspectj.ajde.core.JavaOptions;
import org.aspectj.ajde.ui.swing.OptionsPanel;

/**
 * An options panel which displays the java compiler compliance options.
 * Users should add this to the Ajde.getOptionsFrame()
 */
public class JavaComplianceOptionsPanel extends OptionsPanel {

	private final String[] complianceLevels = new String[] {JavaOptions.VERSION_13, JavaOptions.VERSION_14, JavaOptions.VERSION_15, JavaOptions.VERSION_16};

	private static final long serialVersionUID = 4491319302490183151L;

	private JPanel parentPanel;

	private Border complianceEtchedBorder;
	private TitledBorder complianceTitleBorder;
	private Border complianceCompoundBorder;
	private JPanel compliancePanel;
	private Box complianceBox = Box.createVerticalBox();

	private JavaBuildOptions javaBuildOptions;

	private Map<String,JComboBox<String>> complianceComboBoxes = new HashMap<>();

	public JavaComplianceOptionsPanel(JavaBuildOptions javaBuildOptions) {
		this.javaBuildOptions = javaBuildOptions;
		try {
			jbInit();
			this.setName("Java Compliance Options");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void loadOptions() throws IOException {
		createComplianceContents();
	}

	@Override
	public void saveOptions() throws IOException {
		Set<Map.Entry<String,JComboBox<String>>> s = complianceComboBoxes.entrySet();
		for (Map.Entry<String,JComboBox<String>> entry : s) {
			String javaOption = entry.getKey();
			JComboBox<String> combo = entry.getValue();
			String value = (String) combo.getSelectedItem();
			javaBuildOptions.setOption(javaOption, value);
		}
	}

	private void jbInit() throws Exception {
		this.setLayout(new BorderLayout());
		createBorders();
		addBordersToPanel();
		this.add(parentPanel,BorderLayout.NORTH);
	}


	private void createComplianceContents() {
		createComplianceEntry("AjCompiler compliance level: ",JavaOptions.COMPLIANCE_LEVEL);
		createComplianceEntry("Source compatibility: ",JavaOptions.SOURCE_COMPATIBILITY_LEVEL);
		createComplianceEntry("Generated class file compatibility: ",JavaOptions.TARGET_COMPATIBILITY_LEVEL);
		compliancePanel.add(complianceBox);
	}

	private void createComplianceEntry(String labelText, String javaOptionToSet) {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());

		JLabel label = new JLabel();
		label.setFont(new java.awt.Font("Dialog", 0, 11));
		label.setText(labelText);
		panel.add(label,BorderLayout.WEST);

		JComboBox<String> levels = new JComboBox<>(complianceLevels);
		String value = javaBuildOptions.getJavaBuildOptionsMap().get(javaOptionToSet);
		if (value == null) {
			// default to 1.5
			levels.setSelectedIndex(2);
		} else if (value.equals(JavaOptions.VERSION_13)) {
			levels.setSelectedIndex(0);
		} else if (value.equals(JavaOptions.VERSION_14)){
			levels.setSelectedIndex(1);
		} else if (value.equals(JavaOptions.VERSION_15)){
			levels.setSelectedIndex(2);
		} else if (value.equals(JavaOptions.VERSION_16)){
			levels.setSelectedIndex(3);
		}
		panel.add(levels,BorderLayout.EAST);
		complianceBox.add(panel,null);
		complianceComboBoxes.put(javaOptionToSet,levels);
	}


	private void createBorders() {
		complianceEtchedBorder = BorderFactory.createEtchedBorder(Color.white, new Color(156, 156, 158));
		complianceTitleBorder = new TitledBorder(complianceEtchedBorder, "Compliance Options");
		complianceCompoundBorder = BorderFactory.createCompoundBorder(complianceTitleBorder,
				BorderFactory.createEmptyBorder(5, 5, 5, 5));
		complianceTitleBorder.setTitleFont(new java.awt.Font("Dialog", 0, 11));
	}

	private void addBordersToPanel() {
		parentPanel = new JPanel();
		parentPanel.setLayout(new BorderLayout());

		compliancePanel = new JPanel();
		compliancePanel.setBorder(complianceCompoundBorder);

		parentPanel.add(compliancePanel,BorderLayout.CENTER);
	}


}
