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
 * AST node for a qualified name. A qualified name is defined recursively
 * as a simple name preceded by a name, which qualifies it. Expressing it this
 * way means that the qualifier and the simple name get their own AST nodes.
 * <pre>
 * QualifiedName:
 *    Name <b>.</b> SimpleName
 * </pre>
 *
 * Range 0: first character of qualified name through the last character
 * of the simple name.
 *
 * @since 2.0
 */
public class QualifiedName extends Name {
	/**
	 * The identifier; lazily initialized; defaults to a unspecified, legal 
	 * Java identifier.
	 */
	private Name qualifier = null;
	
	/**
	 * The name being qualified; lazily initialized; defaults to a unspecified,
	 * legal Java identifier.
	 */
	private SimpleName name = null;
	
	/**
	 * Creates a new AST node for a qualified name owned by the given AST.
	 * <p>
	 * N.B. This constructor is package-private; all subclasses must be 
	 * declared in the same package; clients are unable to declare 
	 * additional subclasses.
	 * </p>
	 * 
	 * @param ast the AST that is to own this node
	 */
	QualifiedName(AST ast) {
		super(ast);
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	public int getNodeType() {
		return QUALIFIED_NAME;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	ASTNode clone(AST target) {
		QualifiedName result = new QualifiedName(target);
		result.setQualifier((Name) getQualifier().clone(target));
		result.setName((SimpleName) getName().clone(target));
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
	 * Returns the qualifier part of this qualified name.
	 * 
	 * @return the qualifier part of this qualified name
	 */ 
	public Name getQualifier() {
		if (qualifier == null) {
			// lazy initialize - use setter to ensure parent link set too
			setQualifier(new SimpleName(getAST()));
		}
		return qualifier;
	}
	
	/**
	 * Sets the qualifier of this qualified name to the given name.
	 * 
	 * @param the qualifier of this qualified name
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * <li>a cycle in would be created</li>
	 * </ul>
	 */ 
	public void setQualifier(Name qualifier) {
		if (qualifier == null) {
			throw new IllegalArgumentException();
		}
		// a QualifiedName may occur inside a QualifiedName - must check cycles
		replaceChild((ASTNode) this.qualifier, (ASTNode) qualifier, true);
		this.qualifier = qualifier;
	}
	
	/**
	 * Returns the name part of this qualified name.
	 * 
	 * @return the name being qualified 
	 */ 
	public SimpleName getName() {
		if (name == null) {
			// lazy initialize - use setter to ensure parent link set too
			setName(new SimpleName(getAST()));
		}
		return name;
	}
	
	/**
	 * Sets the name part of this qualified name to the given simple name.
	 * 
	 * @param name the identifier of this qualified name
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * </ul>
	 */ 
	public void setName(SimpleName name) {
		if (name == null) {
			throw new IllegalArgumentException();
		}
		replaceChild(this.name, name, false);
		this.name = name;
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
			+ (name == null ? 0 : getName().treeSize())
			+ (qualifier == null ? 0 : getQualifier().treeSize());
	}
}

