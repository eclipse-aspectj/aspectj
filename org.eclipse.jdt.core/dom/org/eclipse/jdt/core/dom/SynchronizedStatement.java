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
 * Synchronized statement AST node type.
 *
 * <pre>
 * SynchronizedStatement:
 *    <b>synchronized</b> <b>(</b> Expression <b>)</b> Block
 * </pre>
 * 
 * @since 2.0
 */
public class SynchronizedStatement extends Statement {

	/**
	 * The expression; lazily initialized; defaults to an unspecified, but 
	 * legal, expression.
	 */
	private Expression expression = null;

	/**
	 * The body; lazily initialized; defaults to an empty block.
	 */
	private Block body = null;

	/**
	 * Creates a new unparented synchronized statement node owned by the given 
	 * AST. By default, the expression is unspecified, but legal, and the
	 * blody is an empty block.
	 * <p>
	 * N.B. This constructor is package-private.
	 * </p>
	 * 
	 * @param ast the AST that is to own this node
	 */
	SynchronizedStatement(AST ast) {
		super(ast);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	public int getNodeType() {
		return SYNCHRONIZED_STATEMENT;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	ASTNode clone(AST target) {
		SynchronizedStatement result = new SynchronizedStatement(target);
		result.setLeadingComment(getLeadingComment());
		result.setExpression((Expression) getExpression().clone(target));
		result.setBody((Block) getBody().clone(target));
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
			acceptChild(visitor, getBody());
		}
		visitor.endVisit(this);
	}
	
	/**
	 * Returns the expression of this synchronized statement.
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
	 * Sets the expression of this synchronized statement.
	 * 
	 * @param expression the expression node
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
		// a SynchronizedStatement may occur inside an Expression
		// must check cycles
		replaceChild(this.expression, expression, true);
		this.expression = expression;
	}

	/**
	 * Returns the body of this synchronized statement.
	 * 
	 * @return the body block node
	 */ 
	public Block getBody() {
		if (body == null) {
			// lazy initialize - use setter to ensure parent link set too
			setBody(new Block(getAST()));
		}
		return body;
	}
	
	/**
	 * Sets the body of this synchronized statement.
	 * 
	 * @param block the body statement node
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * <li>a cycle in would be created</li>
	 * </ul>
	 */ 
	public void setBody(Block block) {
		if (block == null) {
			throw new IllegalArgumentException();
		}
		// a WhileStatement may occur inside a Statement - must check cycles
		replaceChild(this.body, block, true);
		this.body = block;
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
			+ (body == null ? 0 : getBody().treeSize());
	}
}