package ca.ubc.cs.spl.aspectPatterns.examples.chainOfResponsibility.aspectj;

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
  
import javax.swing.*;
import java.awt.event.*;

/** 
 * A simple GUI button that implements its own ActionListener.
 *
 * @author  Jan Hannemann
 * @author  Gregor Kiczales
 * @version 1.1, 01/27/04
 *  
 */  
 
public class Button extends JButton { 

    /** 
     * Creates a Button widget. An ActionListener is also added that calls
     * the <code>doClick(Click)</code> method when the button is pressed
     *
     * @param label the button label  
     */    
     
 	public Button(String label) {
		super(label);
		this.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				doClick(new Click(ae));
			}
		});
	}      

	
    /** 
     * An empty method that is called when the button is clicked. This method
     * could also be defined in the concrete aspect.
     *
     * @param click the <code>Click</code> that was created when the 
     * button was clicked.  
     */    
 
 	public void doClick(Click click) {}
}