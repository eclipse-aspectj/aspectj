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
 * Array access expression AST node type.
 *
 * <pre>
 * ArrayAccess:
 *    Expression <b>[</b> Expression <b>]</b>
 * </pre>
 * 
 * @since 2.0
 */
public class ArrayAccess extends Expression {
	
	/**
	 * The array expression; lazily initialized; defaults to an unspecified,
	 * but legal, expression.
	 */
	private Expression arrayExpression = null;

	/**
	 * The index expression; lazily initialized; defaults to an unspecified,
	 * but legal, expression.
	 */
	private Expression indexExpression = null;

	/**
	 * Creates a new unparented array access expression node owned by the given 
	 * AST. By default, the array and index expresssions are unspecified, 
	 * but legal.
	 * <p>
	 * N.B. This constructor is package-private.
	 * </p>
	 * 
	 * @param ast the AST that is to own this node
	 */
	ArrayAccess(AST ast) {
		super(ast);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	public int getNodeType() {
		return ARRAY_ACCESS;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	ASTNode clone(AST target) {
		ArrayAccess result = new ArrayAccess(target);
		result.setArray((Expression) getArray().clone(target));
		result.setIndex((Expression) getIndex().clone(target));
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
			acceptChild(visitor, getArray());
			acceptChild(visitor, getIndex());
		}
		visitor.endVisit(this);
	}
	
	/**
	 * Returns the array expression of this array access expression.
	 * 
	 * @return the array expression node
	 */ 
	public Expression getArray() {
		if (arrayExpression == null) {
			// lazy initialize - use setter to ensure parent link set too
			setArray(new SimpleName(getAST()));
		}
		return arrayExpression;
	}
	
	/**
	 * Sets the array expression of this array access expression.
	 * 
	 * @param expression the array expression node
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * <li>a cycle in would be created</li>
	 * </ul>
	 */ 
	public void setArray(Expression expression) {
		if (expression == null) {
			throw new IllegalArgumentException();
		}
		// an ArrayAccess may occur inside an Expression
		// must check cycles
		replaceChild(this.arrayExpression, expression, true);
		this.arrayExpression = expression;
	}
	
	/**
	 * Returns the index expression of this array access expression.
	 * 
	 * @return the index expression node
	 */ 
	public Expression getIndex() {
		if (indexExpression == null) {
			// lazy initialize - use setter to ensure parent link set too
			setIndex(new SimpleName(getAST()));
		}
		return indexExpression;
	}
	
	/**
	 * Sets the index expression of this array access expression.
	 * 
	 * @param expression the index expression node
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * <li>a cycle in would be created</li>
	 * </ul>
	 */ 
	public void setIndex(Expression expression) {
		if (expression == null) {
			throw new IllegalArgumentException();
		}
		// an ArrayAccess may occur inside an Expression
		// must check cycles
		replaceChild(this.indexExpression, expression, true);
		this.indexExpression = expression;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int memSize() {
		return BASE_NODE_SIZE + 2 * 4;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int treeSize() {
		return
			memSize()
			+ (arrayExpression == null ? 0 : getArray().treeSize())
			+ (indexExpression == null ? 0 : getIndex().treeSize());
	}
}

