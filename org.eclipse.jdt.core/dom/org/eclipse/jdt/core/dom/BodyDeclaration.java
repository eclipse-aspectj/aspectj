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
 * Abstract base class of all AST nodes that represent body declarations 
 * that may appear in the body of a class or interface declaration.
 * <p>
 * <pre>
 * ClassBodyDeclaration:
 *		ClassDeclaration
 *		InterfaceDeclaration
 *		MethodDeclaration
 * 		ConstructorDeclaration
 * 		FieldDeclaration
 * 		Initializer
 * InterfaceBodyDeclaration:
 *		ClassDeclaration
 *		InterfaceDeclaration
 *		MethodDeclaration
 * 		FieldDeclaration
 * </pre>
 * </p>
 * <p>
 * Most types of body declarations can carry a Javadoc comment; Initializer
 * is the only ones that does not. The source range for body declarations
 * always includes the Javadoc comment if present.
 * </p>
 * 
 * @since 2.0
 */
public abstract class BodyDeclaration extends ASTNode {
	
	/**
	 * The Javadoc comment, or <code>null</code> if none.
	 * Defaults to none.
	 */
	private Javadoc optionalJavadoc = null;

	/**
	 * Creates a new AST node for a body declaration node owned by the 
	 * given AST.
	 * <p>
	 * N.B. This constructor is package-private.
	 * </p>
	 * 
	 * @param ast the AST that is to own this node
	 */
	BodyDeclaration(AST ast) {
		super(ast);
	}
	
	/**
	 * Returns the Javadoc comment node.
	 * 
	 * @return the javadoc comment node, or <code>null</code> if none
	 */
	public Javadoc getJavadoc() {
		return optionalJavadoc;
	}

	/**
	 * Sets or clears the Javadoc comment node.
	 * 
	 * @param javadoc the javadoc comment node, or <code>null</code> if none
	 * @exception IllegalArgumentException if the Java comment string is invalid
	 */
	public void setJavadoc(Javadoc javadoc) {
		replaceChild(this.optionalJavadoc, javadoc, false);
		this.optionalJavadoc = javadoc;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int memSize() {
		return BASE_NODE_SIZE + 1 * 4;
	}
}

