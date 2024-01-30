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
 
import java.awt.event.ActionEvent; 
import ca.ubc.cs.spl.aspectPatterns.patternLibrary.ChainOfResponsibilityProtocol;

/**
 * Implements an instance of the abstracted ChainOfResponsibility design
 * pattern. Here, the a click on the button triggers an event (request) 
 * that gets passed along the widget hierarchy (button -> panel -> frame). 
 * 
 * In this implementation, the request is handled by the panel if the 
 * CTRL mask is active (i.e., if the CTRL key was pressed while the button 
 * was clicked). If the SHIFT mask is active, the frame handles the request.
 * Otherwise, the request is unhandled.
 *
 * @author  Jan Hannemann
 * @author  Gregor Kiczales
 * @version 1.1, 01/27/04
 *
 */
  
public aspect ClickChain extends ChainOfResponsibilityProtocol {

    /**
     * Frame, Panel and Button are all Handlers
     */

	declare parents: Frame       implements Handler;
	declare parents: Panel       implements Handler;
	declare parents: Button      implements Handler; 

    declare parents: Click       implements Request; 
 
 
    protected pointcut eventTrigger(Handler handler, Request request): 
    	call(void Button.doClick(Click)) && target(handler) && args(request);
 

    public boolean Button.acceptRequest(Request request) {
    	System.out.println("Button is asked to accept the request...");
		if (request instanceof Click) {
			Click click = (Click) request;
			return (click.hasShiftMask());
		} 
        return false;
    } 
    
    public void Button.handleRequest(Request request) {
		System.out.println("Button is handling the event.\n");
    }
    
    
    public boolean Panel.acceptRequest(Request request) { 
		System.out.println("Panel is asked to accept the request...");
        if (request instanceof Click) {
            Click click = (Click) request;
            return (click.hasCtrlMask());
        } 
        return false;
    } 
    
    public void Panel.handleRequest(Request event) {
		System.out.println("Panel is handling the event.\n");
    }


	public boolean Frame.acceptRequest(Request request) { 
		System.out.println("Frame is asked to accept the request...");
		if (request instanceof Click) {
			Click click = (Click) request;
			return (click.hasAltMask());
		} 
		return false;
	} 
    
	public void Frame.handleRequest(Request event) {
		System.out.println("Frame is handling the event.\n");
	}

}
