package ca.ubc.cs.spl.aspectPatterns.examples.templateMethod.java;

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
 * Implements the driver for the Template Method design pattern example.<p>
 *
 * Intent: <i>Define the skeleton of an algorithm in an operation, deferring 
 * some steps to subclasses. Template Method lets subclasses redefine certain
 * steps of an algorithm without changing the algorithm's structure</i><p>
 *
 * Participating objects are <code>SimpleGenerator</code> and 
 * <code>FancyGenerator</code> as <i>ConcreteClass</i>es. The 
 * <i>AbstractClass</i> is <code>DecoratedStringGenerator</code>.
 *  <p>
 *
 * In this example, the template method <code>generate(String)</code>
 * modifies a string in three steps and returns the result. While the 
 * SimpleGenerator does not change the string much, the
 * FancyGenerator turns the string to lowercase, then capitalizes all 
 * consonants and adds an explanantion to it.
 *
 * <p><i>This is the Java version.</i><p>
 *
 * Note that <i>AbstractClass</i> does in fact have to be an abstract
 * class (as opposed to an interface), to make it possible to define 
 * a default implementation for the template method.
 *
 * @author  Jan Hannemann
 * @author  Gregor Kiczales
 * @version 1.1, 02/17/04
 */

public class Main {

    /**
     * Implements the driver for the Template Method design 
     * pattern example.<p>
     *
     * @param args the command line parameters, unused
     */
 
	public static void main(String[] args) {
		String original = "This Is The Original String To Be Processed";
		
		DecoratedStringGenerator c1 = new SimpleGenerator();
		DecoratedStringGenerator c2 = new FancyGenerator();
		
		System.out.println("<Original>");
		System.out.println(original);
		System.out.println("<SimpleGenerator>");
		System.out.println(c1.generate(original));
		System.out.println("<FancyGenerator>");
		System.out.println(c2.generate(original));
	}
}