/*******************************************************************************
 * Copyright (c) 2002 International Business Machines Corp. and others.
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
 * A binding represents a named entity in the Java language. The world of 
 * bindings provides an integrated picture of the structure of the program as
 * seen from the compiler's point of view. This interface declare protocol
 * common to the various different kinds of named entities in the Java language:
 * packages, types, fields, methods, constructors, and local variables.
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 *
 * @see IPackageBinding
 * @see ITypeBinding
 * @see IVariableBinding
 * @see IMethodBinding
 * @since 2.0
 */
public interface IBinding {
	
	/**
	 * Kind constant (value 1) indicating a package binding.
	 * Bindings of this kind can be safely cast to <code>IPackageBinding</code>.
	 * 
	 * @see #getKind
	 * @see IPackageBinding
	 */
	public static final int PACKAGE = 1;
	
	/**
	 * Kind constant (value 2) indicating a type binding.
	 * Bindings of this kind can be safely cast to <code>ITypeBinding</code>.
	 * 
	 * @see #getKind
	 * @see ITypeBinding
	 */
	public static final int TYPE = 2;
		
	/**
	 * Kind constant (value 3) indicating a field or local variable binding.
	 * Bindings of this kind can be safely cast to <code>IVariableBinding</code>.
	 * 
	 * @see #getKind
	 * @see IVariableBinding
	 */
	public static final int VARIABLE = 3;

	/**
	 * Kind constant (value 4) indicating a method or constructor binding.
	 * Bindings of this kind can be safely cast to <code>IMethodBinding</code>.
	 * 
	 * @see #getKind
	 * @see IMethodBinding
	 */
	public static final int METHOD = 4;
	
	/**
	 * Returns the kind of bindings this is.
	 * 
	 * @return one of the kind constants:
	 * 	<code>PACKAGE</code>,
	 * 	<code>TYPE</code>,
	 * 	<code>VARIABLE</code>,
	 * 	or <code>METHOD</code>.
	 */
	public int getKind();
	
	/**
	 * Returns the name of this binding.
	 * Details of the name are specified with each specific kind of binding.
	 * 
	 * @return the name of this binding
	 */
	public String getName();

	/**
	 * Returns the modifiers for this binding.
	 * <p>
	 * Note that deprecated is not included among the modifiers.
	 * Use <code>isDeprecated</code> to find out whether a binding is deprecated.
	 * </p>
	 * 
	 * @return the bit-wise or of <code>Modifier</code> constants
	 * @see Modifier
	 */ 
	public int getModifiers();

	/**
	 * Return whether this binding is for something that is deprecated.
	 * A deprecated class, interface, field, method, or constructor is one that
	 * is marked with the 'deprecated' tag in its Javadoc comment.
	 *
	 * @return <code>true</code> if this binding is deprecated, and 
	 *    <code>false</code> otherwise
	 */
	public boolean isDeprecated();

	/**
	 * Returns whether this binding is synthetic. A synthetic binding is one that
	 * was made up by the compiler, rather than something declared in the 
	 * source code.
	 * 
	 * @return <code>true</code> if this binding is synthetic, and 
	 *    <code>false</code> otherwise
	 */
	public boolean isSynthetic();
	
	/**
	 * Returns the key for this binding.
	 * <p>
	 * Within a connected cluster of bindings (for example, all bindings 
	 * reachable from a given AST), each binding will have a distinct keys.
	 * The keys are generated in a manner that is predictable and as
	 * stable as possible. This last property makes these keys useful for 
	 * comparing bindings between disconnected clusters of bindings (for example, 
	 * the bindings between the "before" and "after" ASTs of the same
	 * compilation unit).
	 * </p>
	 * <p>
	 * The exact details of how the keys are generated is unspecified.
	 * However, it is a function of the following information:
	 * <ul>
	 * <li>packages - the name of the package (for an unnamed package,
	 *   some internal id)</li>
	 * <li>classes or interfaces - the VM name of the type and the key
	 *   of its package</li>
	 * <li>array types - the key of the component type and number of
	 *   dimensions</li>
	 * <li>primitive types - the name of the primitive type</li>
	 * <li>fields - the name of the field and the key of its declaring
	 *   type</li>
	 * <li>methods - the name of the method, the key of its declaring
	 *   type, and the keys of the parameter types</li>
	 * <li>constructors - the key of its declaring class, and the 
	 *   keys of the parameter types</li>
	 * </ul>
	 * Some bindings, like ones that correspond to declarations occurring
	 * within the body of a method, are problematic because of the lack of
	 * any universally acceptable way of assigning keys that are both
	 * predictable and stable. The keys for bindings to local variables, 
	 * local types, etc. is unspecified, and may be <code>null</code>.
	 * </p>
	 * 
	 * @return the key for this binding, or <code>null</code> if none
	 */
	public String getKey();
	
	/**
	 * There is no special definition of equality for bindings; equality is
	 * simply object identity.  Within the context of a single cluster of
	 * bindings, each binding is represented by a distinct object. However,
	 * between different clusters of bindings, the binding objects may or may
	 * not be different; in these cases, the client should compare bindings
	 * via their binding keys (<code>getKey</code>) if available.
	 * 
	 * @see #getKey
	 */
	public boolean equals(Object obj);
	
	/**
	 * Returns a string representation of this binding suitable for debugging
	 * purposes only.
	 * 
	 * @return a debug string 
	 */
	public String toString();
}