package ca.ubc.cs.spl.aspectPatterns.examples.mediator.java;

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
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/** 
 * Basically a <code>JButton</code> with an <code>ActionListener</code>. 
 * The listener calls <code>clicked()</code> when the button gets pressed. 
 *
 * @author  Jan Hannemann
 * @author  Gregor Kiczales
 * @version 1.1, 02/12/04
 */

public class Button extends JButton implements GUIColleague {   
    
    private GUIMediator mediator;
    
    /**
     * Creates a new <code>Button</code> object with the provided label.
     *
     * @param name the label for the new <code>Button</code> object 
     */
 
 	public Button(String name) {
		super(name);
		this.setActionCommand(name);
		this.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				clicked(); 
			}
		}); 
	}
	
	public void clicked() {
	    mediator.colleagueChanged(this);
	}  
	
    /**
     * Allows to set the <i>Mediator</i> for this <i>Colleague</i>
     *
     * @param mediator the new mediator
     */
    
	public void setMediator(GUIMediator mediator) {
	    this.mediator = mediator;
	}
}