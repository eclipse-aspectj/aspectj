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
 * An options panel which displays the java compiler debug options.
 * Users should add this to the Ajde.getOptionsFrame()
 */
public class JavaDebugOptionsPanel extends OptionsPanel {

	private final String[] debugOptions = new String[] {JavaOptions.GENERATE,JavaOptions.DO_NOT_GENERATE};
	private final String[] preserveOptions = new String[] {JavaOptions.PRESERVE,JavaOptions.OPTIMIZE};

	private static final long serialVersionUID = 4491319302490183151L;

	private JPanel parentPanel;

	private Border debugEtchedBorder;
	private TitledBorder debugTitleBorder;
	private Border debugCompoundBorder;
	private JPanel debugPanel;
	private Box debugBox = Box.createVerticalBox();

	private JavaBuildOptions javaBuildOptions;

	private Map<String,JComboBox<String>> debugComboBoxes = new HashMap();

	public JavaDebugOptionsPanel(JavaBuildOptions javaBuildOptions) {
		this.javaBuildOptions = javaBuildOptions;
		try {
			jbInit();
			this.setName("Java Debug Options");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void loadOptions() throws IOException {
		createDebugContents();
	}

	@Override
	public void saveOptions() throws IOException {
		Set<Map.Entry<String,JComboBox<String>>> s = debugComboBoxes.entrySet();
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

	private void createDebugContents() {
		createDebugEntry("Add line number attributes to generated class files",JavaOptions.DEBUG_LINES);
		createDebugEntry("Add source file name to generated class file",JavaOptions.DEBUG_SOURCE);
		createDebugEntry("Add variable attributes to generated class files",JavaOptions.DEBUG_VARS);
		createDebugEntry("Preserve unused (never read) local variables",JavaOptions.PRESERVE_ALL_LOCALS);
		debugPanel.add(debugBox);
	}

	private void createDebugEntry(String labelText, String javaOptionToSet) {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());

		JLabel label = new JLabel();
		label.setFont(new java.awt.Font("Dialog", 0, 11));
		label.setText(labelText);
		panel.add(label,BorderLayout.WEST);

		JComboBox<String> debug = null;
		if (javaOptionToSet.equals(JavaOptions.PRESERVE_ALL_LOCALS)) {
			debug = new JComboBox<>(preserveOptions);
			String value = javaBuildOptions.getJavaBuildOptionsMap().get(javaOptionToSet);
			if (value.equals(JavaOptions.PRESERVE)) {
				debug.setSelectedIndex(0);
			} else {
				debug.setSelectedIndex(1);
			}
		} else {
			debug = new JComboBox<>(debugOptions);
			String value = javaBuildOptions.getJavaBuildOptionsMap().get(javaOptionToSet);
			if (value.equals(JavaOptions.GENERATE)) {
				debug.setSelectedIndex(0);
			} else {
				debug.setSelectedIndex(1);
			}
		}
		panel.add(debug,BorderLayout.EAST);
		debugBox.add(panel,null);
		debugComboBoxes.put(javaOptionToSet,debug);
	}


	private void createBorders() {
		debugEtchedBorder = BorderFactory.createEtchedBorder(Color.white, new Color(156, 156, 158));
		debugTitleBorder = new TitledBorder(debugEtchedBorder, "Debug Options");
		debugCompoundBorder = BorderFactory.createCompoundBorder(debugTitleBorder,
				BorderFactory.createEmptyBorder(5, 5, 5, 5));
		debugTitleBorder.setTitleFont(new java.awt.Font("Dialog", 0, 11));
	}

	private void addBordersToPanel() {
		parentPanel = new JPanel();
		parentPanel.setLayout(new BorderLayout());

		debugPanel = new JPanel();
		debugPanel.setBorder(debugCompoundBorder);

		parentPanel.add(debugPanel,BorderLayout.CENTER);
	}


}
