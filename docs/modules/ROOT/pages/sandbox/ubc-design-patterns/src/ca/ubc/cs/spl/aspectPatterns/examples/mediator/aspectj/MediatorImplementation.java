package ca.ubc.cs.spl.aspectPatterns.examples.mediator.aspectj;

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
 
import ca.ubc.cs.spl.aspectPatterns.patternLibrary.MediatorProtocol;

/**
 * Concretizes the mediation relationship for <code>Button</code> 
 * (as <i>Colleague</i>) and <code>Label</code> (as <i>Mediator</i>). 
 * <code>Button</code> clicks trigger <code>Label</code> updates. 
 *
 * @author  Jan Hannemann
 * @author  Gregor Kiczales
 * @version 1.1, 02/12/04
 */

public aspect MediatorImplementation extends MediatorProtocol {

    /**
     * Assings the <i>Colleague</i> role to the <code>Button</code> 
     * class. Roles are modeled as (empty) interfaces.
     */
   
	declare parents: Button implements Colleague;

    /**
     * Assings the <i>Mediator</i> role to the <code>Label</code> 
     * class. Roles are modeled as (empty) interfaces.
     */

	declare parents: Label	implements Mediator;

    /**
     * Defines what changes on Colleagues cause their <i>Mediator</i> to be 
     * notified (here: Button clicks)
     *
     * @param cs the colleague on which the change occured
     */

	protected pointcut change(Colleague c): 
	    (call(void Button.clicked()) && target(c));

    /**
      * Defines how the <i>Mediator</i> is to be updated when a change
      * to a <i>Colleague</i> occurs. Here, the label's text is set 
      * depending on which button was clicked. The appropriate button's label 
      * is also updated.
      *
      * @param c the colleague on which a change of interest occured
      * @param m the mediator to be notifed of the change  
      */

	protected void notifyMediator(Colleague c, Mediator m) {
		Button button = (Button) c;
		Label  label  = (Label)  m; 
		if (button == Main.button1) {
			label.setText("Button1 clicked"); 
		} else if (button == Main.button2) {
			label.setText("Button2 clicked");
		}
		button.setText("(Done)");
	}
}
