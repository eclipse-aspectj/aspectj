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
 * Implements the <i>ConcreteState</i> "normal" for the queue example. 
 * Inserting and deleting items is possible in this state.
 *
 * @author  Jan Hannemann
 * @author  Gregor Kiczales
 * @version 1.1, 02/17/04
 *
 * @see QueueEmpty
 * @see QueueFull 
 */

public class QueueNormal implements QueueState {
	
    /**
     * stores the items in the queue
     */
     
	protected Object[] items = new Object[3];

    /**
     * stores the index of the first item in the queue
     */
     
	protected int first = 0; 

    /**
     * stores the index of the last item in the queue
     */
     
	protected int last  = 0;

    /**
     * Creates a new QueueNormal state object with the given set of elements
     * and the given indices for first and last object.  
     *
     * @param items the content of the full queue
     * @param first the index of the first item in the queue
     * @param last the index of the last item in the queue
     */

	public QueueNormal(Object[] items, int first, int last) {
		this.items = items;
		this.first = first;
		this.last  = last;
	}
	
	/** 
	 * Alternate constructor that uses preset values for object variables.
	 */
	
	public QueueNormal() {
	}

    /**
     * Tries to insert an object into the queue. Returns true if successful, 
     * false otherwiese. If the queue is full
     * after the insertion, the state of the context is changed to "full".
     *
     * @param context the <i>Context</i> for this design pattern (for update 
     * on demand)
     * @param arg the object to be inserted into the queue
     * @return true if insertion was successful, false otherwise.
     */ 
	
	public boolean insert(QueueContext context, Object arg) {
		items[(last)%items.length] = arg; 
		last = (last+1) % items.length;  
		if (first == last) {
			context.setState(new QueueFull(items, first));
		}
		return true;
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
     * false otherwiese. If the removed item was
     * the last one in the queue, the state of the context is changed to
     * "empty".
     *
     * @param context the <i>Context</i> for this design pattern (for update 
     * on demand)
     * @return true if deletion was successful, false otherwise.
     */ 

	public boolean removeFirst(QueueContext context){	  
		first = (first + 1) % items.length; 
		if (first == last) {
			context.setState(new QueueEmpty());
		}
		return true;
	}
}