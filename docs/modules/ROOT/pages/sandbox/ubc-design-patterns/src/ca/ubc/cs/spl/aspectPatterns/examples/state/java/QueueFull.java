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
 * Implements the <i>ConcreteState</i> "full" for the queue example. 
 * Inserting items is impossible if the queue is full. Removing items will
 * trigger a state change to "normal".
 *
 * @author  Jan Hannemann
 * @author  Gregor Kiczales
 * @version 1.1, 02/17/04
 *
 * @see QueueEmpty
 * @see QueueNormal
 */

public class QueueFull implements QueueState {

    /**
     * stores the items in the queue
     */
     
	protected Object[] items;
	
	/**
	 * stores the index of the first item in the queue. 
	 */  
	 
	protected int first;

    /**
     * Creates a new QueueFull state object with the given set of elements
     * and the given index.  
     *
     * @param items the content of the full queue
     * @param first the index of the first item in the queue
     */

	public QueueFull(Object[] items, int first) {
		this.items = items;
		this.first = first;
	}

    /**
     * Tries to insert an object into the queue. Returns false since the
     * queue is full.
     *
     * @param context the <i>Context</i> for this design pattern (for update 
     * on demand)
     * @param arg the object to be inserted into the queue
     * @return false.
     */ 
	
	public boolean insert(QueueContext context, Object arg) {	
		return false;
	}

    /**
     * Returns the first item in the queue. 
     *
     * @param context the <i>Context</i> for this design pattern (for update 
     * on demand)
     * @return the first item in the queue.
     */ 

	public Object  getFirst(QueueContext context) {	 
		return items[first];
	}

    /**
     * Tries to remove an object from the queue. Returns true if successful,
     * false otherwiese. The state of the context is changed to "normal".
     *
     * @param context the <i>Context</i> for this design pattern (for update 
     * on demand)
     * @return true if deletion was successful, false otherwise.
     */ 

	public boolean removeFirst(QueueContext context){		     
		QueueState nextState = new QueueNormal(items, first, first); 
		context.setState(nextState);
		return nextState.removeFirst(context);
	}
}