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
 * Variable declaration fragment AST node type, used in field declarations, 
 * local variable declarations, and <code>ForStatement</code> initializers.
 * It contrast to <code>SingleVariableDeclaration</code>, fragments are
 * missing the modifiers and the type; these are located in the fragment's
 * parent node.
 *
 * <pre>
 * VariableDeclarationFragment:
 *    Identifier { <b>[</b><b>]</b> } [ <b>=</b> Expression ]
 * </pre>
 * 
 * @since 2.0
 */
public class VariableDeclarationFragment extends VariableDeclaration {
		
	/**
	 * The variable name; lazily initialized; defaults to an unspecified,
	 * legal Java identifier.
	 */
	private SimpleName variableName = null;

	/**
	 * The number of extra array dimensions that this variable;
	 * defaults to 0.
	 */
	private int extraArrayDimensions = 0;

	/**
	 * The initializer expression, or <code>null</code> if none;
	 * defaults to none.
	 */
	private Expression optionalInitializer = null;
	
	/**
	 * Creates a new AST node for a variable declaration fragment owned by the 
	 * given AST. By default, the variable declaration has: an unspecified 
	 * (but legal) variable name, no initializer, and no extra array dimensions.
	 * <p>
	 * N.B. This constructor is package-private.
	 * </p>
	 * 
	 * @param ast the AST that is to own this node
	 */
	VariableDeclarationFragment(AST ast) {
		super(ast);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	public int getNodeType() {
		return VARIABLE_DECLARATION_FRAGMENT;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	ASTNode clone(AST target) {
		VariableDeclarationFragment result = new VariableDeclarationFragment(target);
		result.setName((SimpleName) getName().clone(target));
		result.setExtraDimensions(getExtraDimensions());
		result.setInitializer(
			(Expression) ASTNode.copySubtree(target, getInitializer()));
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
			acceptChild(visitor, getName());
			acceptChild(visitor, getInitializer());
		}
		visitor.endVisit(this);
	}
	
	/* (omit javadoc for this method)
	 * Method declared on VariableDeclaration.
	 */ 
	public SimpleName getName() {
		if (variableName == null) {
			// lazy initialize - use setter to ensure parent link set too
			setName(new SimpleName(getAST()));
		}
		return variableName;
	}
		
	/* (omit javadoc for this method)
	 * Method declared on VariableDeclaration.
	 */ 
	public void setName(SimpleName variableName) {
		if (variableName == null) {
			throw new IllegalArgumentException();
		}
		replaceChild(this.variableName, variableName, false);
		this.variableName = variableName;
	}

	/**
	 * Returns the number of extra array dimensions this variable has over
	 * and above the type specified in the enclosing declaration.
	 * <p>
	 * For example, in the AST for <code>int[] i, j[], k[][]</code> the 
	 * variable declaration fragments for the variables <code>i</code>,
	 * <code>j</code>, and <code>k</code>, have 0, 1, and 2 extra array
	 * dimensions, respectively.
	 * </p>
	 * 
	 * @return the number of extra array dimensions
	 */ 
	public int getExtraDimensions() {
		return extraArrayDimensions;
	}

	/**
	 * Sets the number of extra array dimensions this variable has over
	 * and above the type specified in the enclosing declaration.
	 * 
	 * @return the number of extra array dimensions
	 * @see Modifier
	 * @exception IllegalArgumentException if the number of dimensions is negative
	 */ 
	public void setExtraDimensions(int dimensions) {
		if (dimensions < 0) {
			throw new IllegalArgumentException();
		}
		modifying();
		this.extraArrayDimensions = dimensions;
	}

	/* (omit javadoc for this method)
	 * Method declared on VariableDeclaration.
	 */ 
	public Expression getInitializer() {
		return optionalInitializer;
	}
	
	/* (omit javadoc for this method)
	 * Method declared on VariableDeclaration.
	 */ 
	public void setInitializer(Expression initializer) {
		// a SingleVariableDeclaration may occur inside an Expression 
		// must check cycles
		replaceChild(this.optionalInitializer, initializer, true);
		this.optionalInitializer = initializer;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int memSize() {
		// treat Operator as free
		return BASE_NODE_SIZE + 3 * 4;
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int treeSize() {
		return 
			memSize()
			+ (variableName == null ? 0 : getName().treeSize())
			+ (optionalInitializer == null ? 0 : getInitializer().treeSize());
	}
}
