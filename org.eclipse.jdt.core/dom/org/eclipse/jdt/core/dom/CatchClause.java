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
 * Catch clause AST node type.
 *
 * <pre>
 * CatchClause:
 * 			<b>catch</b> <b>(</b> FormalParameter <b>)</b> Block
 * </pre>
 * 
 * @since 2.0
 */
public class CatchClause extends ASTNode {
	/**
	 * The body; lazily initialized; defaults to an empty block.
	 */
	private Block body = null;

	/**
	 * The exception variable declaration; lazily initialized; defaults to a
	 * unspecified, but legal, variable declaration.
	 */
	private SingleVariableDeclaration exceptionDecl = null;

	/**
	 * Creates a new AST node for a catch clause owned by the given 
	 * AST. By default, the catch clause declares an unspecified, but legal, 
	 * exception declaration and has an empty block.
	 * <p>
	 * N.B. This constructor is package-private.
	 * </p>
	 * 
	 * @param ast the AST that is to own this node
	 */
	CatchClause(AST ast) {
		super(ast);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	public int getNodeType() {
		return CATCH_CLAUSE;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	ASTNode clone(AST target) {
		CatchClause result = new CatchClause(target);
		result.setBody((Block) getBody().clone(target));
		result.setException(
			(SingleVariableDeclaration) ASTNode.copySubtree(target, getException()));
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
			acceptChild(visitor, getException());
			acceptChild(visitor, getBody());
		}
		visitor.endVisit(this);
	}
	
	/**
	 * Returns the exception variable declaration of this catch clause.
	 * 
	 * @return the exception variable declaration node
	 */ 
	public SingleVariableDeclaration getException() {
		if (exceptionDecl == null) {
			// lazy initialize - use setter to ensure parent link set too
			setException(new SingleVariableDeclaration(getAST()));
		}
		return exceptionDecl;
	}
		
	/**
	 * Sets the variable declaration of this catch clause.
	 * 
	 * @param decl the exception variable declaration node
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * <li>a cycle in would be created</li>
	 * </ul>
	 */ 
	public void setException(SingleVariableDeclaration exception) {
		if (exception == null) {
			throw new IllegalArgumentException();
		}
		// a CatchClause may occur inside an 
		// SingleVariableDeclaration - must check cycles
		replaceChild(this.exceptionDecl, exception, true);
		this.exceptionDecl= exception;
	}
	
	/**
	 * Returns the body of this catch clause.
	 * 
	 * @return the catch clause body
	 */ 
	public Block getBody() {
		if (body == null) {
			// lazy initialize - use setter to ensure parent link set too
			setBody(new Block(getAST()));
		}
		return body;
	}
	
	/**
	 * Sets the body of this catch clause.
	 * 
	 * @param body the catch clause block node
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
		// a CatchClause may occur in a Block - must check cycles
		replaceChild(this.body, body, true);
		this.body = body;
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
			+ (exceptionDecl == null ? 0 : getException().treeSize())
			+ (body == null ? 0 : getBody().treeSize());
	}
}
