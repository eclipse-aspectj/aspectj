package ca.ubc.cs.spl.aspectPatterns.examples.adapter.aspectj;

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
 * Implements the driver for the Adapter design pattern example. <p> 
 *
 * Intent: <i> Convert the interface of a class into another interface clients
 * expect. Adapter lets classes work together that couldn't otherwise because
 * incompatible interfaces.</i><p>
 *
 * Experimental setup: anloguous to the pattern structure as described on page
 * 141 of the "Design Patterns" book:
 * 
 * <code>Adaptee</code> can print strings to <code>System.out</code>. 
 * <code>Adapter</code> allows to access <code>Adaptee</code>'s functionality
 * via <code>Target</code>'s interface.<p>  
 * 
 * <i>This is the AspectJ implementation.</i><p>
 *
 * The implementation is that of an <i>object adapter</i> (NOT class adapter),
 * as the latter requires multiple inheritance which Java does not provide. 
 * 
 * In this implementation, the <i>Adaptee</i> is effectively made to conform
 * with the <i>Target</i> interface directly. <code>Adapter</code> is an 
 * aspect that ensures that by using the <code>declare parents</code> 
 * construct (to ensure <i>Adaptee</i> is of type <i>Target</i>) and an
 * inter-type declaration of the missing interface method. 
 *
 * @author Jan Hannemann
 * @author Gregor Kiczales
 * @version 1.1, 01/26/04
 * 
 * @see Target
 * @see Adaptee
 * @see Adapter
 */

public class Main { 
	
    /**
     * the Adaptee in the scenario. Note that our adaptee can be used as a 
     * Writer because of the <code>declare parents</code> statement in the 
     * aspect.
     */
     	
	private static Writer adaptee; 

	/**
	 * Implements the driver. 
	 * 
	 * In this implementation, the <i>Adaptee</i> becomes its own
	 * <i>Adapter</i>, so only one variable is needed.
	 * 
	 * @param args required for a main method, but ignored
	 */
    
	public static void main(String[] args) {

		System.out.println("Creating Adaptee (which is its own Adapter)...");
		adaptee = new SystemOutPrinter();

		System.out.print  ("Adapter and Adaptee are the same object: ");
		System.out.println(adaptee == adaptee);

		System.out.println("Issuing the request() to the Adapter...");
		adaptee.write("Test successful.");
	}
}