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
 * Super constructor invocation expression AST node type.
 *
 * <pre>
 * SuperConstructorInvocation:
 *		[ Expression <b>.</b> ] <b>super</b>
 * 				<b>(</b> [ Expression { <b>,</b> Expression } ] <b>)</b>
 * </pre>
 * 
 * @since 2.0
 */
public class SuperConstructorInvocation extends Statement {
	
	/**
	 * The expression; <code>null</code> for none; defaults to none.
	 */
	private Expression optionalExpression = null;
	
	/**
	 * The list of argument expressions (element type: 
	 * <code>Expression</code>). Defaults to an empty list.
	 */
	private ASTNode.NodeList arguments =
		new ASTNode.NodeList(true, Expression.class);

	/**
	 * Creates a new AST node for an super constructor invocation statement
	 * owned by the given AST. By default, an empty list of arguments.
	 * 
	 * @param ast the AST that is to own this node
	 */
	SuperConstructorInvocation(AST ast) {
		super(ast);	
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	public int getNodeType() {
		return SUPER_CONSTRUCTOR_INVOCATION;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	ASTNode clone(AST target) {
		SuperConstructorInvocation result = new SuperConstructorInvocation(target);
		result.setLeadingComment(getLeadingComment());
		result.setExpression(
			(Expression) ASTNode.copySubtree(target, getExpression()));
		result.arguments().addAll(ASTNode.copySubtrees(target, arguments()));
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
			// visit children in normal left to right reading order
			acceptChild(visitor, getExpression());
			acceptChildren(visitor, arguments);
		}
		visitor.endVisit(this);
	}
	
	/**
	 * Returns the expression of this super constructor invocation statement,
	 * or <code>null</code> if there is none.
	 * 
	 * @return the expression node, or <code>null</code> if there is none
	 */ 
	public Expression getExpression() {
		return optionalExpression;
	}
	
	/**
	 * Sets or clears the expression of this super constructor invocation
	 * statement.
	 * 
	 * @param expression the expression node, or <code>null</code> if 
	 *    there is none
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * <li>a cycle in would be created</li>
	 * </ul>
	 */ 
	public void setExpression(Expression expression) {
		// a SuperConstructorInvocation may occur inside an Expression
		// must check cycles
		replaceChild(this.optionalExpression, expression, true);
		this.optionalExpression = expression;
	}

	/**
	 * Returns the live ordered list of argument expressions in this super
	 * constructor invocation statement.
	 * 
	 * @return the live list of argument expressions 
	 *    (element type: <code>Expression</code>)
	 */ 
	public List arguments() {
		return arguments;
	}

	/**
	 * Resolves and returns the binding for the constructor invoked by this
	 * expression.
	 * <p>
	 * Note that bindings are generally unavailable unless requested when the
	 * AST is being built.
	 * </p>
	 * 
	 * @return the constructor binding, or <code>null</code> if the binding
	 *    cannot be resolved
	 */	
	public IMethodBinding resolveConstructorBinding() {
		return getAST().getBindingResolver().resolveConstructor(this);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int memSize() {
		// treat Code as free
		return BASE_NODE_SIZE + 2 * 4;
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int treeSize() {
		return 
			memSize()
			+ (optionalExpression == null ? 0 : getExpression().treeSize())
			+ arguments.listSize();
	}
}
