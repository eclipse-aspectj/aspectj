package ca.ubc.cs.spl.aspectPatterns.examples.singleton.java;

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
 * Implements a sample <i>Singleton</i> class. The class's functionality
 * is to store an instance-specific ID and provide a <code>print()</code>
 * method that shows an object's ID.  
 *
 * Note that in this implementation the <i>Singleton</i> class has to know
 * that it implements the pattern (i.e. has to have appropriate code in it).
 *
 * @author  Jan Hannemann
 * @author  Gregor Kiczales
 * @version 1.1, 02/18/04
 */

 
public class PrinterSingleton { 

    /**
     * counts the instances of this class
     */
      
	protected static int objectsSoFar = 0;  

    /**
     * stores this <i>Singleton</i>'s only instance
     */
  
	protected static PrinterSingleton onlyInstance = null;

    /**
     * each instance has an ID to distinguish them.
     */
  
	protected int id;

    /**
     * Creates a new <code>PrinterSingleton</code>. The new instance gets
     * an ID equal to the total number of instances created of that type.
     * This constructor is protected to disallow it being called from other
     * places but the factory method and this type's subtypes. 
     * 
     * Unfortunately, this still allows other types in the same package 
     * to access the constructor.
     * 
     * Choosing to make it <code>private</code> would prevent that problem, 
     * but would make it impossible to subclass the type properly 
     * (as subtypes then could not use <code>super(..)</code> in their 
     * constructor.
     */
    
	protected PrinterSingleton() {
		id = ++ objectsSoFar;
	}
	
    /**
     * Factory method that provides access to the <i>Singleton</i> instance.
     * Uses creation-on-demand.
     *
     * @return the unique <i>Singleton</i> instance
     */
    
	public static PrinterSingleton instance() {
		if(onlyInstance == null) {
			onlyInstance = new PrinterSingleton();
		}
		return onlyInstance;
	}
	
    /**
     * Prints the instance's ID to <code>System.out</code>.
     */
    
	public void print() {
		System.out.println("\tMy ID is "+id);
	}
}
	