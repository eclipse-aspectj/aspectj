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

import javax.swing.JButton;
import javax.swing.JComponent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * Implements a <i>ConcreteCreator</i> that creates buttons.
 *
 * @author  Jan Hannemann
 * @author  Gregor Kiczales
 * @version 1.1, 02/11/04
 * 
 * @see LabelCreator
 */ 
 
public class ButtonCreator implements GUIComponentCreator {

    /**
     * Factory method that creates a button with label and <code>
     * ActionListener</code>.
     *
     * @returns the created button
     */

    public JComponent createComponent() {
        final JButton button = new JButton("Click me!");
        button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				button.setText("Thank you!");
			}
        });
        return button;            
    }
    
    /**
     * Returns a title explaining this example.
     *
     * @returns the title for the GUI frame
     */

    public String getTitle() {
        return "Example 1: A JButton";
    }
}		
