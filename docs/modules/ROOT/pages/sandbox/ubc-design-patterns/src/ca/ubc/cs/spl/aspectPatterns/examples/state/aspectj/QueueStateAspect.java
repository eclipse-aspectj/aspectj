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
 * Implements the state transitions for this state design pattern example. 
 * State transitions are realizied as <code>after</code> advice. The 
 * joinpoints are the calls from the context to its state object.<p>
 *
 * Exisiting states are reused without a employing a flyweight mechanism or 
 * (inflexibly) modularizing the transitions in the context.
 *
 * @author  Jan Hannemann
 * @author  Gregor Kiczales
 * @version 1.1, 02/17/04
 * 
 */


public aspect QueueStateAspect {
    
    /** 
     * the queue's "empty" state
     */  
	
	protected QueueEmpty  empty  = new QueueEmpty();

    /** 
     * the queue's "normal" state
     */  

	protected QueueNormal normal = new QueueNormal();

    /** 
     * the queue's "full" state
     */  

	protected QueueFull   full   = new QueueFull(); 


    /** 
     * Sets the initial state of the queue to empty. 
     *
     * @param queue the queue context that is initialized.
     */

	
	after(Queue queue): initialization(new()) && target(queue) {
		queue.setState(empty);
	}

    /** 
     * Updates the queue context's state after each call from it to the 
     * <code>insert(Object)</code> method if its current state.
     *
     * @param queue the queue context that makes the call.
     * @param qs the current QueueState that receives the call
     * @param arg the object to be inserted.
     */

	after(Queue queue, QueueState qs, Object arg): call(boolean QueueState+.insert(Object)) && target(qs) && args(arg) && this(queue) {
		if (qs == empty) { 
			normal.insert(arg);
			queue.setState(normal);
		} else if (qs == normal) { 
			if (normal.first == normal.last) {
				full.items = normal.items;
				full.first = normal.first;
				queue.setState(full);
			}
		}
	} 
		
    /** 
     * Updates the queue context's state after each call from it to the 
     * <code>removeFirst()</code> method if its current state.
     *
     * @param queue the queue context that makes the call.
     * @param qs the current QueueState that receives the call
     */

	after(Queue queue, QueueState qs): call(boolean QueueState+.removeFirst()) && target(qs) && this(queue) {
		if (qs == full) {
			normal.items = full.items;
			normal.last  = full.first;
			normal.first = (full.first +1) % normal.items.length;
			queue.setState(normal);
		} else if (qs == normal) {
			if (normal.first == normal.last) {
				queue.setState(empty);
			}
		}
	}   
}