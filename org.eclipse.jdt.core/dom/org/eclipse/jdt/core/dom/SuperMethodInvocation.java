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
 * Simple or qualified "super" method invocation expression AST node type.
 *
 * <pre>
 * SuperMethodInvocation:
 *     [ ClassName <b>.</b> ] <b>super</b> <b>.</b> Identifier
 * 			<b>(</b> [ Expression { <b>,</b> Expression } ] <b>)</b>
 * </pre>
 * 
 * @since 2.0
 */
public class SuperMethodInvocation extends Expression {
	
	/**
	 * The optional qualifier; <code>null</code> for none; defaults to none.
	 */
	private Name optionalQualifier = null;

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
	 * Creates a new AST node for a "super" method invocation expression owned
	 * by the given AST. By default, no qualifier, an unspecified, but legal, 
	 * method name, and an empty list of arguments.
	 * 
	 * @param ast the AST that is to own this node
	 */
	SuperMethodInvocation(AST ast) {
		super(ast);	
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	public int getNodeType() {
		return SUPER_METHOD_INVOCATION;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	ASTNode clone(AST target) {
		SuperMethodInvocation result = new SuperMethodInvocation(target);
		result.setName((SimpleName) getName().clone(target));
		result.setQualifier((Name) ASTNode.copySubtree(target, getQualifier()));
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
			acceptChild(visitor, getQualifier());
			acceptChild(visitor, getName());
			acceptChildren(visitor, arguments);
		}
		visitor.endVisit(this);
	}
	
	/**
	 * Returns the qualifier of this "super" method invocation expression, or 
	 * <code>null</code> if there is none.
	 * 
	 * @return the qualifier name node, or <code>null</code> if there is none
	 */ 
	public Name getQualifier() {
		return optionalQualifier;
	}
	
	/**
	 * Sets or clears the qualifier of this "super" method invocation expression.
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
		// a SuperMethodInvocation cannot occur inside a Name
		replaceChild(this.optionalQualifier, name, false);
		this.optionalQualifier = name;
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
	 * Returns the live ordered list of argument expressions in this 
	 * "super" method invocation expression.
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
			+ (optionalQualifier == null ? 0 : getQualifier().treeSize())
			+ (methodName == null ? 0 : getName().treeSize())
			+ arguments.listSize();
	}
}

