package ca.ubc.cs.spl.aspectPatterns.examples.prototype.aspectj;

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
 * Implements the driver for the Prototype design pattern example.<p> 
 *
 * Intent: <i>Specify the kinds of objects to create using a prototypical 
 * instance, and create new objects by copying this prototype.</i><p>
 *
 * Participatng objects are <code>StringPrototypeA</code> and 
 * <code>StringPrototypeB</code> as <i>Prototype</i>s.<p>
 *
 * In this example, both StringPrototypeA and StringPrototypeB implement cloneable
 * classes emulating limited String behavior. This driver creates an 
 * object of each class and clones it. Both originals and clones are
 * manipulated to show that they are different objects. 
 *
 * <p><i>This is the AspectJ version.</i><p> 
 *
 * Java's <code>Cloneable</code> interface is used internally. In this
 * version, a standard implementation for <code>createClone()</code> is
 * provided by the abstract pattern aspect. Each concrete pattern instance
 * aspect can define special behavior by overwriting the appropriate 
 * methods. Consequently, the participants are freed of the pattern.
 *
 * @author  Jan Hannemann
 * @author  Gregor Kiczales
 * @version 1.1, 02/13/04
 * 
 * @see StringPrototypeA
 * @see StringPrototypeB
 */ 

public class Main {

    /**
     * Implements the driver for the Prototype design pattern example.<p> 
     *
     * In this example, both StringPrototypeA and StringPrototypeB implement cloneable
     * classes emulating limited String behavior. This driver creates an 
     * object of each class and clones it. Both originals and clones are
     * manipulated to show that they are different objects. 
     *
     * @param args the command line parameters, unused.
     */

    public static void main(String[] args) {
        
		System.out.println("Testing the Prototype design pattern implementation...");
	            
		StringPrototypeA originalA;
		StringPrototypeB originalB;
		StringPrototypeA copyA1, copyA2;
		StringPrototypeB copyB1;
	            
		originalA = new StringPrototypeA("  This is Prototype 1");
		originalB = new StringPrototypeB("  This is Prototype 2"); 
	            
		System.out.println("These are the two prototypes:");
		System.out.println(originalA);
		System.out.println(originalB);
	            
		copyA1 = (StringPrototypeA) StringPrototypes.aspectOf().cloneObject(originalA);
		copyB1 = (StringPrototypeB) StringPrototypes.aspectOf().cloneObject(originalB);
	    
		System.out.println("These are copies of the prototypes:");
		System.out.println(copyA1);
		System.out.println(copyB1);
	    
		System.out.println("Now prototype 1 is changed. Here are prototype 1 and its former copy:");
		originalA.setText("  This is Prototype 1 (changed)");
		System.out.println(originalA);
		System.out.println(copyA1);
	            
		System.out.println("This is a clone of the changed prototype 1 and a changed copy of prototype 2:");
		copyA2 = (StringPrototypeA) StringPrototypes.aspectOf().cloneObject(originalA);
		copyB1.setText("  This is a changed copy of prototype 2");
		System.out.println(copyA2);
		System.out.println(copyB1);
	            
	            
		System.out.println("... done.");  
    }
}