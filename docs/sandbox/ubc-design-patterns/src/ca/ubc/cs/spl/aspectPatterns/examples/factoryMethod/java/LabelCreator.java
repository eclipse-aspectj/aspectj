package ca.ubc.cs.spl.aspectPatterns.examples.factoryMethod.java;

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
 * Implements a <i>ConcreteCreator</i> that creates labels.
 *
 * @author  Jan Hannemann
 * @author  Gregor Kiczales
 * @version 1.1, 02/11/04
 * 
 * @see ButtonCreator
 */ 
 
public class LabelCreator extends GUIComponentCreator {

    /**
     * Factory method that creates a label.
     *
     * @returns the created label
     */

    public JComponent createComponent() {
        JLabel label = new JLabel("This is a JLabel.");
        return label;            
    }
    
    /**
     * Returns a title explaining this example.
     *
     * @returns the title for the GUI frame
     */

    public String getTitle() {
        return "Example 2: A JLabel";
    }
}		
