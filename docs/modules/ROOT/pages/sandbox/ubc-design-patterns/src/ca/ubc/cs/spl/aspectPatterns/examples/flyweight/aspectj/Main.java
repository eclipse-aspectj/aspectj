package ca.ubc.cs.spl.aspectPatterns.examples.flyweight.aspectj;

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
 * Implements the driver for the Flyweight design pattern example.<p> 
 *
 * Intent: <i>Use sharing to support large numbers of fine-grained objects 
 * efficiently.</i><p>
 *
 * Participating <i>Flyweight</i> classes are <code>CharacterFlyweight</code>
 * and <code>WhitespaceFlyweight</code>. Both implement the <code>
 * PrintableFlyweight</code> interface. Flyweights are generated via the 
 * <code>PrintableFlyweightFactory</code>. <P>
 *     
 * Intrinsic state: The character to print, 
 * Extrinsic state: Whether the char is upper case or lower case
 *
 * This example creates a sentence out of <code>PrintableFlyweight</i>s
 * (characters and whitespaces).
 *
 * <p><i>This is the AspectJ version.</i><p>   
 *
 * The creation-on-demand functionality is performed by the abstract
 * pattern aspect.
 *
 * @author  Jan Hannemann
 * @author  Gregor Kiczales
 * @version 1.1, 02/11/04
 * 
 * @see PrintableFlyweight
 */

public class Main {

	/**
	 * Implements the driver for the Flyweight design pattern example.<p> 
	 *
	 * This example creates a sentence out of <code>PrintableFlyweight</i>s
	 * (characters and whitespaces). 
	 */

	public static void main(String[] args) {
	
		System.out.println("This is a test for the AspectJ version of the "
		    + "Flyweight pattern implementation.");
		System.out.println("The client will use char flyweights to print "
		    + "the phrase");
		System.out.println("\"This Is A Test\".\n");
		System.out.println("Testing Pattern: Flyweight - STARTING\n");

		PrintableFlyweight T = 
			FlyweightImplementation.aspectOf().getPrintableFlyweight('t');
		PrintableFlyweight H = 
			FlyweightImplementation.aspectOf().getPrintableFlyweight('h');
		PrintableFlyweight I = 
			FlyweightImplementation.aspectOf().getPrintableFlyweight('i');
		PrintableFlyweight S = 
			FlyweightImplementation.aspectOf().getPrintableFlyweight('s');
		PrintableFlyweight A = 
			FlyweightImplementation.aspectOf().getPrintableFlyweight('a');
		PrintableFlyweight E = 
			FlyweightImplementation.aspectOf().getPrintableFlyweight('e');
		PrintableFlyweight Empty = 
			FlyweightImplementation.aspectOf().getPrintableFlyweight(' '); 
		
		// Printing: "This Is A Test"
	
		T.print(true);
		H.print(false);
		I.print(false);
		S.print(false);
		
		Empty.print(true);
		
		I.print(true);
		S.print(false);
		
		Empty.print(true);
		
		A.print(true);
		
		Empty.print(true);
		
		T.print(true);
		E.print(false);
		S.print(false);
		T.print(false);	
		
		System.out.println();
		
		System.out.println("\nTesting Pattern: State - FINISHED");
	}
}	