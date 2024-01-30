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
 * For more details and the latest version of this code, please see:
 * http://www.cs.ubc.ca/labs/spl/projects/aodps.html
 *
 * Contributor(s):   
 */
 
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JLabel; 
import javax.swing.JButton; 
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * Sets up and displays a new GUI given a concrete factory.
 *
 * @author  Jan Hannemann
 * @author  Gregor Kiczales
 * @version 1.1, 01/20/04
 *
 */
 
public class Display extends JFrame  { 
    
    /**
     * Sets up the frame with a label and a button created by the respective
     * <i>ConcreteFactory</i>. Both button and frame receive their appropriate 
     * listeners to close the frame when either the button is clicked or
     * the frame is closing.
     *
     * @param factory the concrete factory to use for creating GUI elements
     */ 

	Display(ComponentFactory factory) {
		super("New GUI"); 
		JLabel label = factory.createLabel(); 
		JButton button = factory.createButton("OK");
		button.addActionListener(new myActionListener(this));
		JPanel panel = new JPanel();
		panel.add(label);
		panel.add(button);
		this.getContentPane().add(panel);
		this.pack();
		this.setVisible(true); 
		this.addWindowListener(new myWindowListener(this));
	}
	
	/**
	 * Adds a window listener that closes the frame on demand 
	 */

	private class myWindowListener extends WindowAdapter {
		
		Display display = null;
		
		protected myWindowListener(Display display) {
			super();
			this.display = display;   
		}
		
		public void windowClosing(WindowEvent e) {
			display.setVisible(false);
		}
	}
	
	/**
	 * Adds a button listener that closes the frame on demand 
	 */

	private class myActionListener implements ActionListener {
	    
	    Display display;
	    
	    protected myActionListener(Display display) {
	        super();
	        this.display = display;
	    }
	    
	    public void actionPerformed(ActionEvent e) { 
			display.setVisible(false);
		}
	}
}