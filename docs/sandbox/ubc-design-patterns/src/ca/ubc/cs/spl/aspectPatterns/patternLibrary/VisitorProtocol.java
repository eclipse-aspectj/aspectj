package ca.ubc.cs.spl.aspectPatterns.patternLibrary;

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
 * Implements the abstracted Visitor design pattern.<p> 
 *
 * Intent: <i>Represents an operation to be performed on the elements of an 
 * object structure. Visitor lets you define a new operation without changing
 * the classes of the elements on which it operates</i><p>
 *
 * Note that this implementation only deals with two different kind of nodes:
 * terminal and non-terminal nodes. In cases where the aggregate structure  
 * contains more types of nodes, this aspect cannot be used without 
 * modifications. <p> 
 *
 * Note further that whenever the aggregate structure is unimportant, the
 * additional functionality can be added in a much sipmler using 
 * AspectJ's open classes mechanism (i.e., by using inter-type declarations
 * to implement the desired functionality).
 *
 * @author  Jan Hannemann
 * @author  Gregor Kiczales
 * @version 1.1, 02/17/04
 */

public abstract aspect VisitorProtocol {

    /**
     * Defines the <i>Element</i> role. The inerface is public so that
     * <i>ConcreteVisitor</i>s can use the type.
     */
     
    public interface VisitableNode {}

    /**
     * Defines a <i>ConcreteElement</i> role for non-terminal nodes in 
     * a binary tree structure. The interface is protected as it is only used
     * by concrete subaspects.
     */

	protected interface Node extends VisitableNode {}

    /**
     * Defines a <i>ConcreteElement</i> role for terminal nodes in 
     * a tree structure. The inerface is protected as it is only used
     * by concrete subaspects.
     */

	protected interface Leaf extends VisitableNode {} 
	
    /**
     * This interface is implemented by <i>ConcreteVisitor</i>s. 
     */
	
	public interface Visitor {
	    
	    /**
	     * Defines a method signature for visiting regular nodes.
	     *
	     * @param node the regular node to visit
	     */
	     
		public void visitNode(VisitableNode node);

	    /**
	     * Defines a method signature for visiting leaf nodes.
	     *
	     * @param node the leaf node to visit
	     */

		public void visitLeaf(VisitableNode node);

	    /**
	     * Defines a method signature for returning the visitor's results
	     *
	     * @param node a string containig the visitor's results
	     */

		public String report();
	}
	
    /**
     * Declares <code>accept(..)</code> for visitable nodes 
     *
     * @param visitor the visitor that is to be accepted
     */
     	
	public void VisitableNode.accept(Visitor visitor) {}
	       
    /**
     * Declares <code>accept(..)</code> for regular nodes 
     *
     * @param visitor the visitor that is to be accepted
     */
     
	public void Node.accept(Visitor visitor) { 
	    visitor.visitNode(this); 
	}
	
    /**
     * Declares <code>accept(..)</code> for leaf nodes 
     *
     * @param visitor the visitor that is to be accepted
     */

	public void Leaf.accept(Visitor visitor) { 
	    visitor.visitLeaf(this); 
	}
}