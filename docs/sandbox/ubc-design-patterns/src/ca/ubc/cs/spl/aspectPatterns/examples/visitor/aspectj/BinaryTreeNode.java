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
 * Implements a <i>ConcreteElement</i> of the aggregate strcuture. This is a
 * non-terminal binary tree element.
 *
 * @author  Jan Hannemann
 * @author  Gregor Kiczales
 * @version 1.1, 02/17/04
 */

public class BinaryTreeNode implements Visitable {

    /**
     * the left subtree
     */
    
	protected Visitable left;

    /**
     * the right subtree
     */
    
	protected Visitable right;
	 	
	/**
	 * Accessor for the left subtree.
	 *
	 * @return the left subtree.
	 */
	
	public Visitable getLeft() {
	    return left;
	}

	/**
	 * Accessor for the right subtree.
	 *
	 * @return the right subtree.
	 */
	
	public Visitable getRight() {
	    return right;
	}

    /**
     * Creates a non-terminal node of a binary tree. 
     *
     * @param l the new left subtree.
     * @param l the new left subtree.
     */
     
	public BinaryTreeNode(Visitable left, Visitable right) {
		this.left  = left;
		this.right = right;
	}
}