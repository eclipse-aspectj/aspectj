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
 * Sorts an int array with a provided sorting strategy.
 *
 * @author  Jan Hannemann
 * @author  Gregor Kiczales
 * @version 1.1, 02/17/04
 * 
 * @see LinearSort
 * @see BubbleSort
 */

public class Sorter { 
    
    /**
     * Shows the original (unsorted) array, sorts it and shows the new
     * (sorted) array.
     * 
     * @param sort the sorting strategy
     * @param numbers the array of int to sort
     */

	public Sorter(SortingStrategy sort, int[] numbers)	{
		System.out.println("\nPreparing sort...");
		System.out.println("original: "+show(numbers)); 
		sort.sort(numbers);
		System.out.println("sorted:   "+show(numbers));  
		System.out.println("Done sorting.");
	}

	/**
	 * Returns the content of the int array in a string
	 * 
	 * @param numbers the int array to display
	 * @returns a string with all the ints from the array
	 */

	private static String show(int[] numbers) {
		String out = "";
		for (int i=0; i<numbers.length; i++)
		{
			out += (numbers[i] + " ");
		}  
		return out;
	}
}