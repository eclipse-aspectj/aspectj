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
 * Switch statement AST node type.
 * <p>
 * <pre>
 * SwitchStatement:
 *		<b>switch</b> <b>(</b> Expression <b>)</b> 
 * 			<b>{</b> { SwitchCase | Statement } } <b>}</b>
 * SwitchCase:
 *		<b>case</b> Expression  <b>:</b>
 *		<b>default</b> <b>:</b>
 * </pre>
 * <code>SwitchCase</code> nodes are treated as a kind of
 * <code>Statement</code>.
 * </p>
 * 
 * @since 2.0
 */
public class SwitchStatement extends Statement {
			
	/**
	 * The expression; lazily initialized; defaults to a unspecified, but legal,
	 * expression.
	 */
	private Expression expression = null;

	/**
	 * The statements and SwitchCase nodes
	 * (element type: <code>Statement</code>).
	 * Defaults to an empty list.
	 */
	private ASTNode.NodeList statements =
		new ASTNode.NodeList(true, Statement.class);
	
	/**
	 * Creates a new unparented switch statement node owned by the given 
	 * AST. By default, the swicth statement has an unspecified, but legal,
	 * expression, and an empty list of switch groups.
	 * <p>
	 * N.B. This constructor is package-private.
	 * </p>
	 * 
	 * @param ast the AST that is to own this node
	 */
	SwitchStatement(AST ast) {
		super(ast);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	public int getNodeType() {
		return SWITCH_STATEMENT;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	ASTNode clone(AST target) {
		SwitchStatement result = new SwitchStatement(target);
		result.setLeadingComment(getLeadingComment());
		result.setExpression((Expression) getExpression().clone(target));
		result.statements().addAll(ASTNode.copySubtrees(target, statements()));
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
			acceptChildren(visitor, statements);
		}
		visitor.endVisit(this);
	}
	
	/**
	 * Returns the expression of this switch statement.
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
	 * Sets the expression of this switch statement.
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
		// a SwitchStatement may occur inside an Expression 
		// must check cycles
		replaceChild(this.expression, expression, true);
		this.expression = expression;
	}
	
	/**
	 * Returns the live ordered list of statements for this switch statement.
	 * Within this list, <code>SwitchCase</code> nodes mark the start of 
	 * the switch groups.
	 * 
	 * @return the live list of switch group nodes
	 *    (element type: <code>SwitchGroups</code>)
	 */ 
	public List statements() {
		return statements;
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
			+ statements.listSize();
	}
}
