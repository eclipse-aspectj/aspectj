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
 * Try statement AST node type.
 *
 * <pre>
 * TryStatement:
 *    <b>try</b> Block 
 * 			{ CatchClause }
 * 			[ <b>finally</b> Block ]
 * </pre>
 * 
 * @since 2.0
 */
public class TryStatement extends Statement {
	
	/**
	 * The body; lazily initialized; defaults to an empty block.
	 */
	private Block body = null;

	/**
	 * The catch clauses (element type: <code>CatchClause</code>).
	 * Defaults to an empty list.
	 */
	private ASTNode.NodeList catchClauses =
		new ASTNode.NodeList(true, CatchClause.class);
	
	/**
	 * The finally block, or <code>null</code> if none.
	 * Defaults to none.
	 */
	private Block optionalFinallyBody = null;

			
	/**
	 * Creates a new AST node for a try statement owned by the given 
	 * AST. By default, the try statement has an empty block, no catch
	 * clauses, and no finally block.
	 * <p>
	 * N.B. This constructor is package-private.
	 * </p>
	 * 
	 * @param ast the AST that is to own this node
	 */
	TryStatement(AST ast) {
		super(ast);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	public int getNodeType() {
		return TRY_STATEMENT;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	ASTNode clone(AST target) {
		TryStatement result = new TryStatement(target);
		result.setLeadingComment(getLeadingComment());
		result.setBody((Block) getBody().clone(target));
		result.catchClauses().addAll(
			ASTNode.copySubtrees(target, catchClauses()));
		result.setFinally(
			(Block) ASTNode.copySubtree(target, getFinally()));
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
			acceptChild(visitor, getBody());
			acceptChildren(visitor, catchClauses);
			acceptChild(visitor, getFinally());
		}
		visitor.endVisit(this);
	}
	
	/**
	 * Returns the body of this try statement.
	 * 
	 * @return the try body
	 */ 
	public Block getBody() {
		if (body == null) {
			// lazy initialize - use setter to ensure parent link set too
			setBody(new Block(getAST()));
		}
		return body;
	}
	
	/**
	 * Sets the body of this try statement.
	 * 
	 * @param body the block node
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * <li>a cycle in would be created</li>
	 * </ul>
	 */ 
	public void setBody(Block body) {
		if (body == null) {
			throw new IllegalArgumentException();
		}
		// a TryStatement may occur in a Block - must check cycles
		replaceChild(this.body, body, true);
		this.body = body;
	}

	/**
	 * Returns the live ordered list of catch clauses for this try statement.
	 * 
	 * @return the live list of catch clauses
	 *    (element type: <code>CatchClause</code>)
	 */ 
	public List catchClauses() {
		return catchClauses;
	}
		
	/**
	 * Returns the finally block of this try statement, or <code>null</code> if 
	 * this try statement has <b>no</b> finally block.
	 * 
	 * @return the finally block, or <code>null</code> if this try statement
	 *    has none
	 */ 
	public Block getFinally() {
		return optionalFinallyBody;
	}

	/**
	 * Sets or clears the finally block of this try statement.
	 * 
	 * @param block the finally block node, or <code>null</code> if 
	 *    there is none
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * <li>a cycle in would be created</li>
	 * </ul>
	 */ 
	public void setFinally(Block block) {
		// a TryStatement may occur in a Block - must check cycles
		replaceChild(this.optionalFinallyBody, block, true);
		this.optionalFinallyBody = block;
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int memSize() {
		return super.memSize() + 3 * 4;
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int treeSize() {
		return
			memSize()
			+ (body == null ? 0 : getBody().treeSize())
			+ catchClauses.listSize()
			+ (optionalFinallyBody == null ? 0 : getFinally().treeSize());
	}
}
