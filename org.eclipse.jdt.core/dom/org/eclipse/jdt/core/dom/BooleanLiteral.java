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
 * Boolean literal node.
 * 
 * <pre>
 * BooleanLiteral:
 * 		<b>true</b>
 * 		<b>false</b>
 * </pre>
 * 
 * @since 2.0
 */
public class BooleanLiteral extends Expression {
	
	/**
	 * The boolean; defaults to the literal for <code>false</code>.
	 */
	private boolean value = false;

	/**
	 * Creates a new unparented boolean literal node owned by the given AST.
	 * <p>
	 * N.B. This constructor is package-private.
	 * </p>
	 * 
	 * @param ast the AST that is to own this node
	 */
	BooleanLiteral(AST ast) {
		super(ast);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	public int getNodeType() {
		return BOOLEAN_LITERAL;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	ASTNode clone(AST target) {
		BooleanLiteral result = new BooleanLiteral(target);
		result.setBooleanValue(booleanValue());
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
		visitor.endVisit(this);
	}
	
	/**
	 * Returns the boolean value of this boolean literal node.
	 * 
	 * @return <code>true</code> for the boolean literal spelled
	 *    <code>"true"</code>, and <code>false</code> for the boolean literal 
	 *    spelled <code>"false"</code>.
	 */ 
	public boolean booleanValue() {
		return value;
	}
		
	/**
	 * Sets the boolean value of this boolean literal node.
	 * 
	 * @param value <code>true</code> for the boolean literal spelled
	 *    <code>"true"</code>, and <code>false</code> for the boolean literal 
	 *    spelled <code>"false"</code>.
	 */ 
	public void setBooleanValue(boolean value) {
		modifying();
		this.value = value;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int memSize() {
		return BASE_NODE_SIZE + 1 * 4;
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int treeSize() {
		return memSize();
	}
}

