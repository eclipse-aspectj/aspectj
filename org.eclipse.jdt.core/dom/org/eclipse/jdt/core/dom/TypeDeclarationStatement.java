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
 * Local type declaration statement AST node type.
 * <p>
 * This kind of node is used to convert a type declaration
 * (<code>TypeDeclaration</code>) into a statement
 * (<code>Statement</code>) by wrapping it.
 * </p>
 * <pre>
 * TypeDeclarationStatement:
 *    TypeDeclaration
 * </pre>
 * 
 * @since 2.0
 */
public class TypeDeclarationStatement extends Statement {
	
	/**
	 * The type declaration; lazily initialized; defaults to a unspecified, 
	 * but legal, type declaration.
	 */
	private TypeDeclaration typeDecl = null;

	/**
	 * Creates a new unparented local type declaration statement node owned 
	 * by the given AST. By default, the local type declaration is an
	 * unspecified, but legal, type declaration.
	 * <p>
	 * N.B. This constructor is package-private.
	 * </p>
	 * 
	 * @param ast the AST that is to own this node
	 */
	TypeDeclarationStatement(AST ast) {
		super(ast);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	public int getNodeType() {
		return TYPE_DECLARATION_STATEMENT;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	ASTNode clone(AST target) {
		TypeDeclarationStatement result = 
			new TypeDeclarationStatement(target);
		result.setLeadingComment(getLeadingComment());
		result.setTypeDeclaration(
			(TypeDeclaration) getTypeDeclaration().clone(target));
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
			acceptChild(visitor, getTypeDeclaration());
		}
		visitor.endVisit(this);
	}
	
	/**
	 * Returns the type declaration of this local type declaration
	 * statement.
	 * 
	 * @return the type declaration node
	 */ 
	public TypeDeclaration getTypeDeclaration() {
		if (typeDecl == null) {
			// lazy initialize - use setter to ensure parent link set too
			setTypeDeclaration(new TypeDeclaration(getAST()));
		}
		return typeDecl;
	}
		
	/**
	 * Sets the type declaration of this local type declaration
	 * statement.
	 * 
	 * @param decl the type declaration node
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * <li>a cycle in would be created</li>
	 * </ul>
	 */ 
	public void setTypeDeclaration(TypeDeclaration decl) {
		if (decl == null) {
			throw new IllegalArgumentException();
		}
		// a TypeDeclarationStatement may occur inside an 
		// TypeDeclaration - must check cycles
		replaceChild(this.typeDecl, decl, true);
		this.typeDecl= decl;
	}
	/**
	 * Resolves and returns the binding for the class or interface declared in
	 * this type declaration statement.
	 * <p>
	 * Note that bindings are generally unavailable unless requested when the
	 * AST is being built.
	 * </p>
	 * 
	 * @return the binding, or <code>null</code> if the binding cannot be 
	 *    resolved
	 */	
	public ITypeBinding resolveBinding() {
		// forward request to the wrapped type declaration
		TypeDeclaration d = getTypeDeclaration();
		return d.resolveBinding();
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
		return
			memSize()
			+ (typeDecl == null ? 0 : getTypeDeclaration().treeSize());
	}
}

