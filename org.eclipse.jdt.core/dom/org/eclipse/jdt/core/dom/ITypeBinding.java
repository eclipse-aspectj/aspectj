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
 * A type binding represents a class type, interface type, array type, a 
 * primitive type (including the special return type <code>void</code>), or the
 * null type.
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 * 
 * @see ITypeBinding#getDeclaredTypes()
 * @since 2.0
 */
public interface ITypeBinding extends IBinding {
	
	/**
	 * Returns whether this type binding represents a primitive type.
	 * <p>
	 * There are nine predefined type bindings to represent the eight primitive
	 * types and <code>void</code>. These have the same names as the primitive
	 * types that they represent, namely boolean, byte, char, short, int,
	 * long, float, and double, and void.
	 * </p>
	 * <p>
	 * The set of primitive types is mutually exclusive with the sets of
	 * array types, with the sets of class and interface types, and with the null type.
	 * </p>
	 * 
	 * @return <code>true</code> if this type binding is for a primitive type,
	 *   and <code>false</code> otherwise
	 *
	 * @see #isArray()
	 * @see #isClass()
	 * @see #isInterface()
	 */
	public boolean isPrimitive();

	/**
	 * Returns whether this type binding represents the null type.
	 * <p>
	 * The null type is the type of a <code>NullLiteral</code> node.
	 * </p>
	 * <p>
	 * The null type is mutually exclusive with the sets of
	 * array types, with the sets of class and interface types, and 
	 * with the set of primitive types .
	 * </p>
	 * 
	 * @return <code>true</code> if this type binding is for the null type,
	 *   and <code>false</code> otherwise
	 */
	public boolean isNullType();
	
	/**
	 * Returns whether this type binding represents an array type.
	 * <p>
	 * The set of array types is mutually exclusive with the sets of
	 * primitive types and with the sets of class and interface types.
	 * </p>
	 *
	 * @return <code>true</code> if this type binding is for an array type,
	 *   and <code>false</code> otherwise
	 * @see #isClass()
	 * @see #isInterface()
	 * @see #isPrimitive()
	 */
	public boolean isArray();
	
	/**
	 * Returns the binding representing the element type of this array type,
	 * or <code>null</code> if this is not an array type binding. The element
	 * type of an array is never itself an array type.
	 *
	 * @return the element type binding, or <code>null</code> if this is
	 *   not an array type
	 */
	public ITypeBinding getElementType();
	
	/**
	 * Returns the dimensionality of this array type, or <code>0</code> if this
	 * is not an array type binding.
	 *
	 * @return the number of dimension of this array type binding, or 
	 *   <code>0</code> if this is not an array type
	 */
	public int getDimensions();
	
	/**
	 * Returns whether this type binding represents a class type.
	 * <p>
	 * The set of class types is mutually exclusive with the sets of
	 * primive types, array types, interface types, and the null type.
	 * </p>
	 *
	 * @return <code>true</code> if this object represents a class,
	 *    and <code>false</code> otherwise
	 *
	 * @see #isArray()
	 * @see #isInterface()
	 * @see #isPrimitive()
	 */
	public boolean isClass();
	
	/**
	 * Returns whether this type binding represents an interface type.
	 * <p>
	 * The set of interface types is mutually exclusive with the sets of
	 * primive types, array types, class types, and the null type.
	 * </p>
	 *
	 * @return <code>true</code> if this object represents an interface,
	 *    and <code>false</code> otherwise
	 *
	 * @see #isArray()
	 * @see #isClass()
	 * @see #isPrimitive()
	 */
	public boolean isInterface();
	
	/**
	 * Returns the unqualified name of the type represented by this binding.
	 * <p>
	 * For named classes and interfaces, this is the simple name of the type. 
	 * For primitive types, the name is the keyword for the primitive type. For array
	 * types, the name is the unqualified name of the component type followed by "[]".
	 * If this represents an anonymous class, it returns an empty string (note that
	 * it is impossible to have an array type with an anonymous class as element type).
	 * For the null type, it returns "null".
	 * </p>
	 * 
	 * @return the unqualified name of the type represented by this binding, an
	 *    empty string this is an anonymous type, or "null" for the null type
	 */
	public String getName();
			
