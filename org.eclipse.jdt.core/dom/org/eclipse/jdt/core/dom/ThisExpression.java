/*******************************************************************************
 * Copyright (c) 2001 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jdt.core.dom;

/**
 * Simple or qualified "this" AST node type.
 *
 * <pre>
 * ThisExpression:
 *     [ ClassName <b>.</b> ] <b>this</b>
 * </pre>
 * 
 * @since 2.0
 */
public class ThisExpression extends Expression {
			
	/**
	 * The optional qualifier; <code>null</code> for none; defaults to none.
	 */
	private Name optionalQualifier = null;

	/**
	 * Creates a new AST node for a "this" expression owned by the 
	 * given AST. By default, there is no qualifier.
	 * 
	 * @param ast the AST that is to own this node
	 */
	ThisExpression(AST ast) {
		super(ast);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	public int getNodeType() {
		return THIS_EXPRESSION;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	ASTNode clone(AST target) {
		ThisExpression result = new ThisExpression(target);
		result.setQualifier((Name) ASTNode.copySubtree(target, getQualifier()));
		return result;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	public boolean subtreeMatch(ASTMatcher matcher, Object other) {
		// dispatch to correct overloaded match method
		return matcher.match(this, other);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	void accept0(ASTVisitor visitor) {
		boolean visitChildren = visitor.visit(this);
		if (visitChildren) {
			acceptChild(visitor, getQualifier());
		}
		visitor.endVisit(this);
	}
	
	/**
	 * Returns the qualifier of this "this" expression, or 
	 * <code>null</code> if there is none.
	 * 
	 * @return the qualifier name node, or <code>null</code> if there is none
	 */ 
	public Name getQualifier() {
		return optionalQualifier;
	}
	
	/**
	 * Sets or clears the qualifier of this "this" expression.
	 * 
	 * @param name the qualifier name node, or <code>null</code> if 
	 *    there is none
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * </ul>
	 */ 
	public void setQualifier(Name name) {
		// a ThisExpression cannot occur inside an Expression
		replaceChild(this.optionalQualifier, name, false);
		this.optionalQualifier = name;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int memSize() {
		// treat Operator as free
		return BASE_NODE_SIZE + 1 * 4;
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int treeSize() {
		return 
			memSize()
			+ (optionalQualifier == null ? 0 : getQualifier().treeSize());
	}
}
