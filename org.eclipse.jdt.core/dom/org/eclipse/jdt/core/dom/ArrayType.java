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
 * Type node for an array type.
 * <p>
 * Array types are expressed in a recursive manner, one dimension at a time.
 * </p>
 * <pre>
 * ArrayType:
 *    Type <b>[</b> <b>]</b>
 * </pre>
 * 
 * @since 2.0
 */
public class ArrayType extends Type {
	/** 
	 * The component type; lazily initialized; defaults to a simple type with
	 * an unspecfied, but legal, name.
	 */
	private Type componentType = null;
	
	/**
	 * Creates a new unparented node for an array type owned by the given AST.
	 * By default, a 1-dimensional array of an unspecified simple type.
	 * <p>
	 * N.B. This constructor is package-private.
	 * </p>
	 * 
	 * @param ast the AST that is to own this node
	 */
	ArrayType(AST ast) {
		super(ast);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	public int getNodeType() {
		return ARRAY_TYPE;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	ASTNode clone(AST target) {
		ArrayType result = new ArrayType(target);
		result.setComponentType((Type) getComponentType().clone(target));
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
			acceptChild(visitor, getComponentType());
		}
		visitor.endVisit(this);
	}
	
	/**
	 * Returns the component type of this array type. The component type
	 * may be another array type.
	 * 
	 * @return the component type node
	 */ 
	public Type getComponentType() {
		if (componentType == null) {
			// lazy initialize - use setter to ensure parent link set too
			setComponentType(new SimpleType(getAST()));
		}
		return componentType;
	}

	/**
	 * Sets the component type of this array type. The component type
	 * may be another array type.
	 * 
	 * @param componentType the component type
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * <li>a cycle in would be created</li>
	 * </ul>
	 */ 
	public void setComponentType(Type componentType) {
		if (componentType == null) {
			throw new IllegalArgumentException();
		}
		// an ArrayType may occur inside an ArrayType - must check cycles
		replaceChild(
			(ASTNode) this.componentType,
			(ASTNode) componentType, true);
		this.componentType = componentType;
	}

	/**
	 * Returns the element type of this array type. The element type is
	 * never an array type.
	 * <p>
	 * This is a convenience method that descends a chain of nested array types
	 * until it reaches a non-array type. 
	 * </p>
	 * 
	 * @return the component type node
	 */ 
	public Type getElementType() {
		Type t = getComponentType();
		while (t.isArrayType()) {
			t = ((ArrayType) t).getComponentType();
		}
		return t;
	}
	
	/**
	 * Returns the number of dimensions in this array type.
	 * <p>
	 * This is a convenience method that descends a chain of nested array types
	 * until it reaches a non-array type. 
	 * </p>
	 * 
	 * @return the number of dimensions (always positive)
	 */ 
	public int getDimensions() {
		Type t = getComponentType();
		int dimensions = 1; // always include this array type
		while (t.isArrayType()) {
			dimensions++;
			t = ((ArrayType) t).getComponentType();
		}
		return dimensions;
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
			+ (componentType == null ? 0 : getComponentType().treeSize());
	}
}

