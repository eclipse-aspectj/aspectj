package ca.ubc.cs.spl.aspectPatterns.examples.visitor.aspectj;

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
 * Implements the driver for the Visitor design pattern example.<p> 
 *
 * Intent: <i>Represents an operation to be performed on the elements of an 
 * object structure. Visitor lets you define a new operation without changing
 * the classes of the elements on which it operates</i><p>
 *
 * Participating classes are <code>SummationVisitor</code> and 
 * <code>TraversalVisitor</code> as <i>ConcreteVisitor</i>s, implementing the 
 * <code>VisitorProtocol.Visitor</code> interface. <BR>
 * 
 * <code>BinaryTreeNode</code> and <code>BinaryTreeLeaf</code> are 
 * <i>ConcreteElement</i>s, implementing the <code>Visitable</code> interface.
 * <p>
 *
 * In this example, a binary tree that has int values as leafs is built. 
 * SummationVisitor is a <i>Visitor</i> that collects the sum of 
 * elements in the leafs (should be 6). 
 * 
 * TraversalVisitor is a <i>Visitor</i> that 
 * collects a description of the tree like {{1,2},3}
 *
 * <p><i>This is the AspectJ version.</i><p>
 *
 * Note that <UL>
 * <LI> Every visitor (even the inteface) has to know of each possible element 
 *      type in the object structure. 
 * <LI> Nodes need not to know of the visitor interface; 
 * </UL>
 *
 * @author  Jan Hannemann
 * @author  Gregor Kiczales
 * @version 1.1, 02/17/04
 */

public class Main { 
	
    /**
     * Implements the driver for the Visitor design pattern example.<p> 
     *
     * @param args the command-line parameters, unused
     */


	public static void main(String[] args) { 
	    
	    System.out.println("Building the tree (1): leaves");
		
		BinaryTreeLeaf one   = new BinaryTreeLeaf(1);
		BinaryTreeLeaf two   = new BinaryTreeLeaf(2);
		BinaryTreeLeaf three = new BinaryTreeLeaf(3);
		
	    System.out.println("Building the tree (1): regular nodes");
		
		BinaryTreeNode regN = new BinaryTreeNode(one, two);
		BinaryTreeNode root = new BinaryTreeNode(regN, three);
		
        System.out.println("The tree now looks like this: ");
        System.out.println("         regN                 ");
        System.out.println("        /    \\               ");
        System.out.println("    regN      3               ");
        System.out.println("   /    \\                    ");
        System.out.println("  1      2                    ");
		            
        System.out.println("Visitor 1: SumVisitor, collects the sum of leaf");
        System.out.println("values. Result should be 6.");
				            
		SummationVisitor sumVisitor = new SummationVisitor();  
		root.accept(sumVisitor);
		System.out.println(sumVisitor.report());  
		
        System.out.println("Visitor 2: TraversalVisitor, collects a tree");
        System.out.println("representation. Result should be {{1,2},3}.");
		
		TraversalVisitor traversalVisitor = new TraversalVisitor();  
		root.accept(traversalVisitor);
		System.out.println(traversalVisitor.report());  
	}
}