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
 * A method binding represents a method or constructor of a class or interface.
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 *
 * @see ITypeBinding#getDeclaredMethods
 * @since 2.0
 */
public interface IMethodBinding extends IBinding {
	
	/**
	 * Returns whether this binding is for a constructor or a method.
	 * 
	 * @return <code>true</code> if this is the binding for a constructor,
	 *    and <code>false</code> if this is the binding for a method
	 */ 
	public boolean isConstructor();
	
	/**
	 * Returns the name of the method declared in this binding. The method name
	 * is always a simple identifier. The name of a constructor is always the
	 * same as the declared name of its declaring class.
	 * 
	 * @return the name of this method, or the declared name of this
	 *   constructor's declaring class
	 */
	public String getName();
	
	/**
	 * Returns the type binding representing the class or interface
	 * that declares this method or constructor.
	 * 
	 * @return the binding of the class or interface that declares this method
	 *    or constructor
	 */
	public ITypeBinding getDeclaringClass();

	/**
	 * Returns a list of type bindings representing the formal parameter types,
	 * in declaration order, of this method or constructor. Returns an array of
	 * length 0 if this method or constructor does not takes any parameters.
	 * <p>
	 * Note: The result does not include synthetic parameters introduced by
	 * inner class emulation.
	 * </p>
	 * 
	 * @return a (possibly empty) list of type bindings for the formal
	 *   parameters of this method or constructor
	 */
	public ITypeBinding[] getParameterTypes();

	/**
	 * Returns the binding for the return type of this method. Returns the
	 * special primitive <code>void</code> return type for constructors.
	 * 
	 * @return the binding for the return type of this method, or the
	 *    <code>void</code> return type for constructors
	 */
	public ITypeBinding getReturnType();

	/**
	 * Returns a list of type bindings representing the types of the exceptions thrown
	 * by this method or constructor. Returns an array of length 0 if this method
	 * throws no exceptions. The resulting types are in no particular order.
	 * 
	 * @return a list of type bindings for exceptions
	 *   thrown by this method or constructor
	 */
	public ITypeBinding[] getExceptionTypes();
}