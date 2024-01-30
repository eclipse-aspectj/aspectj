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
 * For more details and the latest version of this code please see
 * http://www.cs.ubc.ca/labs/spl/projects/aodps.html
 *
 * Contributor(s):   
 */

import javax.swing.JLabel;
import javax.swing.JButton; 

/**
 * This <i>Concrete Factory</i> implements the 
 * <code>AbstractFactory</code> interface to provide 
 * regular Swing GUI components.
 * 
 * The factroy methods <code>createLabel()</code> and <create>Button()</code>
 * do not need to be defined here, they recieve their implementation from
 * the inter-type declarations in aspect 
 * <code>AbstractFactroyEnhancement</code>.
 * 
 * This is done so that future concrete factories can reuse the 
 * implementations of the factory methods and will only have to specify those
 * that differ from the default ones.
 * 
 * @author Jan Hannemann
 * @author Gregor Kiczales
 * @version 1.1, 01/20/04
 *
 */
public class RegularFactory implements ComponentFactory {
	  
    /** 	
     * Returns the name of the factory.
     *
     * @return the name of the factory
     */
     	  
 	public String getName() {
		return ("Regular Factory");
	} 
}