package ca.ubc.cs.spl.aspectPatterns.examples.state.java;

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
 * Implements the <i>context</i> of the queue example. This is effectively
 * a queue with limited capacity. Requests are forwarded to the current state
 * object.  
 *
 * @author  Jan Hannemann
 * @author  Gregor Kiczales
 * @version 1.1, 02/17/04
 */

public class Queue implements QueueContext {   
    
    /**
     * the current <i>State</i> of this <i>Context</i>
     */          
		
	protected QueueState state = new QueueEmpty();  
	
    /**
     * Tries to insert an object into the queue. Returns true if successful, 
     * false otherwiese.
     *
     * @param arg the object to be inserted into the queue
     * @return true if insertion was successful, false otherwise.
     */ 
	
	public boolean insert(Object arg) {	
		return state.insert(this, arg);
	}
	
    /**
     * Returns the first item in the queue
     *
     * @return the first item in the queue
     */ 

	public Object  getFirst() {		
		return state.getFirst(this);
	}
	
    /**
     * Tries to remove an object from the queue. Returns true if successful,
     * false otherwise.
     *
     * @return true if deletion was successful, false otherwise.
     */ 

	public boolean removeFirst() {		// Removes the first element from the queue
		return state.removeFirst(this);
	} 
	
    /**
     * Sets the state of the context to the arguments state.
     *
     * @param state the new state for the context object.
     */
     
	public void setState(QueueState state) {
		this.state = state;
	}
}