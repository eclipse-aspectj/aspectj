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
 * Abstract base class of all type AST node types. A type node represents a 
 * reference to a primitive type (including void), to a named class or 
 * interface type, or to an array type.
 * <p>
 * <pre>
 * Type:
 *    PrimitiveType
 *    SimpleType
 *    ArrayType
 * PrimitiveType:
 *    <b>byte</b>
 *    <b>short</b>
 *    <b>char</b>
 *    <b>int</b>
 *    <b>long</b>
 *    <b>float</b>
 *    <b>double</b>
 *    <b>boolean</b>
 *    <b>void</b>
 * SimpleType:
 *    TypeName
 * ArrayType:
 *    Type <b>[</b> <b>]</b>
 * </pre>
 * </p>
 * 
 * @since 2.0
 */
public abstract class Type extends ASTNode {
	
	/**
	 * Creates a new AST node for a type owned by the given AST.
	 * <p>
	 * N.B. This constructor is package-private.
	 * </p>
	 * 
	 * @param ast the AST that is to own this node
	 */
	Type(AST ast) {
		super(ast);
	}
	
	/**
	 * Returns whether this type is a primitive type
	 * (<code>PrimitiveType</code>). 
	 * 
	 * @return <code>true</code> if this is a primitive type, and 
	 *    <code>false</code> otherwise
	 */
	public final boolean isPrimitiveType() {
		return (this instanceof PrimitiveType);
	}

	/**
	 * Returns whether this type is a simple type 
	 * (<code>SimpleType</code>).
	 * 
	 * @return <code>true</code> if this is a simple type, and 
	 *    <code>false</code> otherwise
	 */
	public final boolean isSimpleType() {
		return (this instanceof SimpleType);
	}

	/**
	 * Returns whether this type is an array type
	 * (<code>ArrayType</code>).
	 * 
	 * @return <code>true</code> if this is an array type, and 
	 *    <code>false</code> otherwise
	 */
	public final boolean isArrayType() {
		return (this instanceof ArrayType);
	}

	/**
	 * Resolves and returns the binding for this type.
	 * <p>
	 * Note that bindings are generally unavailable unless requested when the
	 * AST is being built.
	 * </p>
	 * 
	 * @return the type binding, or <code>null</code> if the binding cannot be 
	 *    resolved
	 */	
	public final ITypeBinding resolveBinding() {
		return getAST().getBindingResolver().resolveType(this);
	}
	
// JSR-014
//	/**
//	 * Returns whether this type is a parameterized type 
//   * (<code>ParameterizedType</code>).
//	 * 
//	 * @return <code>true</code> if this is a parameterized type, and 
//	 *    <code>false</code> otherwise
//	 */
//	public final boolean isParameterizedType() {
//		return (this instanceof ParameterizedType);
//	}

//	public IBinding resolvedType();
}

//// JSR-014
//public class ParameterizedType extends Type {
//	public ParameterizedType(AST ast) {
//		super(ast);
//	}
//
//	public Type getGenericType();
//	public void setGenericType(Type genericType);
//
//	public NodeList<Type> parameters();
//}

