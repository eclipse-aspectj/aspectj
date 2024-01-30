package ca.ubc.cs.spl.aspectPatterns.examples.state.aspectj;

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
 * Inserting items is impossible if the queue is full. 
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
     * Tries to insert an object into the queue. Returns false since the
     * queue is full.
     *
     * @param arg the object to be inserted into the queue
     * @return false.
     */ 
	
	public boolean insert(Object arg) {	
		return false;
	}

    /**
     * Returns the first item in the queue. 
     *
     * @return the first item in the queue.
     */ 

	public Object  getFirst() {	
		return items[first];
	}

    /**
     * Tries to remove an object from the queue. Returns true if successful,
     * false otherwiese. The state transition to "normal" is implemented by 
     * the aspect.
     *
     * @return true since it is always possible to delete an item from a 
     * full queue
     */ 

	public boolean removeFirst(){	    
		return true;
	}
}