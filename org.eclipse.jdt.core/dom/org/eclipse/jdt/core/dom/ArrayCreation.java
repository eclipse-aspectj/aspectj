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

import java.util.List;

/**
 * Array creation expression AST node type.
 *
 * <pre>
 * ArrayCreation:
 *		<b>new</b> PrimitiveType <b>[</b> Expression <b>]</b> { <b>[</b> Expression <b>]</b> } { <b>[</b> <b>]</b> }
 *		<b>new</b> TypeName <b>[</b> Expression ]</b> { <b>[</b> Expression <b>]</b> } { <b>[</b> <b>]</b> }
 *		<b>new</b> PrimitiveType <b>[</b> <b>]</b> { <b>[</b> <b>]</b> } ArrayInitializer
 * 		<b>new</b> TypeName <b>[</b> <b>]</b> { <b>[</b> <b>]</b> } ArrayInitializer
 * </pre>
 * <p>
 * The mapping from Java language syntax to AST nodes is as follows:
 * <ul>
 * <li>the type node is the array type of the creation expression,
 *   with one level of array per set of square brackets,</li>
 * <li>the dimension expressions are collected into the <code>dimensions</code>
 *   list.</li>
 * </ul>
 * </p>
 *
 * @since 2.0
 */
public class ArrayCreation extends Expression {
	
	/**
	 * The array type; lazily initialized; defaults to a unspecified,
	 * legal array type.
	 */
	private ArrayType arrayType = null;

	/**
	 * The list of dimension expressions (element type:
	 * <code>Expression</code>). Defaults to an empty list.
	 */
	private ASTNode.NodeList dimensions =
		new ASTNode.NodeList(true, Expression.class);

	/**
	 * The optional array initializer, or <code>null</code> if none;
	 * defaults to none.
	 */
	private ArrayInitializer optionalInitializer = null;

	/**
	 * Creates a new AST node for an array creation expression owned by the 
	 * given AST. By default, the array type is an unspecified 1-dimensional
	 * array, the list of dimensions is empty, and there is no array
	 * initializer.
	 * 
	 * @param ast the AST that is to own this node
	 */
	ArrayCreation(AST ast) {
		super(ast);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	public int getNodeType() {
		return ARRAY_CREATION;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	ASTNode clone(AST target) {
		ArrayCreation result = new ArrayCreation(target);
		result.setType((ArrayType) getType().clone(target));
		result.dimensions().addAll(ASTNode.copySubtrees(target, dimensions()));
		result.setInitializer(
			(ArrayInitializer) ASTNode.copySubtree(target, getInitializer()));
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
			acceptChild(visitor, getType());
			acceptChildren(visitor, dimensions);
			acceptChild(visitor, getInitializer());
		}
		visitor.endVisit(this);
	}
	
	/**
	 * Returns the array type in this array creation expression.
	 * 
	 * @return the array type
	 */ 
	public ArrayType getType() {
		if (arrayType == null) {
			// lazy initialize - use setter to ensure parent link set too
			setType(getAST().newArrayType(
				getAST().newPrimitiveType(PrimitiveType.INT)));
		}
		return arrayType;
	}

	/**
	 * Sets the array type in this array creation expression.
	 * 
	 * @param type the new array type
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * </ul>
	 */ 
	public void setType(ArrayType type) {
		if (type == null) {
			throw new IllegalArgumentException();
		}
		// an ArrayCreation cannot occur inside a ArrayType - cycles not possible
		replaceChild(this.arrayType, type, false);
		this.arrayType = type;
	}
	
	/**
	 * Returns the live ordered list of dimension expressions in this array
	 * initializer.
	 * 
	 * @return the live list of dimension expressions
	 *    (element type: <code>Expression</code>)
	 */ 
	public List dimensions() {
		return dimensions;
	}
	
	/**
	 * Returns the array initializer of this array creation expression, or 
	 * <code>null</code> if there is none.
	 * 
	 * @return the array initializer node, or <code>null</code> if 
	 *    there is none
	 */ 
	public ArrayInitializer getInitializer() {
		return optionalInitializer;
	}
	
	/**
	 * Sets or clears the array initializer of this array creation expression.
	 * 
	 * @param initializer the array initializer node, or <code>null</code>
	 *    if there is none
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * <li>a cycle in would be created</li>
	 * </ul>
	 */ 
	public void setInitializer(ArrayInitializer initializer) {
		// an ArrayCreation may occur inside an ArrayInitializer
		// must check cycles
		replaceChild(this.optionalInitializer, initializer, true);
		this.optionalInitializer = initializer;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int memSize() {
		return BASE_NODE_SIZE + 3 * 4;
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int treeSize() {
		int size = memSize()
			+ (arrayType == null ? 0 : getType().treeSize())
			+ (optionalInitializer == null ? 0 : getInitializer().treeSize())
			+ dimensions.listSize();
		return size;
	}
}

