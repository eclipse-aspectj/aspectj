package ca.ubc.cs.spl.aspectPatterns.examples.strategy.aspectj;

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
 * Implements the linear sort sorting strategy for int arrays.
 *
 * @author  Jan Hannemann
 * @author  Gregor Kiczales
 * @version 1.1, 02/17/04
 */

public class LinearSort {
    
    /**
     * Helper method that exchanges two values in an int array
     * 
     * @param numbers the int array
     * @param pos1 the position of the first element
     * @param pos2 the position of the second element
     */

	private void exchange(int[] numbers, int pos1, int pos2) {
		int tmp = numbers[pos1];
		numbers[pos1] = numbers[pos2];
		numbers[pos2] = tmp;
	}


    /**
     * Sorts an int array
     * 
     * @param numbers the int array to sort
     */

	public void sort(int[] numbers) {
		System.out.print("Sorting by LinearSort...");
		int lowest  = 0;  
		
		for (int start = 0; start < numbers.length; start ++) {
			lowest = start;
			
			for (int current = start; current < numbers.length; current ++) {
				if (numbers[current] < numbers[lowest]) {
					lowest = current;
				}
			}
    		exchange(numbers, start, lowest);
		}
		System.out.println("done.");
	}
}