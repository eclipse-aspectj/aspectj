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
 * Implements the driver for the State design pattern example.<p> 
 *
 * Intent: <i>Allow an object to alter its behavior when its internal state
 * changes. The object will appear to change its class</i><p>
 *
 * Participating objects are <code>Queue</code> as <i>Context</i>, and 
 * <code>QueueNormal</code>, <code>QueueEmpty</code>, and <code>QueueFull
 * </code> as <i>ConcreteState</i>s. The <i>State</i> interface is defined in
 * <code>QueueState</code>. 
 * <p> 
 *
 * This example of the State design pattern models a Queue ADT with
 * a limited capacity that has three different states: 
 * <UL>
 * <LI>Empty:	The queue is empty 
 * <LI>Normal:  The queue is neither empty nor full
 * <LI>Full:	The queue is full (# of elements = capacity)
 * </UL>
 * The queue stores Objects. The following operations are defined on Queue:
 * <UL>
 * <LI><code>insert(Object)</code> Inserts a new Object into the queue
 * <LI><code>getFirst():Object</code> Returns the first element in the queue
 * <LI><code>removeFirst()</code> Removes the first elelemts from the queue
 * </UL>
 * These are the pattern roles:
 * <UL>
 * <LI>Queue:	Context
 * <LI>QueueState: 	State interface
 * <LI>QueueEmpty:  ConcreteState1
 * <LI>QueueNormal: ConcreteState2
 * <LI>QueueFull:	ConcreteState3 
 * </UL>
 * <p><i>This is the AspectJ version.</i><p>
 *
 * This implementation uses a concrete aspect to take care of all the state 
 * transitions in the system. States are no longer tangled with each other
 * as they do not have to know about their successor states. All state 
 * transitions are localized in the aspect.
 *
 * @author  Jan Hannemann
 * @author  Gregor Kiczales
 * @version 1.1, 02/17/04
 * 
 * @see Queue
 * @see QueueState
 * @see QueueEmpty
 * @see QueueNormal
 * @see QueueFull
 * @see QueueStateAspect
 */

public class Main { 

    /**
     * Implements insertion into a queue. Prints out status messages.
     *
     * @param queue the queue to insert into
     * @param s the string to insert into the queue
     */     	

    public static void testInsert(Queue queue, String s) {
		System.out.print("   Trying to insert ["+s+"] into the queue ... ");
		boolean status = queue.insert(s);
		if (status == true) {
			System.out.println("successful"); 
		} else {
			System.out.println("NOT successful, queue probably full");
		}
	}

    /**
     * Implements deletion from a queue. Prints out status messages.
     *
     * @param queue the queue to delete items from
     */     	

	public static void testRemove(Queue queue) {
		System.out.print("   Trying to remove 1st element of the queue ... ");
		String item = (String) queue.getFirst(); 
		boolean status = queue.removeFirst();
		if (status == true) {
			System.out.println("successful: "+item); 
		} else {
			System.out.println("NOT successful: "+item);
		}
	}

    /**
     * Implements the driver for the State design pattern example.<p> 
     *
     * @param args the command line paramters, unused
     */

	public static void main(String[] args) {

		System.out.println("Testing Pattern: State - STARTING\n");
		
		Queue queue = new Queue();
		testInsert(queue, "This ");
		testInsert(queue, "is ");  
		testInsert(queue, "a ");
		testInsert(queue, "test");   
		System.out.println();
		testRemove(queue);
		testRemove(queue);
		testRemove(queue);
		testRemove(queue);

		System.out.println("\nTesting Pattern: State - FINISHED");

	}
}
	