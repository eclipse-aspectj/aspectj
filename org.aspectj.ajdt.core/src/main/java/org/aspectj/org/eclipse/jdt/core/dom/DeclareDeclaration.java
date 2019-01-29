
/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.aspectj.org.eclipse.jdt.core.dom;


/**
 * DeclareDeclaration DOM AST node.
 *   
 * @author ajh02
 */
public abstract class DeclareDeclaration extends BodyDeclaration {
	
	DeclareDeclaration(AST ast) {
		super(ast);
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	final int getNodeType0() {
		return METHOD_DECLARATION; // ajh02: should add one called DECLARE_DECLARATION or something
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int memSize() {
		return super.memSize() + 9 * 4;
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int treeSize() {
		return
			memSize()
			+ (this.optionalDocComment == null ? 0 : getJavadoc().treeSize());
	}

	/**
	 * Resolves and returns the binding for the method or constructor declared
	 * in this method or constructor declaration.
	 * <p>
	 * Note that bindings are generally unavailable unless requested when the
	 * AST is being built.
	 * </p>
	 * 
	 * @return the binding, or <code>null</code> if the binding cannot be 
	 *    resolved
	 */	
	public IMethodBinding resolveBinding() {
		return null; // ajh02: :-/
		//return this.ast.getBindingResolver().resolveMethod(this);
	}

}