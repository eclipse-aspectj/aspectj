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
 * Implements the <i>ConcreteState</i> "empty" for the queue example. 
 * Removing items is impossible if the queue is empty. Inserting items will
 * trigger a state change to "normal".
 *
 * @author  Jan Hannemann
 * @author  Gregor Kiczales
 * @version 1.1, 02/17/04
 *        
 * @see QueueNormal
 * @see QueueFull
 */

public class QueueEmpty implements QueueState {
	
    /**
     * Tries to insert an object into the queue. Returns true if successful, 
     * false otherwiese. The state of the context
     * is changed to "normal" (queue is no longer empty).
     *
     * @param context the <i>Context</i> for this design pattern (for update 
     * on demand)
     * @param arg the object to be inserted into the queue
     * @return true if insertion was successful, false otherwise.
     */ 
	
	public boolean insert(QueueContext context, Object arg) {
		QueueNormal nextState = new QueueNormal(); 
		context.setState(nextState);
		return nextState.insert(context, arg); 
	}

    /**
     * Returns the first item in the queue. Returns null since the queue is 
     * empty.
     *
     * @param context the <i>Context</i> for this design pattern (for update 
     * on demand)
     * @return null.
     */ 

	public Object  getFirst(QueueContext context) {
		return null;
	}

    /**
     * Tries to remove an object from the queue. Returns false (queue is 
     * empty).
     *
     * @param context the <i>Context</i> for this design pattern (for update 
     * on demand)
     * @return false.
     */ 

	public boolean removeFirst(QueueContext context){
		return false;
	} 

}