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
 * Assert statement AST node type.
 *
 * <pre>
 * AssertStatement:
 *    <b>assert</b> Expression [ <b>:</b> Expression ] <b>;</b>
 * </pre>
 * 
 * @since 2.0
 */
public class AssertStatement extends Statement {
			
	/**
	 * The expression; lazily initialized; defaults to a unspecified, but legal,
	 * expression.
	 */
	private Expression expression = null;

	/**
	 * The message expression; <code>null</code> for none; defaults to none.
	 */
	private Expression optionalMessageExpression = null;
	
	/**
	 * Creates a new unparented assert statement node owned by the given 
	 * AST. By default, the assert statement has an unspecified, but legal,
	 * expression, and not message expression.
	 * <p>
	 * N.B. This constructor is package-private.
	 * </p>
	 * 
	 * @param ast the AST that is to own this node
	 */
	AssertStatement(AST ast) {
		super(ast);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	public int getNodeType() {
		return ASSERT_STATEMENT;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	ASTNode clone(AST target) {
		AssertStatement result = new AssertStatement(target);
		result.setLeadingComment(getLeadingComment());
		result.setExpression(
			(Expression) ASTNode.copySubtree(target, getExpression()));
		result.setMessage(
			(Expression) ASTNode.copySubtree(target, getMessage()));
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
			acceptChild(visitor, getMessage());
		}
		visitor.endVisit(this);
	}
	
	/**
	 * Returns the first expression of this assert statement.
	 * 
	 * @return the expression node
	 */ 
	public Expression getExpression() {
		if (expression == null) {
			// lazy initialize - use setter to ensure parent link set too
			setExpression(new SimpleName(getAST()));
		}
		return expression;
	}
		
	/**
	 * Sets the first expression of this assert statement.
	 * 
	 * @param expression the new expression node
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * <li>a cycle in would be created</li>
	 * </ul>
	 */ 
	public void setExpression(Expression expression) {
		if (expression == null) {
			throw new IllegalArgumentException();
		}
		// an AssertStatement may occur inside an Expression - must check cycles
		replaceChild(this.expression, expression, true);
		this.expression = expression;
	}

	/**
	 * Returns the message expression of this assert statement, or 
	 * <code>null</code> if there is none.
	 * 
	 * @return the message expression node, or <code>null</code> if there 
	 *    is none
	 */ 
	public Expression getMessage() {
		return optionalMessageExpression;
	}
	
	/**
	 * Sets or clears the message expression of this assert statement.
	 * 
	 * @param expression the message expression node, or <code>null</code> if 
	 *    there is none
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * <li>a cycle in would be created</li>
	 * </ul>
	 */ 
	public void setMessage(Expression expression) {
		// an AsertStatement may occur inside an Expression - must check cycles
		replaceChild(this.optionalMessageExpression, expression, true);
		this.optionalMessageExpression = expression;
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int memSize() {
		return super.memSize() + 2 * 4;
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int treeSize() {
		return
			memSize()
			+ (expression == null ? 0 : getExpression().treeSize())
			+ (optionalMessageExpression == null ? 0 : getMessage().treeSize());
			
	}
}

