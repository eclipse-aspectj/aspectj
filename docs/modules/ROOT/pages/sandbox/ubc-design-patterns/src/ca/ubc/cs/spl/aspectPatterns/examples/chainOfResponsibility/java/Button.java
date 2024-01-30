package ca.ubc.cs.spl.aspectPatterns.examples.chainOfResponsibility.java;

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
 * GUI element at the start of the responsibility chain. A click on the 
 * button starts a request. The <code>Button</code> will only handle the
 * request if the SHIFT key was pressed when the button was clicked.
 *
 * @author  Jan Hannemann
 * @author  Gregor Kiczales
 * @version 1.1, 01/27/04
 *
 */

public class Button extends JButton implements ClickHandler { 
	
    /**
     * the successor in the chain of responsibility
     */

	protected ClickHandler successor;

    /** 
     * Creates a <code>Button</code> with a given label and successor. 
     *
     * @param label The button label
     * @param successor The successor in the chain of responsibility
     */	 
     
    public Button(String label, ClickHandler successor) {
		super(label);
		this.successor = successor; 
		this.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				handleClick(new Click(ae));
			}
		});
	}      
	
    /** 
     * Implements the method to handle requests as defined by the
     * <code>ClickHandler</code> interface. The request is only handled here
     * if the SHIFT key was pressed.
     * 
     * @see ClickHandler
     */	 
     
	public void handleClick(Click click) {
		System.out.println("Button is asked to handle the request...");
		if (click.hasShiftMask()) {
			System.out.println("Button handles the request.");
		} else {
			if (successor == null) {
				throw new RuntimeException("request unhandled (end of chain reached)");
			} else {
				successor.handleClick(click);
			}
		} 
	}
}
	
	