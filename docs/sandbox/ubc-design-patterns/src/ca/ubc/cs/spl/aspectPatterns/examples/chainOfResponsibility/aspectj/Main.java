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
 * Clicking the button will start a request, that gets passed on
 * along the following chain: button, panel, frame. Depending on 
 * whether the ALT, SHIFT, or CTRL keys are pressed during the 
 * button click, a different object in the chain will handle the
 * request:
 * 
 * <ol>
 * 		<li> If the SHIFT key is pressed, Button will handle the request
 *   	<li> If the CTRL  key is pressed, Panel  will handle the request
 * 		<li> If the ALT   key is pressed, Frame  will handle the request
 * 		<li> If no keys are pressed, the request will not be handled and
 * 			 an exception will be raised.
 * </ol>
 *
 * <p><i>This is the AspectJ version.</i><p>    
 *
 * In this implementation, the former <i>ConcreteHandlers</i> do not
 * contain any pattern code at all. The shared logic for forwarding requests
 * is implemented once in the reusable abstract library aspect. The current 
 * implementation does require some casts (as generally the case in AspectJ
 * solutions that employ similar approaches), due to the lack of support for
 * generics.
 * 
 * For limitations of this approach, see the ChainOfResponsibilityProtocol
 * library aspect.
 *
 * @author  Jan Hannemann
 * @author  Gregor Kiczales
 * @version 1.1, 01/27/04
 * 
 * @see Button
 * @see Panel
 * @see Frame
 * @see MyChain
 * @see ChainOfResponsibilityProtocol
 */

public class Main {
	
    /**
     * Implements a GUI-motivated example for the Chain Of Responsibility design
     * pattern.<p> 
     *
     * In this implementation, the request is handled by the panel if the 
     * CTRL mask is active (i.e., if the CTRL key was pressed while the button 
     * was clicked). If the SHIFT mask is active, the frame handles the request.
     * Otherwise, the request is unhandled.      
     *
     * @param args command line parameters, unused
     */
     
	public static void main(String[] args) {
		Frame  frame  = new Frame("Chain of Responsibility pattern example");
		Panel  panel  = new Panel();
		Button button = new Button("Click me to see the pattern in action! Use <SHIFT>, <CTRL>, and <ALT> during clicks to see different behavior"); 
		
		ClickChain.aspectOf().setSuccessor(button, panel);
		ClickChain.aspectOf().setSuccessor(panel, frame);  
		                          
		frame.getContentPane().add(panel);
		panel.add(button);
		
		frame.pack();
		frame.setVisible(true);  
	}
}