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

import java.util.List;

/**
 * Array initializer AST node type.
 *
 * <pre>
 * ArrayInitializer:
 * 		<b>{</b> [ Expression { <b>,</b> Expression} [ <b>,</b> ]] <b>}</b>
 * </pre>
 * 
 * @since 2.0
 */
public class ArrayInitializer extends Expression {
	
	/**
	 * The list of expressions (element type:
	 * <code>Expression</code>). Defaults to an empty list.
	 */
	private ASTNode.NodeList expressions =
		new ASTNode.NodeList(true, Expression.class);

	/**
	 * Creates a new AST node for an array initializer owned by the 
	 * given AST. By default, the list of expressions is empty.
	 * 
	 * @param ast the AST that is to own this node
	 */
	ArrayInitializer(AST ast) {
		super(ast);	
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	public int getNodeType() {
		return ARRAY_INITIALIZER;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	ASTNode clone(AST target) {
		ArrayInitializer result = new ArrayInitializer(target);
		result.expressions().addAll(ASTNode.copySubtrees(target, expressions()));
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
			acceptChildren(visitor, expressions);
		}
		visitor.endVisit(this);
	}
	
	/**
	 * Returns the live ordered list of expressions in this array initializer.
	 * 
	 * @return the live list of expressions 
	 *    (element type: <code>Expression</code>)
	 */ 
	public List expressions() {
		return expressions;
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int memSize() {
		return BASE_NODE_SIZE + 1 * 4;
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int treeSize() {
		return memSize() + expressions.listSize();
	}
}

