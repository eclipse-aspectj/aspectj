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
 * Switch case AST node type. A switch case is a special kind of node used only
 * in switch statements. It is a <code>Statement</code> in name only.
 * <p>
 * <pre>
 * SwitchCase:
 *		<b>case</b> Expression  <b>:</b>
 *		<b>default</b> <b>:</b>
 * </pre>
 * </p>
 * 
 * @since 2.0
 */
public class SwitchCase extends Statement {
	
	/**
	 * The expression; <code>null</code> for none; lazily initialized (but
	 * does <b>not</b> default to none).
	 * @see #expressionInitialized
	 */
	private Expression optionalExpression = null;

	/**
	 * Indicates whether <code>optionalExpression</code> has been initialized.
	 */
	private boolean expressionInitialized = false;
	
	/**
	 * Creates a new AST node for a switch case pseudo-statement owned by the 
	 * given AST. By default, there is an unspecified, but legal, expression.
	 * 
	 * @param ast the AST that is to own this node
	 */
	SwitchCase(AST ast) {
		super(ast);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	public int getNodeType() {
		return SWITCH_CASE;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	ASTNode clone(AST target) {
		SwitchCase result = new SwitchCase(target);
		result.setLeadingComment(getLeadingComment());
		result.setExpression(
			(Expression) ASTNode.copySubtree(target, getExpression()));
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
			acceptChild(visitor, getExpression());
		}
		visitor.endVisit(this);
	}
	
	/**
	 * Returns the expression of this switch case, or 
	 * <code>null</code> if there is none (the "default:" case).
	 * 
	 * @return the expression node, or <code>null</code> if there is none
	 */ 
	public Expression getExpression() {
		if (!expressionInitialized) {
			// lazy initialize - use setter to ensure parent link set too
			setExpression(new SimpleName(getAST()));
		}
		return optionalExpression;
	}
	
	/**
	 * Sets the expression of this switch case, or clears it (turns it into
	 * the  "default:" case).
	 * 
	 * @param expression the expression node, or <code>null</code> to 
	 *    turn it into the  "default:" case
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * <li>a cycle in would be created</li>
	 * </ul>
	 */ 
	public void setExpression(Expression expression) {
		// a ReturnStatement may occur inside an Expression - must check cycles
		replaceChild(this.optionalExpression, expression, true);
		this.optionalExpression = expression;
		expressionInitialized = true;
	}

	/**
	 * Returns whether this switch case represents the "default:" case.
	 * <p>
	 * This convenience method is equivalent to
	 * <code>getExpression() == null</code>.
	 * </p>
	 * 
	 * @return <code>true</code> if this is the default switch case, and
	 *    <code>false</code> if this is a non-default switch case
	 */ 
	public boolean isDefault()  {
		return getExpression() == null;
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
			+ (optionalExpression == null ? 0 : optionalExpression.treeSize());
	}
}
