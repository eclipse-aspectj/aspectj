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
 * Conditional expression AST node type.
 *
 * <pre>
 * ConditionalExpression:
 *    Expression <b>?</b> Expression <b>:</b> Expression
 * </pre>
 * 
 * @since 2.0
 */
public class ConditionalExpression extends Expression {
	
	/**
	 * The condition expression; lazily initialized; defaults to an unspecified,
	 * but legal, expression.
	 */
	private Expression conditionExpression = null;

	/**
	 * The "then" expression; lazily initialized; defaults to an unspecified,
	 * but legal, expression.
	 */
	private Expression thenExpression = null;

	/**
	 * The "else" expression; lazily initialized; defaults to an unspecified,
	 * but legal, expression.
	 */
	private Expression elseExpression = null;

	/**
	 * Creates a new unparented conditional expression node owned by the given 
	 * AST. By default, the condition, "then", and "else" expresssions are
	 * unspecified, but legal.
	 * <p>
	 * N.B. This constructor is package-private.
	 * </p>
	 * 
	 * @param ast the AST that is to own this node
	 */
	ConditionalExpression(AST ast) {
		super(ast);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	public int getNodeType() {
		return CONDITIONAL_EXPRESSION;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	ASTNode clone(AST target) {
		ConditionalExpression result = new ConditionalExpression(target);
		result.setExpression((Expression) getExpression().clone(target));
		result.setThenExpression(
			(Expression) getThenExpression().clone(target));
		result.setElseExpression(
			(Expression) getElseExpression().clone(target));
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
			acceptChild(visitor, getThenExpression());
			acceptChild(visitor, getElseExpression());
		}
		visitor.endVisit(this);
	}
	
	/**
	 * Returns the condition of this conditional expression.
	 * 
	 * @return the condition node
	 */ 
	public Expression getExpression() {
		if (conditionExpression == null) {
			// lazy initialize - use setter to ensure parent link set too
			setExpression(new SimpleName(getAST()));
		}
		return conditionExpression;
	}
	
	/**
	 * Sets the condition of this conditional expression.
	 * 
	 * @param expression the condition node
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
		// a ConditionalExpression may occur inside an Expression
		// must check cycles
		replaceChild(this.conditionExpression, expression, true);
		this.conditionExpression = expression;
	}
	
	/**
	 * Returns the "then" part of this conditional expression.
	 * 
	 * @return the "then" expression node
	 */ 
	public Expression getThenExpression() {
		if (thenExpression == null) {
			// lazy initialize - use setter to ensure parent link set too
			setThenExpression(new SimpleName(getAST()));
		}
		return thenExpression;
	}
	
	/**
	 * Sets the "then" part of this conditional expression.
	 * 
	 * @param expression the "then" expression node
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * <li>a cycle in would be created</li>
	 * </ul>
	 */ 
	public void setThenExpression(Expression expression) {
		if (expression == null) {
			throw new IllegalArgumentException();
		}
		// a ConditionalExpression may occur inside an Expression
		// must check cycles
		replaceChild(this.thenExpression, expression, true);
		this.thenExpression = expression;
	}

	/**
	 * Returns the "else" part of this conditional expression.
	 * 
	 * @return the "else" expression node
	 */ 
	public Expression getElseExpression() {
		if (elseExpression == null) {
			// lazy initialize - use setter to ensure parent link set too
			setElseExpression(new SimpleName(getAST()));
		}
		return elseExpression;
	}
	
	/**
	 * Sets the "else" part of this conditional expression.
	 * 
	 * @param expression the "else" expression node
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * <li>a cycle in would be created</li>
	 * </ul>
	 */ 
	public void setElseExpression(Expression expression) {
		if (expression == null) {
			throw new IllegalArgumentException();
		}
		// a ConditionalExpression may occur inside an Expression
		// must check cycles
		replaceChild(this.elseExpression, expression, true);
		this.elseExpression = expression;
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
			+ (conditionExpression == null ? 0 : getExpression().treeSize())
			+ (thenExpression == null ? 0 : getThenExpression().treeSize())
			+ (elseExpression == null ? 0 : getElseExpression().treeSize());
	}
}
