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
 * Import declaration AST node type.
 *
 * <pre>
 * ImportDeclaration:
 *    <b>import</b> Name [ <b>.</b> <b>*</b> ] <b>;</b>
 * </pre>
 * 
 * @since 2.0
 */
public class ImportDeclaration extends ASTNode {
	/**
	 * The import name; lazily initialized; defaults to a unspecified,
	 * legal Java identifier.
	 */
	private Name importName = null;

	/**
	 * On demand versus single type import; defaults to single type import.
	 */
	private boolean onDemand = false;

	/**
	 * Creates a new AST node for an import declaration owned by the
	 * given AST. The import declaration initially is a single type
	 * import for an unspecified, but legal, Java type name.
	 * <p>
	 * N.B. This constructor is package-private; all subclasses must be 
	 * declared in the same package; clients are unable to declare 
	 * additional subclasses.
	 * </p>
	 * 
	 * @param ast the AST that is to own this node
	 */
	ImportDeclaration(AST ast) {
		super(ast);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	public int getNodeType() {
		return IMPORT_DECLARATION;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	ASTNode clone(AST target) {
		ImportDeclaration result = new ImportDeclaration(target);
		result.setOnDemand(isOnDemand());
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
	 * Returns the name imported by this declaration.
	 * <p>
	 * For an on-demand import, this is the name of a package. For a 
	 * single-type import, this is the qualified name of a type.
	 * </p>
	 * 
	 * @return the imported name node
	 */ 
	public Name getName()  {
		if (importName == null) {
			// lazy initialize - use setter to ensure parent link set too
			setName(getAST().newQualifiedName(
				new SimpleName(getAST()), new SimpleName(getAST())));
		}
		return importName;
	}
	
	/**
	 * Sets the name of this import declaration to the given name.
	 * <p>
	 * For an on-demand import, this is the name of a package. For a 
	 * single-type import, this is the qualified name of a type.
	 * </p>
	 * 
	 * @param name the new import name
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * </ul>
	 */ 
	public void setName(Name name) {
		if (name == null) {
			throw new IllegalArgumentException();
		}
		replaceChild(this.importName, name, false);
		this.importName = name;
	}
		
	/**
	 * Returns whether this import declaration is an on-demand or a 
	 * single-type import.
	 * 
	 * @return <code>true</code> if this is an on-demand import,
	 *    and <code>false</code> if this is a single type import
	 */ 
	public boolean isOnDemand() {
		return onDemand;
	}
		
	/**
	 * Sets whether this import declaration is an on-demand or a 
	 * single-type import.
	 * 
	 * @param onDemand <code>true</code> if this is an on-demand import,
	 *    and <code>false</code> if this is a single type import
	 */ 
	public void setOnDemand(boolean onDemand) {
		modifying();
		this.onDemand = onDemand;
	}
	
	/**
	 * Resolves and returns the binding for the package or type imported by
	 * this import declaration.
	 * <p>
	 * Note that bindings are generally unavailable unless requested when the
	 * AST is being built.
	 * </p>
	 * 
	 * @return the package binding (for on-demand imports) or type binding
	 *    (for single-type imports), or <code>null</code> if the binding cannot
	 *    be resolved
	 */	
	public IBinding resolveBinding() {
		return getAST().getBindingResolver().resolveImport(this);
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int memSize() {
		return BASE_NODE_SIZE + 2 * 4;
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int treeSize() {
		return
			memSize()
			+ (importName == null ? 0 : getName().treeSize());
	}
}