	/**
	 * Returns the binding for the package in which this class or interface is 
	 * declared.
	 * 
	 * @return the binding for the package in which this class or interface is
	 *   declared, or <code>null</code> if this type binding represents a 
	 *   primitive type, an array type, or the null type.
	 */
	public IPackageBinding getPackage();
	
	/**
	 * Returns the type binding representing the class or interface
	 * that declares this binding.
	 * <p>
	 * The declaring class of a member class or interface is the class or
	 * interface of which it is a member. The declaring class of a local class
	 * or interface (including anonymous classes) is the innermost class or
	 * interface containing the expression or statement in which this type is
	 * declared. Array types, primitive types, the null type, and top-level types 
	 * have no declaring class.
	 * </p>
	 * 
	 * @return the binding of the class or interface that declares this type,
	 *   or <code>null</code> if none
	 */
	public ITypeBinding getDeclaringClass();
	
	/**
	 * Returns the type binding for the superclass of the type represented
	 * by this class binding.
	 * <p>
	 * If this type binding represents any class other than the class
	 * <code>java.lang.Object</code>, then the type binding for the direct
	 * superclass of this class is returned. If this type binding represents
	 * the class <code>java.lang.Object</code>, then <code>null</code> is
	 * returned.
	 * </p>
	 * <p>
	 * If this type binding represents an interface, an array type, a
	 * primitive type, or the null type, then <code>null</code> is returned. 
	 * </p>
	 *
	 * @return the superclass of the class represented by this type binding,
	 *    or <code>null</code> if none
	 */
	public ITypeBinding getSuperclass();
	
	/**
	 * Returns a list of type bindings representing the direct superinterfaces
	 * of the class or interface represented by this type binding. 
	 * <p>
	 * If this type binding represents a class, the return value is an array
	 * containing type bindings representing all interfaces directly implemented
	 * by this class. The number and order of the interface objects in the array
	 * corresponds to the number and order of the interface names in the 
	 * <code>implements</code> clause of the original declaration of this class.
	 * </p>
	 * <p>
	 * If this type binding represents an interface, the array contains 
	 * type bindings representing all interfaces directly extended by this
	 * interface. The number and order of the interface objects in the array 
	 * corresponds to the number and order of the interface names in the 
	 * <code>extends</code> clause of the original declaration of this interface. 
	 * </p>
	 * <p>
	 * If the class implements no interfaces, or the interface extends no 
	 * interfaces, or if this type binding represents an array type, a
	 * primitive type, or the null type, this method returns an array of length 0.
	 * </p>
	 *
	 * @return the list of type bindings for the interfaces extended by this
	 *   class, or interfaces extended by this interface, or otherwise the 
	 *   empty list
	 */
	public ITypeBinding[] getInterfaces();
		
	/**
	 * Returns the compiled modifiers for this class or interface binding. 
	 * The result may not correspond to the modifiers as declared in the
	 * original source, since the compiler may change them (in particular, 
	 * for inner class emulation). The <code>getDeclaredModifiers</code> method
	 * should be used if the original modifiers are needed. 
	 * Returns 0 if this type does not represent a class or interface.
	 * 
	 * @see #getDeclaredModifiers
	 */
	public int getModifiers();
	
	/**
	 * Returns the declared modifiers for this class or interface binding
	 * as specified in the original source declaration of the class or 
	 * interface. The result may not correspond to the modifiers in the compiled
	 * binary, since the compiler may change them (in particular, for inner 
	 * class emulation). The <code>getModifiers</code> method should be used if
	 * the compiled modifiers are needed. Returns -1 if this type does not 
	 * represent a class or interface.
	 *
	 * @return the bit-wise or of <code>Modifier</code> constants
	 * @see #getModifiers
	 * @see Modifier
	 */
	public int getDeclaredModifiers();
	
	/**
	 * Returns whether this type binding represents a top-level class or
	 * interface.
	 * <p>
	 * A top-level type is any class or interface whose declaration does not
	 * occur within the body of another class or interface. The set of top
	 * level types is disjoint from the set of nested types.
	 * </p>
	 *
	 * @return <code>true</code> if this type binding is for a top-level class
	 *   or interface, and <code>false</code> otherwise
	 */
	public boolean isTopLevel();

