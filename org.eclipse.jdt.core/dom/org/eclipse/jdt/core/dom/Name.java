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
 * Abstract base class for all AST nodes that represent names.
 * There are exactly two kinds of name: simple ones 
 * (<code>SimpleName</code>) and qualified ones (<code>QualifiedName</code>).
 * <p>
 * <pre>
 * Name:
 *     SimpleName
 *     QualifiedName
 * </pre>
 * </p>
 * 
 * @since 2.0
 */
public abstract class Name extends Expression {
	
	/**
	 * Creates a new AST node for a name owned by the given AST.
	 * <p>
	 * N.B. This constructor is package-private.
	 * </p>
	 * 
	 * @param ast the AST that is to own this node
	 */
	Name(AST ast) {
		super(ast);
	}
	
	/**
	 * Returns whether this name is a simple name
	 * (<code>SimpleName</code>).
	 * 
	 * @return <code>true</code> if this is a simple name, and 
	 *    <code>false</code> otherwise
	 */
	public final boolean isSimpleName() {
		return (this instanceof SimpleName);
	}
		
	/**
	 * Returns whether this name is a qualified name
	 * (<code>QualifiedName</code>).
	 * 
	 * @return <code>true</code> if this is a qualified name, and 
	 *    <code>false</code> otherwise
	 */
	public final boolean isQualifiedName() {
		return (this instanceof QualifiedName);
	}

	/**
	 * Resolves and returns the binding for the entity referred to by this name.
	 * <p>
	 * Note that bindings are generally unavailable unless requested when the
	 * AST is being built.
	 * </p>
	 * 
	 * @return the binding, or <code>null</code> if the binding cannot be 
	 *    resolved
	 */	
	public final IBinding resolveBinding() {
		return getAST().getBindingResolver().resolveName(this);
	}
}
