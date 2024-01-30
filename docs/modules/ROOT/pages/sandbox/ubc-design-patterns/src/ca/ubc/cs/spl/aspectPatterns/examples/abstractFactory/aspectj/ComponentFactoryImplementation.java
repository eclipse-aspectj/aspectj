package ca.ubc.cs.spl.aspectPatterns.examples.abstractFactory.aspectj;

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
import javax.swing.JButton; 

/**
 * Illustrates AspectJ's inter-type declarations: a default implementation 
 * of the two factory methods declared in the <code>AbstractFactory</code> 
 * interface is provided here. 
 *
 * @author Jan Hannemann
 * @author Gregor Kiczales
 * @version 1.1, 01/20/04
 *
 */

public aspect ComponentFactoryImplementation {

   /**
     * Provides a default implementation for all <code>ComponentFactories
     * </code> for the <code>createLabel()</code> method.  
     *
     * @return a regular <code>JLabel</code>
     */
 
 	public JLabel ComponentFactory.createLabel() {
		return new JLabel("This Label was created by " +getName());
	}
	
    /**
     * Provides a default implementation for all <code>ComponentFactories
     * </code> for the <code>createButton()</code> method.  
     *
     * @param  a label for the new <code>JButton</code>
     * @return a regular <code>JButton</code>
     */
 
    public JButton ComponentFactory.createButton(String label) {
		return new JButton(label);
	}	
}