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
  
import javax.swing.JFrame;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;  


/** 
 * Represents a regular GUI frame modified to play its role in the 
 * <i>Chain of Responisiblity</i> pattern, i.e. to handle requests and/or
 * forward them to its successor in the chain.
 * 
 * Requests are only handled if the ALT key is pressed during a click.
 *
 * @author  Jan Hannemann
 * @author  Gregor Kiczales
 * @version 1.1, 01/27/04
 *
 */

public class Frame extends JFrame implements ClickHandler {

    /**
     * the successor in the chain of responsibility
     */

	protected ClickHandler successor;


    /** 
     * Creates a <code>Frame</code> with a given title that responds 
     * properly to <code>WindowClosing<code> events. The frame
     * does not have a successor and handles request that it receives.
     *
     * @param title the frame title
     */	 
     
 	public Frame(String title) {
		super(title);
		
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
	}  
	
    /** 
     * Implements the method to handle requests as defined by the
     * <code>ClickHandler</code> interface. The request is only handled here
     * if the ALT key was pressed.
     * 
     * @see ClickHandler
     */	 

	public void handleClick(Click click) {
		System.out.println("Frame is asked to handle the request...");
		if (click.hasAltMask()) {
			System.out.println("Frame handles the request.");
		} else {
			if (successor == null) {
				throw new RuntimeException("request unhandled (end of chain reached)");
			} else {
				successor.handleClick(click);
			}
		} 
	}
}

	
	