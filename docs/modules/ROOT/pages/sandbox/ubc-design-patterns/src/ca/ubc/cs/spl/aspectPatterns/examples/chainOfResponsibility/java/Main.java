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

/**
 * Implements a GUI-motivated example for the Chain Of Rspsonsibility design
 * pattern.<p> 
 *
 * Intent: <i>Avoid coupling the sender of a request to its receiver by giving
 * more than one object a chance to handle the request. Chain the receiving 
 * objects and pass the request along the chain until an object handles it.
 * </i><p>
 *
 * Participatng objects are a <code>Frame</code>, a <code>Panel</code>, and 
 * <code>Button</code>
 *
 * A click on the button triggers an event (request) that gets passed along
 * the widget hierarchy (button -> panel -> frame).
 *
 * The <code>Handler</code> interface defines the <code>handleRequest()</code>
 * method for asking an object if it is willing to handle the request.  
 *
 * If an object chooses not to handle a click, the event gets forwarded to
 * the the object's successor. If such a successor does not exist, an 
 * appropriate message is shown. 
 *
 * <p><i>This is the Java version.</i><p>    
 *
 * In this version, it is not possible to extract the common code for 
 * the case that the object does not want to handle the click into the 
 * <code>ClickHandler</code> interface. The reason for this is that this would
 * turn <code>ClickHandler</code> into an abstract class. Since Java
 * does not support multiple inheritance and the individual <code>
 * ConcreteHandlers</code> already extend other classes (GUI elements here),
 * this would not work. The result is some code duplication in the
 * implementation of the <code>handleClick(..)</code> methods.
 *
 * @author  Jan Hannemann
 * @author  Gregor Kiczales
 * @version 1.1, 01/27/04
 * 
 * @see Button
 * @see Panel
 * @see Frame
 * @see Handler
 */

public class Main {
	
    /**
     * Implements the driver for the chain of responisbility example. 
     * It creates a simple GUI consisting of a <code>Button</code> in a 
     * <code>Panel</code> in a <code>Frame</code>. 
     *
     * Clicking the button will start a request, that gets passed on
     * along the following chain: button, panel, frame. Depending on 
     * whether the ALT, SHIFT, or CTRL keys are pressed during the 
     * button click, a different object in the chain will handle the
     * request:
     * 
     * <ol>
     * 		<li> If the SHIFT key is pressed, Button will handle the request
     * 		<li> If the CTRL  key is pressed, Panel  will handle the request
     * 		<li> If the ALT   key is pressed, Frame  will handle the request
     * 		<li> If no keys are pressed, the request will not be handled and
     * 			 an exception will be raised.
     * </ol>
     */ 

	public static void main(String[] args) {  
	    
		Frame  frame  = new Frame("Chain of Responsibility");
		Panel  panel  = new Panel(frame);
		Button button = new Button("Click me to see the pattern in action! Use <SHIFT>, <CTRL>, and <ALT> during clicks to see different behavior", panel); 
		
		frame.getContentPane().add(panel);
		panel.add(button);
		
		frame.pack();
		frame.setVisible(true);
	}
}