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
 * Simple or qualified "super" field access expression AST node type.
 *
 * <pre>
 * SuperFieldAccess:
 *     [ ClassName <b>.</b> ] <b>super</b> <b>.</b> Identifier
 * </pre>
 * 
 * @since 2.0
 */
public class SuperFieldAccess extends Expression {

	/**
	 * The optional qualifier; <code>null</code> for none; defaults to none.
	 */
	private Name optionalQualifier = null;

	/**
	 * The field; lazily initialized; defaults to an unspecified,
	 * but legal, simple field name.
	 */
	private SimpleName fieldName = null;

	/**
	 * Creates a new unparented node for a super field access expression owned
	 * by the given AST. By default, field name is an unspecified, but legal, 
	 * name, and there is no qualifier.
	 * <p>
	 * N.B. This constructor is package-private.
	 * </p>
	 * 
	 * @param ast the AST that is to own this node
	 */
	SuperFieldAccess(AST ast) {
		super(ast);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	public int getNodeType() {
		return SUPER_FIELD_ACCESS;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	ASTNode clone(AST target) {
		SuperFieldAccess result = new SuperFieldAccess(target);
		result.setName((SimpleName) ASTNode.copySubtree(target, getName()));
		result.setQualifier((Name) ASTNode.copySubtree(target, getQualifier()));
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
			acceptChild(visitor, getQualifier());
			acceptChild(visitor, getName());
		}
		visitor.endVisit(this);
	}
	
	/**
	 * Returns the qualifier of this "super" field access expression, or 
	 * <code>null</code> if there is none.
	 * 
	 * @return the qualifier name node, or <code>null</code> if there is none
	 */ 
	public Name getQualifier() {
		return optionalQualifier;
	}
	
	/**
	 * Sets or clears the qualifier of this "super" field access expression.
	 * 
	 * @param name the qualifier name node, or <code>null</code> if 
	 *    there is none
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * </ul>
	 */ 
	public void setQualifier(Name name) {
		// a SuperFieldAccess cannot occur inside a Name - no cycle check
		replaceChild(this.optionalQualifier, name, false);
		this.optionalQualifier = name;
	}

	/**
	 * Returns the name of the field accessed in this "super" field access 
	 * expression.
	 * 
	 * @return the field name
	 */ 
	public SimpleName getName() {
		if (fieldName == null) {
			setName(new SimpleName(getAST()));
		}
		return fieldName;
	}
		
	/**
	 * Sets the name of the field accessed in this "super" field access 
	 * expression.
	 * 
	 * @param fieldName the field name
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * </ul>
	 */ 
	public void setName(SimpleName fieldName) {
		if (fieldName == null) {
			throw new IllegalArgumentException();
		}
		// a FieldAccess cannot occur inside a SimpleName
		replaceChild(this.fieldName, fieldName, false);
		this.fieldName = fieldName;
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
			+ (optionalQualifier == null ? 0 : getQualifier().treeSize())
			+ (fieldName == null ? 0 : getName().treeSize());
	}
}

