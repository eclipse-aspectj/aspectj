package ca.ubc.cs.spl.aspectPatterns.examples.adapter.java;

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
 * The Original Code is ca.ubc.cs.spl.patterns.
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
 * <i>This is the Java implementation.</i><p>
 *
 * Participants and their roles: <ul>
 *   <LI> Main (Client)
 *   <LI> Writer (Target)
 *   <LI> SystemOutPrinter (Adaptee)
 *   <LI> PrinterAdapter (Adapter)
 * </ul>
 * 
 * The implementation is that of an <i>object adapter</i> (NOT class adapter),
 * as the latter requires multiple inheritance which Java does not provide. 
 *
 * @author Jan Hannemann
 * @author Gregor Kiczales
 * @version 1.1, 01/26/04
 *
 * @see Adapter
 * @see Adaptee
 * @see Target
 */

public class Main { 
	
    /**
     * the Target in the scenario
     */
     	
	private static Writer myTarget; 

    /**
     * the Adaptee in the scenario
     */
     	
	private static SystemOutPrinter adaptee; 

    /**
     * Implements the driver.  
     * 
     * @param args required for a main method, but ignored
     */
     
	public static void main(String[] args) {

		System.out.println("Creating the Adaptee...");
		adaptee = new SystemOutPrinter();

	    System.out.println("Creating the Adapter...");
		myTarget = new PrinterAdapter(adaptee); 

		System.out.print  ("Adapter and Adaptee are the same object: ");
		System.out.println(myTarget.equals(adaptee));

		System.out.println("Issuing the request() to the Adapter...");
		myTarget.write("Test successful."); 
		
	}
}