package ca.ubc.cs.spl.aspectPatterns.patternLibrary;

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

import java.util.WeakHashMap;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Iterator;

/**
 * Defines the general behavior of the Mediator design pattern.
 *
 * Each concrete sub-aspect of MediatorProtocol defines one kind of mediation
 * relationship.  Within that kind of relationship, there can be any number
 * of <i>Colleague</i>s.
 *
 * The sub-aspect defines three things: <ol>
 *
 *   <li> what types can be <i>Colleague</i>s and <i>Mediator</i> <br>
 *        this is done using <code>implements</code>
 *
 *   <li> what operations on the <i>Colleague</i> trigger <i>Mediator</i>
 * 		  updates.<br>
 *        This is done by concretizing the <code>change(Colleague)</code> 
 *        pointcut
 *
 *   <li> how to mediate <br>
 *        this is done by concretizing
 *        <code>notifyMediator(Colleague, Mediator)</code> 
 * </ol>
 *
 * Note that in this implementation, the work of updating is a method
 * on the sub-aspect, not a method introduced on the <i>Mediator</i>.  This
 * allows one class of object to be the <i>Mediator</i> in different kinds of
 * mediation relationships, each of which has a different updating
 * behavior.  
 *
 * @author  Jan Hannemann
 * @author  Gregor Kiczales
 * @version 1.1, 02/12/04
 */

public abstract aspect MediatorProtocol {

	
    /**
     * Declares the Colleague role.
     * Roles are modeled as (empty) interfaces.
     */
 
	protected interface Colleague  { }	

    /**
     * Declares the <code>Mediator</code> role.
     * Roles are modeled as (empty) interfaces.
     */
 
	protected interface Mediator { }

    /**
     * Stores the mapping between <i>Colleagues</i>s and <i>
     * Mediator</i>s. For each <i>Colleague</i>, its <i>Mediator</i>
     * is stored.
     */
    
	private WeakHashMap mappingColleagueToMediator = new WeakHashMap();


    /**
     * Returns the <i>Mediator</i> of 
     * a particular <i>Colleague</i>. Used internally.
     *
     * @param colleague the <i>Colleague</i> for which to return the mediator
     * @return the <i>Mediator</i> of the <i>Colleague</i> in question
     */

	private Mediator getMediator(Colleague colleague) {
		Mediator mediator = 
		    (Mediator) mappingColleagueToMediator.get(colleague);
		return mediator;
	}
	
    /**
     * Sets the <i>Mediator</i> for a <i>Colleague</i>. This is a method 
     * on the pattern aspect, not on any of the participants. 
     *
     * @param colleague the <i>Colleague</i> to set a new <i>Mediator</i> for
     * @param mediator the new <i>Mediator</i> to set
     */ 
     
	public void	setMediator(Colleague colleague, Mediator mediator) { 
	    mappingColleagueToMediator.put(colleague, mediator); 
	}


    /**
     * Defines what changes on <i>Colleague</i>s cause their <i>Mediator</i>
     * to be notified
     *
     * @param colleague the <i>Colleague</i> on which the change occured
     */

	protected abstract pointcut change(Colleague colleague);


	/**
	 * Call updateObserver to update each observer.
	 */  
	 
	after(Colleague c): change(c) {
		notifyMediator(c, getMediator(c));
	}

   /**
     * Defines how the <i>Mediator</i> is to be updated when a change
     * to a <code>Colleague</code> occurs. To be concretized by sub-aspects.
     *
     * @param c the <i>Colleague</i> on which a change of interest occured
     * @param m the <i>Mediator</i> to be notifed of the change  
     */

	protected abstract void notifyMediator(Colleague c, Mediator m);
}
