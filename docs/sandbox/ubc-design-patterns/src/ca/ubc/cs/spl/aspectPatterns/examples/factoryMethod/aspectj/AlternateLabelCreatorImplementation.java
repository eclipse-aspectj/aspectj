package ca.ubc.cs.spl.aspectPatterns.examples.factoryMethod.aspectj;

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
import javax.swing.JComponent;
/**
 * This aspect changes the behavior of a <i>Factory Method</i> using 
 * <code>around</code> advice. With this approach it is possible to 
 * have the factories create different products depending on the 
 * aspects woven into the project. For example, this could be used
 * as a very basic approach to software configuration management.
 * 
 * In this case, two slightly different label products are produced, 
 * depending on whether this aspect is woven into the system or not.
 *
 * @author  Jan Hannemann
 * @author  Gregor Kiczales
 * @version 1.11, 04/01/04
 */ 

public aspect AlternateLabelCreatorImplementation {
	
	/** 
	 * Describes the factory method for which we want to 
	 * modify the product
	 */
	
	pointcut labelCreation(): 
		execution(JComponent LabelCreator.createComponent());
	
	/**
	 * Creates the product, modifies it and passes the 
	 * modified product on.
	 */
	
	JComponent around(): labelCreation() {
		JLabel label = (JLabel) proceed();
		label.setText("This is an alternate JLabel");
		return label;
	}
}