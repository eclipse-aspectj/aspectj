/*******************************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
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
 * Anonymous class declaration AST node type. This type of node appears
 * only as a child on a class instance creation expression.
 *
 * <pre>
 * AnonymousClassDeclaration:
 *        <b>{</b> ClassBodyDeclaration <b>}</b>
 * </pre>
 * 
 * @see ClassInstanceCreation
 * @since 2.0
 */
public class AnonymousClassDeclaration extends ASTNode {

	/**
	 * The body declarations (element type: <code>BodyDeclaration</code>).
	 * Defaults to none.
	 */
	private ASTNode.NodeList bodyDeclarations = 
		new ASTNode.NodeList(true, BodyDeclaration.class);

	/**
	 * Creates a new AST node for an anonymous class declaration owned 
	 * by the given AST. By default, the list of body declarations is empty.
	 * <p>
	 * N.B. This constructor is package-private; all subclasses must be 
	 * declared in the same package; clients are unable to declare 
	 * additional subclasses.
	 * </p>
	 * 
	 * @param ast the AST that is to own this node
	 */
	AnonymousClassDeclaration(AST ast) {
		super(ast);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	public int getNodeType() {
		return ANONYMOUS_CLASS_DECLARATION;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	ASTNode clone(AST target) {
		AnonymousClassDeclaration result = new AnonymousClassDeclaration(target);
		result.bodyDeclarations().addAll(
			ASTNode.copySubtrees(target, bodyDeclarations()));
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
			acceptChildren(visitor, bodyDeclarations);
		}
		visitor.endVisit(this);
	}
	
	/**
	 * Returns the live ordered list of body declarations of this
	 * anonymous class declaration.
	 * 
	 * @return the live list of body declarations
	 *    (element type: <code>BodyDeclaration</code>)
	 */ 
	public List bodyDeclarations() {
		return bodyDeclarations;
	}

	/**
	 * Resolves and returns the binding for the anonymous class declared in
	 * this declaration.
	 * <p>
	 * Note that bindings are generally unavailable unless requested when the
	 * AST is being built.
	 * </p>
	 * 
	 * @return the binding, or <code>null</code> if the binding cannot be 
	 *    resolved
	 */	
	public ITypeBinding resolveBinding() {
		return getAST().getBindingResolver().resolveType(this);
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int memSize() {
		// treat Code as free
		return BASE_NODE_SIZE + 4;
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int treeSize() {
		return 
			memSize()
			+ bodyDeclarations.listSize();
	}
}
