/*******************************************************************************
 * Copyright (c) 2002 International Business Machines Corp. and others.
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
 * Instanceof expression AST node type.
 *
 * Range 0: first character of left operand expression through last character
 * of the right operand expression.
 *
 * <pre>
 * InstanceofExpression:
 *    Expression <b>instanceof</b> Type
 * </pre>
 * 
 * @since 2.0
 */
public class InstanceofExpression extends Expression {

	/**
	 * The left operand; lazily initialized; defaults to an unspecified,
	 * but legal, simple name.
	 */
	private Expression leftOperand = null;

	/**
	 * The right operand; lazily initialized; defaults to an unspecified,
	 * but legal, simple type.
	 */
	private Type rightOperand = null;

	/**
	 * Creates a new AST node for an instanceof expression owned by the given 
	 * AST. By default, the node has unspecified (but legal) operator,
	 * left and right operands.
	 * 
	 * @param ast the AST that is to own this node
	 */
	InstanceofExpression(AST ast) {
		super(ast);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	public int getNodeType() {
		return INSTANCEOF_EXPRESSION;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	ASTNode clone(AST target) {
		InstanceofExpression result = new InstanceofExpression(target);
		result.setLeftOperand((Expression) getLeftOperand().clone(target));
		result.setRightOperand((Type) getRightOperand().clone(target));
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
			acceptChild(visitor, getLeftOperand());
			acceptChild(visitor, getRightOperand());
		}
		visitor.endVisit(this);
	}
	
	/**
	 * Returns the left operand of this instanceof expression.
	 * 
	 * @return the left operand node
	 */ 
	public Expression getLeftOperand() {
		if (leftOperand  == null) {
			// lazy initialize - use setter to ensure parent link set too
			setLeftOperand(new SimpleName(getAST()));
		}
		return leftOperand;
	}
		
	/**
	 * Sets the left operand of this instanceof expression.
	 * 
	 * @param expression the left operand node
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * <li>a cycle in would be created</li>
	 * </ul>
	 */ 
	public void setLeftOperand(Expression expression) {
		if (expression == null) {
			throw new IllegalArgumentException();
		}
		// an InfixExpression may occur inside a Expression - must check cycles
		replaceChild(this.leftOperand, expression, true);
		this.leftOperand = expression;
	}

	/**
	 * Returns the right operand of this instanceof expression.
	 * 
	 * @return the right operand node
	 */ 
	public Type getRightOperand() {
		if (rightOperand  == null) {
			// lazy initialize - use setter to ensure parent link set too
			setRightOperand(new SimpleType(getAST()));
		}
		return rightOperand;
	}
		
	/**
	 * Sets the right operand of this instanceof expression.
	 * 
	 * @param referenceType the right operand node
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * <li>a cycle in would be created</li>
	 * </ul>
	 */ 
	public void setRightOperand(Type referenceType) {
		if (referenceType == null) {
			throw new IllegalArgumentException();
		}
		// an InstanceofExpression may occur inside a Expression - must check cycles
		replaceChild(this.rightOperand, referenceType, true);
		this.rightOperand = referenceType;
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int memSize() {
		// treat Operator as free
		return BASE_NODE_SIZE + 2 * 4;
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int treeSize() {
		return 
			memSize()
			+ (leftOperand == null ? 0 : getLeftOperand().treeSize())
			+ (rightOperand == null ? 0 : getRightOperand().treeSize());
	}
}
