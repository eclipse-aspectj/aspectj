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

import javax.swing.JComponent; 

/**
 * Defines the <i>GUIComponentCreator</i> interface with the 
 * <i>factoryMethod()</i> method signature and the <i>anOperation()</i> 
 * method that uses it. For details, see GoF, page 108.<p> 
 *
 * The factory method is <code>createComponent</code> and it creates
 * A JComponent (a button and a label, repsectively). The <i>anOperation()</i>
 * method is implemented by the aspect, allowing this type to be an interface,
 * not an abstract class
 *
 * @author  Jan Hannemann
 * @author  Gregor Kiczales
 * @version 1.1, 02/11/04
 * 
 * @see ButtonCreator
 * @see LabelCreator
 */ 
 
public interface GUIComponentCreator {
    
    /**
     * The factory method to create <code>JComponent</code>s, to be 
     * concretized by subclasses.
     *
     * @returns the created product
     */

    public JComponent createComponent(); 
    
    /**
     * Another factory method to create a title that explains the created
     * component
     *
     * @returns the title for the GUI frame
     */
     
    public String getTitle(); 
}		

    