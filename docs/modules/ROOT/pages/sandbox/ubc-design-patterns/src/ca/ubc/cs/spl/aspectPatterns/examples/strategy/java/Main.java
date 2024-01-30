package ca.ubc.cs.spl.aspectPatterns.examples.strategy.java;

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
 * Implements the driver for the Strategy design pattern example.<p> 
 *
 * Intent: <i>Define a family of algorithms, encapsulate each one, and make
 * them interchangeable. Strategy lets the algorithm vary independently from 
 * clients that use it.</i><p>
 *
 * Participating objects are <code>LinearSort</code> and <code>BubbleSort
 * </code> as <i>Strategies</i>, and <code>Sorter</code> as <i>Context</i>.
 *
 * In this example, an array of 10 numbers is to be sorted. Depending on the
 * number of arguments of the call to <code>Main.main(..)</code>, linear sort
 * or bubblesort are used as sorting algorithms. The interface for the 
 * <i>strategies</i> is defined in <code>SortingStrategy</code>.
 *
 * <p><i>This is the Java version.</i><p>
 *
 * @author  Jan Hannemann
 * @author  Gregor Kiczales
 * @version 1.1, 02/17/04
 * 
 * @see LinearSort
 * @see BubbleSort
 */
  
public class Main {	
    
    /**
     * Implements the driver for the strategy example. If called with more
     * than zero arguments, bubblesort is used to sort the array of ten
     * numbers; otherwise linear sort is used. 
     */     
     
  	public static void main(String[] args)	{
		int[] numbers = {3, 2, 9, 8, 1, 5, 6, 4, 7, 0};
				
		SortingStrategy sort1 = new LinearSort();
		SortingStrategy sort2 = new BubbleSort();
		
		Sorter sorter;
		
		if (args.length == 0) {
			sorter = new Sorter(sort1, numbers);
		} 
		else {
			sorter = new Sorter(sort2, numbers);
		}
	}
}										
	
	
