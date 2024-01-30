package ca.ubc.cs.spl.aspectPatterns.examples.memento.aspectj;

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

import ca.ubc.cs.spl.aspectPatterns.patternLibrary.Memento;	

/**
 * Implements the driver for the Memento design pattern example.<p> 
 *
 * Intent: <i>Without violating encapsulation, capture and externalize an 
 * object's internal state so that the object can be restored to this state
 * later</i><p>
 *
 * Participating objects are <code>Memento</code> and <code>Originator</code>
 * <p>
 *
 * This example changes the state of the <i>Originator</i> five times, but
 * creates a <i>Memento</i> of it after the third change. After the 5 changes
 * are done, the <i>Memento</i> is used to restore the <i>Originator</i>'s
 * state.
 *
 * <p><i>This is the AspectJ version.</i><p>   
 *
 * Memento and Originator are decoupled.
 *
 * @author  Jan Hannemann
 * @author  Gregor Kiczales
 * @version 1.1, 02/12/04
 * 
 * @see StateMemento
 * @see MyOriginator
 */



public class Main {
	
    /**
     * This example changes the state of the <i>Originator</i> five times, but
     * creates a <i>Memento</i> of it after the third change. After the 5 
     * changes are done, the <i>Memento</i> is used to restore the 
     * <i>Originator</i>'s state.  
     * 
     * @param args command line parameters, unused.
     */
	
	public static void main(String[] args) { 
		
		Memento storedState = null;  
		Counter counter = new Counter(); 
		
		for (int i=1; i<=5; i++) {
			counter.increment();
			counter.show(); 
			if (i==3) { 
				storedState = 
					CounterMemento.aspectOf().createMementoFor(counter); 
			}
		}
		
		System.out.println("\nTrying to reinstate state (3)...");
		CounterMemento.aspectOf().setMemento(counter, storedState);
		counter.show(); 
	}
}