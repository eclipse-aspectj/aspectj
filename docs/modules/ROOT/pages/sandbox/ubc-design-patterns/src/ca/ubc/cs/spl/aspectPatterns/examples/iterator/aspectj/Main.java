package ca.ubc.cs.spl.aspectPatterns.examples.iterator.aspectj;

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

import java.util.Iterator;
 
/**
 * Implements the driver for the Iterator design pattern example.<p> 
 *
 * Intent: <i>Provide a way to access the elements of an aggregate object
 * sequentially without exposing its underlying representation.</i><p>
 *
 * Participating objects are <code>SimpleList</code> as <i>Aggregate</i>,
 * <code>OpenList</code> as <i>ConcreteAggregate</i>, <code>
 * java.util.Iterator</code> as <i>Iterator</i>, and <code>ReverseIterator
 * </code> as <i>ConcreteIterator</i>.
 *
 * In this example, the concrete aggregate is a list that gets filled with 
 * five integer objects (1 to 5). The, the <code>ReverseIterator</code> is 
 * created and used to print all elements in reverse order.
 *
 * <p><i>This is the AspectJ version.</i><p>
 *
 * @author  Jan Hannemann
 * @author  Gregor Kiczales
 * @version 1.1, 02/12/04
 *
 * @see SimpleList 
 * @see OpenList
 * @see java.util.Iterator
 * @see OpenListIteration
 */


public class Main {
    
    /**
     * Prints all elements in the iterator to <code>System.out</code>.
     *
     * @param iter the iterator which elements are to be printed
     */    
    
	private static void print(Iterator iter) { 
		while(iter.hasNext()) {
			System.out.println(iter.next());
		}
	}
	
    /**
     * Implements the driver for the Iterator design pattern example.<p> 
     *
     * In this example, the concrete aggregate is a list that gets filled with 
     * five integer objects (1 to 5). The, the <code>ReverseIterator</code> is 
     * created and used to print all elements in reverse order.  
     *
     * @param args command line paramters, unused
     */
	
	public static void main(String[] args) {
	    
		OpenList openList = new OpenList();
		openList.append(new Integer(1));
		openList.append(new Integer(2));		
		openList.append(new Integer(3));
		openList.append(new Integer(4));
		openList.append(new Integer(5)); 
		
		System.out.println("List created, containing int objects 1, 2, 3, 4, 5.");
		
		Iterator iter = OpenListIteration.aspectOf().createIteratorFor(openList);
		
		System.out.println("Using ReverseIterator to print list elements in reverse order...");

		print(iter);

		System.out.println("done.");
	}
}