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
 * Package declaration AST node type.
 *
 * <pre>
 * PackageDeclaration:
 *    <b>package</b> Name <b>;</b>
 * </pre>
 * 
 * @since 2.0
 */
public class PackageDeclaration extends ASTNode {
	/**
	 * The package name; lazily initialized; defaults to a unspecified,
	 * legal Java package identifier.
	 */
	private Name packageName = null;

	/**
	 * Creates a new AST node for a package declaration owned by the
	 * given AST. The package declaration initially has an unspecified,
	 * but legal, Java identifier.
	 * <p>
	 * N.B. This constructor is package-private; all subclasses must be 
	 * declared in the same package; clients are unable to declare 
	 * additional subclasses.
	 * </p>
	 * 
	 * @param ast the AST that is to own this node
	 */
	PackageDeclaration(AST ast) {
		super(ast);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	public int getNodeType() {
		return PACKAGE_DECLARATION;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	ASTNode clone(AST target) {
		PackageDeclaration result = new PackageDeclaration(target);
		result.setName((Name) getName().clone(target));
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
	 * Returns the package name of this package declaration.
	 * 
	 * @return the package name node
	 */ 
	public Name getName() {
		if (packageName == null) {
			// lazy initialize - use setter to ensure parent link set too
			setName(new SimpleName(getAST()));
		}
		return packageName;
	}
	
	/**
	 * Sets the package name of this package declaration to the given name.
	 * 
	 * @param name the new package name
	 * @exception IllegalArgumentException if`:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * </ul>
	 */ 
	public void setName(Name name) {
		if (name == null) {
			throw new IllegalArgumentException();
		}
		replaceChild(this.packageName, name, false);
		this.packageName = name;
	}
	
	/**
	 * Resolves and returns the binding for the package declared in this package
	 * declaration.
	 * <p>
	 * Note that bindings are generally unavailable unless requested when the
	 * AST is being built.
	 * </p>
	 * 
	 * @return the binding, or <code>null</code> if the binding cannot be 
	 *    resolved
	 */	
	public IPackageBinding resolveBinding() {
		return getAST().getBindingResolver().resolvePackage(this);
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
		return
			memSize()
			+ (packageName== null ? 0 : getName().treeSize());
	}
}