	/**
	 * Returns whether this type binding represents a nested class or
	 * interface.
	 * <p>
	 * A nested type is any class or interface whose declaration occurs within
	 * the body of another class or interface. The set of nested types is 
	 * disjoint from the set of top-level types. Nested types further subdivide
	 * into member types, local types, and anonymous types.
	 * </p>
	 *
	 * @return <code>true</code> if this type binding is for a nested class
	 *   or interface, and <code>false</code> otherwise
	 */
	public boolean isNested();

	/**
	 * Returns whether this type binding represents a member class or
	 * interface.
	 * <p>
	 * A member type is any class or interface declared as a member of
	 * another class or interface. A member type is a subspecies of nested
	 * type, and mutually exclusive with local types.
	 * </p>
	 *
	 * @return <code>true</code> if this type binding is for a member class
	 *   or interface, and <code>false</code> otherwise
	 */
	public boolean isMember();
	
	/**
	 * Returns whether this type binding represents a local class or
	 * interface.
	 * <p>
	 * A local type is any nested class or interface not declared as a member of
	 * another class or interface. A local type is a subspecies of nested
	 * type, and mutually exclusive with member types. Note that anonymous
	 * classes are a subspecies of local types.
	 * </p>
	 *
	 * @return <code>true</code> if this type binding is for a local class
	 *   or interface, and <code>false</code> otherwise
	 */
	public boolean isLocal();
	
	/**
	 * Returns whether this type binding represents an anonymous class.
	 * <p>
	 * An anonymous class is a subspecies of local class, and therefore mutually
	 * exclusive with member types. Note that anonymous classes have no name 
	 * (<code>getName</code> returns the empty string).
	 * </p>
	 *
	 * @return <code>true</code> if this type binding is for an anonymous class,
	 *   and <code>false</code> otherwise
	 */
	public boolean isAnonymous();

	/**
	 * Returns a list of type bindings representing all the classes
	 * and interfaces declared as members of this class or interface type. 
	 * These include public, protected, default (package-private) access,
	 * and private classes and interfaces declared by the class, but excludes
	 * inherited classes and interfaces. Returns an empty list if the
	 * class declares no classes or interfaces as members, or if this type
	 * binding represents an array type, a primitive type, or the null type.
	 * The resulting bindings are in no particular order.
	 * 
	 * @return the list of type bindings for the member types of this type,
	 *   or the empty list if this type does not have member types
	 */
	public ITypeBinding[] getDeclaredTypes();
	
	/**
	 * Returns a list of bindings representing all the fields declared
	 * as members of this class or interface. These include public, 
	 * protected, default (package-private) access, and private fields declared
	 * by the class, but excludes inherited fields. Synthetic fields may or
	 * may not be included.
	 * Returns an empty list if the class or interface declares no fields,
	 * or if this type binding represents a primitive type or an array type 
	 * (the implicit <code>length</code> field of array types is not considered
	 * to be a declared field). The resulting bindings are in no particular 
	 * order.
	 * 
	 * @return the list of bindings for the field members of this type,
	 *   or the empty list if this type does not have field members or is an
	 *   array type, primitive type, or the null type
	 */
	public IVariableBinding[] getDeclaredFields();
	
	/**
	 * Returns a list of method bindings representing all the methods and 
	 * constructors declared for this class or interface. These include public,
	 * protected, default (package-private) access, and private methods. 
	 * Synthetic methods and constructors may or may not be included. Returns
	 * an empty list if the class or interface declares no methods or 
	 * constructors, or if this type binding represents an array type or a
	 * primitive type. The resulting bindings are in no particular order.
	 * 
	 * @return the list of method bindings for the methods and constructors
	 *   declared by this class or interface, or the empty list if this type does
	 *   not declare any methods or constructors
	 */
	public IMethodBinding[] getDeclaredMethods();
	
	/**
	 * Returns whether this type binding originated in source code.
	 * Returns <code>false</code> for primitive types, the null type, array types,
	 * and classes and interfaces whose information came from a pre-compiled binary
	 * class file.
	 * 
	 * @return <code>true</code> if the type is in source code,
	 *    and <code>false</code> otherwise
	 */
	public boolean isFromSource();
}