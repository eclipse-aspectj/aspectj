package ca.ubc.cs.spl.aspectPatterns.examples.composite.aspectj;

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
 
import java.io.PrintStream;  
import java.util.Enumeration; 
import ca.ubc.cs.spl.aspectPatterns.patternLibrary.CompositeProtocol;

/**
 * Implements a concrete instance of the Composite design pattern.<p> 
 * 
 * It maintains the mapping between <i>Composite</i>s and their children, 
 * defines the <i>Component</i>, <i>Composite</i>, and <i>Leaf</i> roles, 
 * and provides facilities to implement methods that work on the whole 
 * aggregate structure.
 *
 * <p><i>This is the AspectJ version.</i><p> 
 *
 * This concrete subaspect does the following things: <UL>
 * <LI> Defines which classes are Components and Leafs
 * <LI> Defines methods that operate on the whole aggregate
 *      structure (using visitors)
 * </UL>
 * 
 * Note that implementing the two visitors is just done for fun. Similar
 * implementations are possible in the OO case of course, although that would
 * require changing the <i>Components</i>. 
 *
 * @author  Jan Hannemann
 * @author  Gregor Kiczales
 * @version 1.1, 02/06/04
 */

public aspect FileSystemComposition extends CompositeProtocol {

    /** 
     * Assigns the Composite role to <code>Directory</code>
     */
     
    declare parents: Directory implements Composite;

    /** 
     * Assigns the Leaf role to <code>File</code>
     */

    declare parents: File      implements Leaf;
 
 
    

	// Test 1: Printing the stucture using a visitor

	/**
	 * helper variable to store recursion depth for pretty printing
	 */

    private static int indent = 0;

    /**
     * Print a number of spaces according to the current recursion depth
     */

    private static void indent() {
		for (int i=0; i<indent; i++)
			System.out.print(" ");
	}

    /**
     * Provides a client-accessible method that pretty-prints the 
     * structure of the aggregate structure using a Visitor
     *
     * @param s the PrintStream to print to
     */

    public void Component.printStructure(PrintStream s) {
        indent();
        s.println("<Component>"+this);
    } 
    
    /**
     * Implements <code>printStructure</code> for Composites: The indent 
     * is appropriately updated and the method call is forwarded to all 
     * children.
     *
     * @param s the PrintStream to print to
     */

    public void Composite.printStructure(final PrintStream s) {
        indent();
        s.println("<Composite>"+this);
        indent +=4;
        FileSystemComposition.aspectOf().recurseOperation(this, new Visitor() { 
            public void doOperation(Component c) { c.printStructure(s); } 
        } );
        indent -=4;
    }

    /**
     * Implements <code>printStructure</code> for <i>Leaf</i>s.
     *
     * @param s the PrintStream to print to
     */

    public void Leaf.printStructure(PrintStream s) {
        indent();
        s.println("<Leaf>"+this);
    }
    
 
 
 
    
    // Test2: Collecting statistics on the structure (aggregation)
    
    /**
     * Provides a client-accessible method that pretty-prints the 
     * structure of the aggregate structure using a FunctionVisitor. 
     * Calculates the sum of all File (<i>Leaf</i>) sizes in the structure.
     *
     * @returns the sum of <i>Leaf</i> sizes of all elements in this structure
     */ 
     
    public int Component.subSum() {
        return 0;
    }

    /**
     * Implements <code>subSum()</code> for Composites: The method call 
     * is forwarded to all children, then the results are summed up.
     *
     * @returns the sum of leaf sizes of all elements in this structure
     */

    public int Directory.subSum() {     
        Enumeration enum = FileSystemComposition.aspectOf().recurseFunction(
          this, new FunctionVisitor() { 
            public Object doFunction(Component c) { 
                return new Integer(c.subSum()); 
            }
        }); 
        
        int sum = 0;
        while (enum.hasMoreElements()) {
            sum += ((Integer) enum.nextElement()).intValue();
        }
        return sum;
    }
    
    /**
     * Implements <code>subSum()</code> for <i>Leaf</i>s: Simply returns
     * the <i>Leaf</i>'s size.
     *
     * @returns the leaf id
     */

    public int File.subSum() {
        return size;
    }
}       

