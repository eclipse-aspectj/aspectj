package ca.ubc.cs.spl.aspectPatterns.examples.command.java;

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
 * Implements a simple extension of JButton that supplies its own 
 * ActionListener and calls its own <code>clicked()</code> method
 * whenever the button is pressed. This method calls the <code>
 * executeCommand()</code> method on the button's associated <i>Command</i>
 * object.
 *
 * @author  Jan Hannemann
 * @author  Gregor Kiczales
 * @version 1.1, 02/06/04
 */


public class Button extends JButton {

    /**
     * the command object associated with this button
     */
     	
	protected Command command;
	
    /**
     * Creates a new button with the provided label
     *
     * @param label the label of the button
     */

	public Button(String label) {
		super(label);
		this.setActionCommand(label);
		this.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					clicked(); 
				}
			}); 
	}

    /**
     * Calls <code>ececuteCommand()</code> on the associated 
     * command object. This method gets called whenever the 
     * button is pressed.
     */
	
	public void clicked() {
		if (command != null) {
			command.executeCommand();
		}
	}
	
    /**
     * Sets the associated command object for this button
     *
     * @param command the new <i>Command</i> object.
     */

	public void setCommand(Command command)	{
		this.command = command;
	}  
}