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
 * Method invocation expression AST node type.
 *
 * <pre>
 * MethodInvocation:
 *		[ Expression <b>.</b> ] Identifier 
 * 			<b>(</b> [ Expression { <b>,</b> Expression } ] <b>)</b>
 * </pre>
 * 
 * @since 2.0
 */
public class MethodInvocation extends Expression {
	
	/**
	 * The expression; <code>null</code> for none; defaults to none.
	 */
	private Expression optionalExpression = null;
	
	/**
	 * The method name; lazily initialized; defaults to a unspecified,
	 * legal Java method name.
	 */
	private SimpleName methodName = null;
	
	/**
	 * The list of argument expressions (element type: 
	 * <code>Expression</code>). Defaults to an empty list.
	 */
	private ASTNode.NodeList arguments =
		new ASTNode.NodeList(true, Expression.class);

	/**
	 * Creates a new AST node for a method invocation expression owned by the 
	 * given AST. By default, no expression, an unspecified, but legal, method
	 * name, and an empty list of arguments.
	 * 
	 * @param ast the AST that is to own this node
	 */
	MethodInvocation(AST ast) {
		super(ast);	
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	public int getNodeType() {
		return METHOD_INVOCATION;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	ASTNode clone(AST target) {
		MethodInvocation result = new MethodInvocation(target);
		result.setName((SimpleName) getName().clone(target));
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
			acceptChild(visitor, getName());
			acceptChildren(visitor, arguments);
		}
		visitor.endVisit(this);
	}
	
	/**
	 * Returns the expression of this method invocation expression, or 
	 * <code>null</code> if there is none.
	 * 
	 * @return the expression node, or <code>null</code> if there is none
	 */ 
	public Expression getExpression() {
		return optionalExpression;
	}
	
	/**
	 * Sets or clears the expression of this method invocation expression.
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
		// a MethodInvocation may occur inside an Expression - must check cycles
		replaceChild(this.optionalExpression, expression, true);
		this.optionalExpression = expression;
	}

	/**
	 * Returns the name of the method invoked in this expression.
	 * 
	 * @return the method name node
	 */ 
	public SimpleName getName() {
		if (methodName == null) {
			// lazy initialize - use setter to ensure parent link set too
			setName(new SimpleName(getAST()));
		}
		return methodName;
	}
	
	/**
	 * Sets the name of the method invoked in this expression to the
	 * given name.
	 * 
	 * @param name the new method name
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * </ul>
	 */ 
	public void setName(SimpleName name) {
		if (name == null) {
			throw new IllegalArgumentException();
		}
		replaceChild(this.methodName, name, false);
		this.methodName = name;
	}

	/**
	 * Returns the live ordered list of argument expressions in this method
	 * invocation expression.
	 * 
	 * @return the live list of argument expressions 
	 *    (element type: <code>Expression</code>)
	 */ 
	public List arguments() {
		return arguments;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int memSize() {
		// treat Code as free
		return BASE_NODE_SIZE + 3 * 4;
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int treeSize() {
		return 
			memSize()
			+ (optionalExpression == null ? 0 : getExpression().treeSize())
			+ (methodName == null ? 0 : getName().treeSize())
			+ arguments.listSize();
	}
}

