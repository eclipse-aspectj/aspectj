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
 * Block statement AST node type.
 *
 * <pre>
 * Block:
 *    <b>{</b> { Statement } <b>}</b>
 * </pre>
 * 
 * @since 2.0
 */
public class Block extends Statement {
	
	/**
	 * The list of statements (element type: <code>Statement</code>). 
	 * Defaults to an empty list.
	 */
	private ASTNode.NodeList statements = 
		new ASTNode.NodeList(true, Statement.class);

	/**
	 * Creates a new unparented block node owned by the given AST.
	 * By default, the block is empty.
	 * <p>
	 * N.B. This constructor is package-private.
	 * </p>
	 * 
	 * @param ast the AST that is to own this node
	 */
	Block(AST ast) {
		super(ast);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	public int getNodeType() {
		return BLOCK;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	ASTNode clone(AST target) {
		Block result = new Block(target);
		result.setLeadingComment(getLeadingComment());
		result.statements().addAll(
			ASTNode.copySubtrees(target, statements()));
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
			acceptChildren(visitor, statements);
		}
		visitor.endVisit(this);
	}
	
	/**
	 * Returns the live list of statements in this block. Adding and
	 * removing nodes from this list affects this node dynamically.
	 * All nodes in this list must be <code>Statement</code>s;
	 * attempts to add any other type of node will trigger an
	 * exception.
	 * 
	 * @return the live list of statements in this block
	 *    (element type: <code>Statement</code>)
	 */ 
	public List statements() {
		return statements;
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int memSize() {
		return super.memSize() + 1 * 4;
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int treeSize() {
		return memSize() + statements.listSize();
	}
}

