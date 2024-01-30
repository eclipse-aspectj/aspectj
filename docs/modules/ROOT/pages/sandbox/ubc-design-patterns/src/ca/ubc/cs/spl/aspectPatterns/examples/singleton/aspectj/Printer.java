package ca.ubc.cs.spl.aspectPatterns.examples.singleton.aspectj;

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
 * Implements a sample class that will be assigned the <i>Singleton</i> role
 * in this example. The class's functionality
 * is to store an instance-specific ID and provide a <code>print()</code>
 * method that shows an object's ID.  
 *
 * Note that in this implementation the class does not have to know
 * that it is a <i>Singleton</i> (i.e. has no pattern code in it).
 * 
 * Note further that instead of assigning the <i>Singleton</i> property
 * via the <code>declare parents</code> construct in the aspect, it is
 * possible to just add a <code>implements Singleton</doce> here. However,
 * that would introduce pattern-related code into this type.
 *
 * @author  Jan Hannemann
 * @author  Gregor Kiczales
 * @version 1.1, 02/18/04
 */

public class Printer { 	 

    /**
     * counts the instances of this class
     */
      
	protected static int objectsSoFar = 0;

    /**
     * each instance has an ID to distinguish them.
     */
  
	protected int id;

	/**
	 * Creates a <code>Printer</code> object. Note that the constructor
	 * is not protected; the protection is realized by the aspect.
	 */
	
	public Printer() {
		id = ++ objectsSoFar;
	}
	
    /**
     * Prints the instance's ID to <code>System.out</code>.
     */
    
	public void print() {
		System.out.println("\tMy ID is "+id);
	}
}
	