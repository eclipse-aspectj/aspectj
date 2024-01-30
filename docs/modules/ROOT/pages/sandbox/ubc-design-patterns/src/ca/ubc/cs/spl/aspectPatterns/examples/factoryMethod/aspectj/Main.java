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

/**
 * Implements the driver for the FactoryMethod design pattern example.<p> 
 *
 * Intent: <i>Define an interface for creating an object, but let subclasses 
 * decide which class to instantiate. Factory Method lets a class defer 
 * instantiation to subclasses.</i><p>
 *
 * Participating objects are <code>ButtonCreator</code> and 
 * <code>LabelCreator</code> as <i>ConcreteCreator</i>s. Both implement
 * the <code>GUIComponentCreator</code> interface.<p>
 *
 * In this example, the factory method <code>createComponent</code> creates
 * A JComponent (a button and a label, respectively). The <i>anOperation()</i>
 * method <code>showFrame()</code> uses the factory method to show a little
 * GUI. In one case, the created frame contains a button, in the other a
 * simple label.
 *
 * <p><i>This is the AspectJ version.</i><p>
 *
 * Since the implementation of the <i>anOperation()</i> method <code>
 * showFrame()</code> is now realized by an aspect, 
 * <code>GUIComponentCreator</code> can now be an interface, allowing 
 * the <i>ConcreteCreator</i>s to be part of a different inheritance 
 * hierarchy.
 *
 * @author  Jan Hannemann
 * @author  Gregor Kiczales
 * @version 1.1, 02/11/04
 * 
 * @see GUIComponentCreator
 * @see ButtonCreator
 * @see LabelCreator
 */

public class Main {

	/**
	 * Implements the driver for the FactoryMethod design pattern example.<p> 
	 *
	 * In this example, the factory method <code>createComponent</code> 
	 * creates a JComponent (a button and a label, repsectively). The 
	 * <i>anOperation()</i> method <code>showFrame()</code> uses the factory 
	 * methods to show a little GUI. In one case, the created frame contains 
	 * a button, in the other a simple label.
	 */    

    public static void main(String[] args) {
        
        GUIComponentCreator creator1 = new ButtonCreator();
        GUIComponentCreator creator2 = new LabelCreator();
        
        creator1.showFrame();
        creator2.showFrame();
    }
}