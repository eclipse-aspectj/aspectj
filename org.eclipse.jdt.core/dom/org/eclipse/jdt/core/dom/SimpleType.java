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
 * Type node for a named class or interface type.
 * <p>
 * This kind of node is used to convert a name (<code>Name</code>) into a type
 * (<code>Type</code>) by wrapping it.
 * </p>
 * 
 * @since 2.0
 */
public class SimpleType extends Type {
	/** 
	 * The type name node; lazily initialized; defaults to a type with
	 * an unspecfied, but legal, name.
	 */
	private Name typeName = null;
	
	/**
	 * Creates a new unparented node for a simple type owned by the given AST.
	 * By default, an unspecified, but legal, name.
	 * <p>
	 * N.B. This constructor is package-private.
	 * </p>
	 * 
	 * @param ast the AST that is to own this node
	 */
	SimpleType(AST ast) {
		super(ast);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	public int getNodeType() {
		return SIMPLE_TYPE;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	ASTNode clone(AST target) {
		SimpleType result = new SimpleType(target);
		result.setName((Name) ((ASTNode) getName()).clone(target));
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
			acceptChild(visitor, getName());
		}
		visitor.endVisit(this);
	}
	
	/**
	 * Returns the name of this simple type.
	 * 
	 * @return the name of this simple type
	 */ 
	public Name getName() {
		if (typeName == null) {
			// lazy initialize - use setter to ensure parent link set too
			setName(new SimpleName(getAST()));
		}
		return typeName;
	}
	
	/**
	 * Sets the name of this simple type to the given name.
	 * 
	 * @param typeName the new name of this simple type
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * </ul>
	 */ 
	public void setName(Name typeName) {
		if (typeName == null) {
			throw new IllegalArgumentException();
		}
		replaceChild((ASTNode) this.typeName, (ASTNode) typeName, false);
		this.typeName = typeName;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int memSize() {
		// treat Code as free
		return BASE_NODE_SIZE + 1 * 4;
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int treeSize() {
		return 
			memSize()
			+ (typeName == null ? 0 : getName().treeSize());
	}
}

