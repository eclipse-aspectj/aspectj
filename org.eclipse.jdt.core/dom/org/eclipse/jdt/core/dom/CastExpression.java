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
 * Cast expression AST node type.
 *
 * <pre>
 * CastExpression:
 *    <b>(</b> Type <b>)</b> Expression 
 * </pre>
 * 
 * @since 2.0
 */
public class CastExpression extends Expression {

	/**
	 * The type; lazily initialized; defaults to a unspecified,
	 * legal type.
	 */
	private Type type = null;

	/**
	 * The expression; lazily initialized; defaults to a unspecified, but legal,
	 * expression.
	 */
	private Expression expression = null;

	/**
	 * Creates a new AST node for a cast expression owned by the given 
	 * AST. By default, the type and expression are unspecified (but legal).
	 * <p>
	 * N.B. This constructor is package-private.
	 * </p>
	 * 
	 * @param ast the AST that is to own this node
	 */
	CastExpression(AST ast) {
		super(ast);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	public int getNodeType() {
		return CAST_EXPRESSION;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	ASTNode clone(AST target) {
		CastExpression result = new CastExpression(target);
		result.setType((Type) getType().clone(target));
		result.setExpression((Expression) getExpression().clone(target));
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
			acceptChild(visitor, getType());
			acceptChild(visitor, getExpression());
		}
		visitor.endVisit(this);
	}
	
	/**
	 * Returns the type in this cast expression.
	 * 
	 * @return the type
	 */ 
	public Type getType() {
		if (type == null) {
			// lazy initialize - use setter to ensure parent link set too
			setType(getAST().newPrimitiveType(PrimitiveType.INT));
		}
		return type;
	}

	/**
	 * Sets the type in this cast expression to the given type.
	 * 
	 * @param type the new type
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * </ul>
	 */ 
	public void setType(Type type) {
		if (type == null) {
			throw new IllegalArgumentException();
		}
		// a CastExpression cannot occur inside a Type - cycles not possible
		replaceChild(this.type, type, false);
		this.type = type;
	}
	
	/**
	 * Returns the expression of this cast expression.
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
	 * Sets the expression of this cast expression.
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
		// a CastExpression may occur inside an Expression 
		// must check cycles
		replaceChild(this.expression, expression, true);
		this.expression = expression;
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
			+ (expression == null ? 0 : getExpression().treeSize())
			+ (type == null ? 0 : getType().treeSize());
	}
}
