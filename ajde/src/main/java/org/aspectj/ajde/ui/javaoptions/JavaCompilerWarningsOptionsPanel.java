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
import java.util.Map.Entry;
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
 * An options panel which displays the java compiler warning options.
 * Users should add this to the Ajde.getOptionsFrame()
 */
public class JavaCompilerWarningsOptionsPanel extends OptionsPanel {

	private final String[] ignoreOrWarning = new String[] {JavaOptions.IGNORE,JavaOptions.WARNING};	
	private static final long serialVersionUID = 4491319302490183151L;
	
	private JPanel parentPanel;
	
	private Border warningsEtchedBorder;
	private TitledBorder warningsTitleBorder;
	private Border warningsCompoundBorder;
	private JPanel warningsPanel;
	private Box warningsBox = Box.createVerticalBox();
	
	private JavaBuildOptions javaBuildOptions;

	private Map/*String --> JComboBox*/ warningComboBoxes = new HashMap();
	
	public JavaCompilerWarningsOptionsPanel(JavaBuildOptions javaBuildOptions) {
		this.javaBuildOptions = javaBuildOptions;
		try {
			jbInit();
			this.setName("Java Compiler Warning Options");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void loadOptions() throws IOException {
		createWarningContents();
	}
	
	public void saveOptions() throws IOException {	
		Set s = warningComboBoxes.entrySet();
		for (Object o : s) {
			Entry entry = (Entry) o;
			String javaOption = (String) entry.getKey();
			JComboBox combo = (JComboBox) entry.getValue();
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


	private void createWarningContents() {
		createWarningsEntry("Method with a constructor name",JavaOptions.WARN_METHOD_WITH_CONSTRUCTOR_NAME);
		createWarningsEntry("Method overriden but not package visible",JavaOptions.WARN_OVERRIDING_PACKAGE_DEFAULT_METHOD);
		createWarningsEntry("Deprecated API's",JavaOptions.WARN_DEPRECATION);
		createWarningsEntry("Hidden catch block",JavaOptions.WARN_HIDDEN_CATCH_BLOCKS);
		createWarningsEntry("Unused local or private member",JavaOptions.WARN_UNUSED_LOCALS);
		createWarningsEntry("Parameter is never read",JavaOptions.WARN_UNUSED_PARAMETER);
		createWarningsEntry("Unused import",JavaOptions.WARN_UNUSED_IMPORTS);
		createWarningsEntry("Synthetic access",JavaOptions.WARN_SYNTHETIC_ACCESS);
		createWarningsEntry("Assert identifier",JavaOptions.WARN_ASSERT_IDENITIFIER);
		createWarningsEntry("Non-externalized strings",JavaOptions.WARN_NON_NLS);
		warningsPanel.add(warningsBox,null);
	}
	
	private void createWarningsEntry(String labelText, String javaOptionToSet) {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		
		JLabel label = new JLabel();
		label.setFont(new java.awt.Font("Dialog", 0, 11));
		label.setText(labelText);
		panel.add(label,BorderLayout.WEST);
		
		JComboBox warnings = new JComboBox(ignoreOrWarning);
		String value = (String) javaBuildOptions.getJavaBuildOptionsMap().get(javaOptionToSet);
		if (value.equals(JavaOptions.IGNORE)) {
			warnings.setSelectedIndex(0);
		} else {
			warnings.setSelectedIndex(1);			
		}
		panel.add(warnings,BorderLayout.EAST);
		warningsBox.add(panel,null);
		warningComboBoxes.put(javaOptionToSet,warnings);
	}
	
	private void createBorders() {		
		warningsEtchedBorder = BorderFactory.createEtchedBorder(Color.white, new Color(156, 156, 158));
		warningsTitleBorder = new TitledBorder(warningsEtchedBorder, "Warning Options");
		warningsCompoundBorder = BorderFactory.createCompoundBorder(warningsTitleBorder,
				BorderFactory.createEmptyBorder(5, 5, 5, 5));
		warningsTitleBorder.setTitleFont(new java.awt.Font("Dialog", 0, 11));
	}
	
	private void addBordersToPanel() {
		parentPanel = new JPanel();
		parentPanel.setLayout(new BorderLayout());
		
		warningsPanel = new JPanel();
		warningsPanel.setBorder(warningsCompoundBorder);
		parentPanel.add(warningsPanel,BorderLayout.CENTER);
	}


}
