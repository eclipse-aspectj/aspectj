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
 
import javax.swing.*;
import java.awt.event.*;

/**
 * Implements the driver for the Mediator design pattern example.<p> 
 *
 * Intent: <i>Define an object that encapsulates how a set of objects
 * interact. Mediator promotes loose coupling by keeping objects from 
 * referring to each other explicitly, and it lets you vary their interaction
 * independently.</i><p>
 *
 * Participating objects are <code>Button</code>s as <i>Colleague</i>s,
 * and a <code>Label</code> as <i>Mediator</i>.
 *
 * Every time an event of interest (a button click) occurs, the mediating
 * <code>Label</code> is updated and it in turn updates the respective 
 * calling button.
 *
 * <p><i>This is the Java version.</i><p>    
 *
 * Both <i>Mediator</i> and <i>Colleague</i>s have to be aware of their role
 * within the pattern.
 *
 * @author  Jan Hannemann
 * @author  Gregor Kiczales
 * @version 1.1, 02/12/04
 * 
 * @see Button
 * @see Label
 */
  
public class Main {

   
    static JFrame frame   = new JFrame("Mediator Demo"); 
    static Button button1 = new Button("Button1");
    static Button button2 = new Button("Button2");
    static Label  label   = new Label ("Click a button!");

    /**
     * Implements the driver for the mediator example. It creates a small
     * GUI with a label and two buttons. The buttons are <i>Colleague</i>s,
     * the label is the <i>Mediator</i>. 
     *
     * Each button click causes the mediator to update itself and the
     * calling button.
     */ 
   
	public static void main(String[] args) {;

	    
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {System.exit(0);}
		});
		    
		JPanel panel = new JPanel();
	
		panel.add(label);
		panel.add(button1);
		panel.add(button2); 
		
		frame.getContentPane().add(panel);
		frame.pack();
		frame.setVisible(true);  
		
	    button1.setMediator(label);    
	    button2.setMediator(label);    
	}
}