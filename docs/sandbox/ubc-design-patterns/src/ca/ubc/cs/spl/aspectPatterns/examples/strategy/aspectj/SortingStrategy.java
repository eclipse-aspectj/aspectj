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

import ca.ubc.cs.spl.aspectPatterns.patternLibrary.StrategyProtocol;
 
/**
 * This aspect defines the mechanics for executing the <i>Strategy</i>'s
 * algorithm. In this case, an around advice is utilized to execute the
 * proper code.
 *
 * @author  Jan Hannemann
 * @author  Gregor Kiczales
 * @version 1.1, 02/17/04
 * 
 * @see LinearSort
 * @see BubbleSort
 */


public aspect SortingStrategy extends StrategyProtocol {
    
	
    /**
     * Assignes the <i>Context</i> role to <code>Sorter</code>
     */

	declare parents: Sorter     implements Context;

    /**
     * Assignes the <i>Strategy</i> role to <code>LinearSort</code>
     */

	declare parents: LinearSort implements Strategy;

    /**
     * Assignes the <i>Strategy</i> role to <code>BubbleSort</code>
     */

	declare parents: BubbleSort implements Strategy;   
	
	
	/**
	 * Sorts the array. This is merely a method stub. The actual 
	 * implementation can either be realized here or in an 
	 * <code>around</code> advice as illustrated below.
	 * 
	 * This method acts as <i>ContextInterface()</code>.
	 * 
	 * @param numbers
	 * @return
	 */
	
	public int[] Sorter.sort(int[] numbers) { 
		return numbers;
	}

	
	
    /**
     * Invokes the appropriate strategy's sort() method when the
     * Sorter needs to sort. Using around advice is only one way
     * of realizing this (see above). 
     * 
     * The around advice solution is more useful if the <i>Context</i>
     * itself defines the <i>ContextInterface()</i> (not the case here).
     * 
     * This particular implementation was chosen to illustrate the
     * alternatives.
     */

	int[] around(Sorter s, int[] numbers): 
		call(int[] Sorter.sort(int[])) && target(s) && args(numbers) {
		
		Strategy strategy = getConcreteStrategy(s);
		
		if (strategy instanceof BubbleSort) {
			((BubbleSort)strategy).sort(numbers);
		} else if (strategy instanceof LinearSort) {
			((LinearSort) strategy).sort(numbers);
		} else {
			// Invalid strategy: could throw an exception here
		}
		return numbers;
	}
}
