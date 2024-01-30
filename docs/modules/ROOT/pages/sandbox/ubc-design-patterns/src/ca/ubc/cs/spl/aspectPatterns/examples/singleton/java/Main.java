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
 * Implements the driver for the Singleton design pattern example.<p> 
 *
 * Intent: <i>Ensure that a class has only one instance and provide a global
 * point of access to it.</i><p>
 *
 * Participating objects are <code>PrinterSingleton</code> printer1, printer2, printer3 and 
 * <code>PrinterSubclass</code> ps1, ps2, ps3.<p>
 *
 * Three different objects of both PrinterSingleton and PrinterSubclass are
 * instantiated and compared. 
 *
 * This Implementation treats the singleton property as a non-inherited 
 * property. This meant that <i>Singleton</i> classes can still be subclassed
 * and these subclasses can access the <i>Singleton</i> constructor normally.
 *
 * <p><i>This is the Java version.</i><p>
 * 
 * Making the singleton property non-inherited requires to make the 
 * constructor of the <i>Singleton</i> protected (so subtypes can still
 * invoke <code>super(..)</code> in their constructor).
 *
 * @author  Jan Hannemann
 * @author  Gregor Kiczales
 * @version 1.1, 02/18/04
 * 
 * @see PrinterSingleton
 * @see PrinterSubclass
 */

class Main { 
    
    /**
     * the three object references to instances of the <i>Singleton</i> class.
     */
    
	private static PrinterSingleton printer1, printer2, printer3;

    /**
     * Implements the first test case. Creates 3 references to the 
     * <i>Singleton</i> by (a) using the regular constructor and then by (b) 
     * using a factory method. (a) should fail (but does not, since this
     * <code>Main</code> class is in the same package and can access the
     * protected constructor), (b) should create three identical objects.
     */	
	
	private static void test1() {
		System.out.println("\nTest 1a: Try to call regular constructor. "
		 + "This should fail.");
		 
		printer1 = new PrinterSingleton();
		printer2 = new PrinterSingleton();
		printer3 = new PrinterSingleton();
		 
		printer1.print();
		printer2.print();
		printer3.print(); 
		
		System.out.println("\t=> OO Problem: Classes in the same package can "
		 + "access the protected constructor.");
		
		System.out.println("\nTest 1b: Using instance() instead. This hould "
		 + "create three identical objects.");
		 
		printer1 = PrinterSingleton.instance();
		printer2 = PrinterSingleton.instance();
		printer3 = PrinterSingleton.instance(); 
		
		printer1.print();
		printer2.print();
		printer3.print(); 
	}
	

    /**
     * Implements the second test case. Tests if the 3 objects from test 1 are
     * in fact identical
     */	

	private static void test2() {
		System.out.println("\nTest 2: All three objects should be identical");

		System.out.print("\tThey are ");
		if ((printer1 == printer2) && (printer1 == printer3)) { 
			System.out.println("identical"); 
		}
		else {
			System.out.println("not identical"); 
		}
	}
	

    /**
     * Implements the third test case. Creates 3 instances of the <i>Singleton
     * </i>'s subclass. These objects should be different.
     */	

	private static void test3() {
		System.out.println("\nTest 3: Ensuring that subclasses can access the" 
		 + " constructor");
		System.out.println("        (All three outputs should be different)");
		
		printer1 = new PrinterSubclass();
		printer2 = new PrinterSubclass();
		printer3 = new PrinterSubclass(); 
		
		printer1.print();
		printer2.print();
		printer3.print(); 
	}
	
    /**
     * This is the driver for the <code>Singleton</code> case. It performes
     * three tests:
     *
     * <OL>
     *  <LI> Creates 3 references to the <i>Singleton</i> by (a) using the
     *       regular constructor and then by (b) using a factory method.
     *       (a) should fail, (b) should create three identical objects.
     *  <LI> Tests if the above 3 objects are in fact identical
     *  <LI> Creates 3 instances of a <i>Singleton</i>'s subclass. These
     *       objects should be different.
     * </OL>
     */
			
	public static void main (String[] args) { 
		System.out.println("Testing SINGLETON pattern (java) ...");
		test1();
		test2();
		test3();
		System.out.println("\n... done.");
	}
}