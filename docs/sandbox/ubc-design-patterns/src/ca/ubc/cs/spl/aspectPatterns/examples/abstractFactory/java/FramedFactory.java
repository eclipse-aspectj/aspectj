package ca.ubc.cs.spl.aspectPatterns.examples.abstractFactory.java;

/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 *
 * This file is part of the design patterns project at UBC
 *
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * either http://www.mozilla.org/MPL/ or http://aspectj.org/MPL/.
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is ca.ubc.cs.spl.aspectPatterns.
 * 
 * For more details and the latest version of this code, please see:
 * http://www.cs.ubc.ca/labs/spl/projects/aodps.html
 *
 * Contributor(s):   
 */
 
import javax.swing.JLabel; 
import javax.swing.border.Border;
import javax.swing.BorderFactory;
import javax.swing.JButton;

/**
 * This <i>Concrete Factory</i> implements the 
 * <code>ComponentFactory</code> interface to provide 
 * framed Swing GUI components.
 * 
 * @author Jan Hannemann
 * @author Gregor Kiczales
 * @version 1.1, 01/20/04
 *
 */

public class FramedFactory implements ComponentFactory {

	/**
	 * Factory method to create framed <code>JLabel</code> objects. 
	 *
	 * @return the framed <code>JLabel</code>
	 */
 

	public JLabel createLabel() {
		JLabel label = new JLabel("This Label was created by " +getName());
		Border raisedbevel = BorderFactory.createRaisedBevelBorder();
		Border loweredbevel = BorderFactory.createLoweredBevelBorder();
		label.setBorder(BorderFactory.createCompoundBorder(raisedbevel, loweredbevel));
		
		return label;
	} 
	
	/**
	 * Factory method to create framed <code>JButton</code> objects. 
	 *
	 * @param  the label for the new <code>JButton</code>
	 * @return the framed <code>JButton</code>
	 */
 
     public JButton createButton(String label) {
		JButton button = new JButton(label);
		Border raisedbevel = BorderFactory.createRaisedBevelBorder();
		Border loweredbevel = BorderFactory.createLoweredBevelBorder();
		button.setBorder(BorderFactory.createCompoundBorder(raisedbevel, loweredbevel));
		return button;
	}	

    /** 	
     * Returns the name of the factory.
     *
     * @return the name of the factory
     */
 
 	public String getName() {
		return "Framed Factory";
	} 
}