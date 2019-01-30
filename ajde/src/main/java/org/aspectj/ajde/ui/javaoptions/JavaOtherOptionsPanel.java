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
import java.awt.Dimension;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import org.aspectj.ajde.core.JavaOptions;
import org.aspectj.ajde.ui.swing.OptionsPanel;

/**
 * An options panel which displays the character encoding java
 * compiler option. Users should add this to the Ajde.getOptionsFrame()
 */
public class JavaOtherOptionsPanel extends OptionsPanel {

	private static final long serialVersionUID = 4491319302490183151L;
	
	private JPanel parentPanel;

	private Border otherEtchedBorder;
	private TitledBorder otherTitleBorder;
	private Border otherCompoundBorder;
	private JPanel otherPanel;
	private Box otherBox = Box.createVerticalBox();
	
	private JavaBuildOptions javaBuildOptions;

	private JTextField characterEncoding;
	
	public JavaOtherOptionsPanel(JavaBuildOptions javaBuildOptions) {
		this.javaBuildOptions = javaBuildOptions;
		try {
			jbInit();
			this.setName("Java Other Build Options");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void loadOptions() throws IOException {
		createOtherContents();
	}
	
	public void saveOptions() throws IOException {		
		String text = characterEncoding.getText();
		if (text != null ) {
			javaBuildOptions.setCharacterEncoding(text);
		}	
	}	
	
	private void jbInit() throws Exception {
		this.setLayout(new BorderLayout());
		createBorders();
		addBordersToPanel();
		this.add(parentPanel,BorderLayout.NORTH);
	}

	private void createOtherContents() {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());

		JLabel label = new JLabel();
		label.setFont(new java.awt.Font("Dialog", 0, 11));
		label.setText("Character encoding (will default to platform encoding)");
		panel.add(label,BorderLayout.WEST);
		
		characterEncoding  = new JTextField();	
		characterEncoding.setFont(new java.awt.Font("SansSerif", 0, 11));
		characterEncoding.setMinimumSize(new Dimension(100, 21));
		characterEncoding.setPreferredSize(new Dimension(150, 21));
		panel.add(characterEncoding,BorderLayout.EAST);
		
		String option = (String) javaBuildOptions.getJavaBuildOptionsMap().get(
				JavaOptions.CHARACTER_ENCODING);
		if (option != null) {
			characterEncoding.setText(option);
		}
		
		otherBox.add(panel,null);
		otherPanel.add(otherBox);
	}
	
	private void createBorders() {
		otherEtchedBorder = BorderFactory.createEtchedBorder(Color.white, new Color(156, 156, 158));
		otherTitleBorder = new TitledBorder(otherEtchedBorder, "Other Options");
		otherCompoundBorder = BorderFactory.createCompoundBorder(otherTitleBorder,
				BorderFactory.createEmptyBorder(5, 5, 5, 5));	
		otherTitleBorder.setTitleFont(new java.awt.Font("Dialog", 0, 11));
	}
	
	private void addBordersToPanel() {
		parentPanel = new JPanel();
		parentPanel.setLayout(new BorderLayout());
		
		otherPanel = new JPanel();
		otherPanel.setBorder(otherCompoundBorder);
		parentPanel.add(otherPanel,BorderLayout.CENTER);
	}


}
