/* *******************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Adrian Colyer			Initial implementation
 * ******************************************************************/
package org.aspectj.lang.reflect;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

/**
 * The runtime representation of a type (Aspect, Class, Interface, Annotation, Enum, or Array) in an AspectJ
 * program.
 */
public interface AjType<T> extends Type, AnnotatedElement {

	/**
	 * @return the name of this type, in the same format as returned by Class.getName()
	 */
	String getName();

	/**
	 * @return the package in which this type is declared
	 */
	Package getPackage();

	/**
	 * @return the interfaces implemented by this type
	 */
	AjType<?>[] getInterfaces();

	/**
	 * @return the modifiers declared for this type. The return value can be interpreted
	 * using java.lang.reflect.Modifier
	 */
	int getModifiers();

	/**
	 * @return the java.lang.Class that corresponds to this AjType
	 */
	Class<T> getJavaClass();

	// scope

	/**
	 * @return the supertype of this type. If this type represents Object or a primitive type
	 * then null is returned.
	 */
	AjType<?> getSupertype();

	/**
	 * @return the generic supertype of this type, as defined by Class.getGenericSupertype
	 */
	Type getGenericSupertype();

	/**
	 * @return the enclosing Method if this type represents a local or anonymous type declared within a method
	 */
	Method getEnclosingMethod();

	/**
	 * @return the enclosing Method if this type represents a local or anonymous type declared within a constructor
	 */
	Constructor getEnclosingConstructor();

	/**
	 * @return the immediately enclosing type of this type.
	 */
	AjType<?> getEnclosingType();

	/**
	 * @return the AjType representing the typei n which it was declared (if this type is a member of another type)
	 */
	AjType<?> getDeclaringType();

	/**
	 * @return the per-clause if this is an aspect, otherwise null
	 */
	PerClause getPerClause();

	// inner types
	/**
	 * @return an array containing all the public types that are members of this type
	 */
	AjType<?>[] getAjTypes();

	/**
	 * @return an array containing all the types declared by this type
	 */
	AjType<?>[] getDeclaredAjTypes();

	// constructors

	/**
	 * @param parameterTypes the types of the constructor parameters
	 * @return the constructor object for the specified public constructor of this type
	 * @throws NoSuchMethodException if constructor not found
	 */
	Constructor getConstructor(AjType<?>... parameterTypes) throws NoSuchMethodException;

	/**
	 * @return all of the public constructors of this type
	 */
	Constructor[] getConstructors();

	/**
	 * @param parameterTypes the types of the constructor parameters
	 * @return the constructor object for the specified constructor of this type
	 * @throws NoSuchMethodException if constructor not found
	 */
	Constructor getDeclaredConstructor(AjType<?>... parameterTypes) throws NoSuchMethodException;

	/**
	 * @return all the constructors declared in this type
	 */
	Constructor[] getDeclaredConstructors();

	// fields

	/**
	 * @param name the field name
	 * @return the declared field
	 * @throws NoSuchFieldException if no field of that name is found
	 */
	Field getDeclaredField(String name) throws NoSuchFieldException;

	/**
	 * @return all the fields declared in this type
	 */
	Field[] getDeclaredFields();

	/**
	 * @param name the field name
	 * @return the public field with the given name
	 * @throws NoSuchFieldException if field not found
	 */
	Field getField(String name) throws NoSuchFieldException;

	/**
	 * @return the public fields declared by this type
	 */
	Field[] getFields();

	// methods

	/**
	 * @param name the method name
	 * @param parameterTypes the types of the method parameters
	 * @return the method object for the specified method declared in this type
	 * @throws NoSuchMethodException if the method cannot be found
	 */
	Method getDeclaredMethod(String name, AjType<?>... parameterTypes) throws NoSuchMethodException;

	/**
	 * @param name the method name
	 * @param parameterTypes the types of the method parameters
	 * @return the method object for the specified public method declared in this type
	 * @throws NoSuchMethodException if the method cannot be found
	 */
	Method getMethod(String name, AjType<?>... parameterTypes) throws NoSuchMethodException;

	/**
	 * @return all the methods declared by this type
	 */
	Method[] getDeclaredMethods();

	/**
	 * @return all the public methods of this type
	 */
	Method[] getMethods();

	// pointcuts

	/**
	 * @param name the pointcut name
	 * @return the pointcut object representing the specified pointcut declared by this type
	 * @throws NoSuchPointcutException if no pointcut of that name can be found
	 */
	Pointcut getDeclaredPointcut(String name) throws NoSuchPointcutException;

	/**
	 * @param name the pointcut name
	 * @return the pointcut object representing the specified public pointcut
	 * @throws NoSuchPointcutException if no pointcut of that name can be found
	 */
	Pointcut getPointcut(String name) throws NoSuchPointcutException;

	/**
	 * @return all of the pointcuts declared by this type
	 */
	Pointcut[] getDeclaredPointcuts();

	/**
	 * @return all of the public pointcuts of this type
	 */
	Pointcut[] getPointcuts();

	// advice

	/**
	 * @param ofTypes the {@link AdviceKind}s of interest
	 * @return all of the advice declared by this type, of an advice kind contained in the
	 * parameter list.
	 */
	Advice[] getDeclaredAdvice(AdviceKind... ofTypes);

	/**
	 * @param ofTypes the {@link AdviceKind}s of interest
	 * @return all of the advice for this type, of an advice kind contained in the parameter
	 * list.
	 */
	Advice[] getAdvice(AdviceKind... ofTypes);

	/**
	 * For an annotation style advice member,
	 * this is the name of the annotated method. For a code-style advice declaration, this
	 * is the name given in the @AdviceName annotation if present.
	 *
	 * @param name the advice name
	 * @return the advice with the given name.
	 * @throws NoSuchAdviceException if no advice can be found with that name
	 */
	Advice getAdvice(String name) throws NoSuchAdviceException;

	/** For an annotation style advice member,
	 * this is the name of the annotated method. For a code-style advice declaration, this
	 * is the name given in the @AdviceName annotation if present.
	 *
	 * @param name the advice name
	 * @return the advice declared in this type with the given name.
	 * @throws NoSuchAdviceException if no advice can be found with that name
	 */
	Advice getDeclaredAdvice(String name) throws NoSuchAdviceException;

	// inter-type declarations

	/**
	 * @param name the method name
	 * @param target the target of the inter-type declaration
	 * @param parameterTypes the types of the inter-type method declaration
	 * @return the inter-type method declared by this type matching the given specification
	 * @throws NoSuchMethodException if the inter-type declaration cannot be found
	 */
	InterTypeMethodDeclaration getDeclaredITDMethod(String name, AjType<?> target, AjType<?>... parameterTypes) throws NoSuchMethodException;

	/**
	 * @return all of the inter-type methods declared by this type
	 */
	InterTypeMethodDeclaration[] getDeclaredITDMethods();

	/**
	 * @param name the method name
	 * @param target the target of the inter-type declaration
	 * @param parameterTypes the types of the inter-type method declaration
	 * @return the public inter-type method of this type matching the given specification
	 * @throws NoSuchMethodException if the inter-type declaration cannot be found
	 */
	InterTypeMethodDeclaration getITDMethod(String name, AjType<?> target, AjType<?>... parameterTypes) throws NoSuchMethodException;

	/**
	 * @return all of the public inter-type declared methods of this type
	 */
	InterTypeMethodDeclaration[] getITDMethods();

	/**
	 * @param target the target of the inter-type constructor of interest
	 * @param parameterTypes the types of the parameter of the inter-type constructor of interest
	 * @return the inter-type constructor declared by this type matching the given specification
	 * @throws NoSuchMethodException if the inter-type declaration cannot be found
	 */
	InterTypeConstructorDeclaration getDeclaredITDConstructor(AjType<?> target, AjType<?>... parameterTypes) throws NoSuchMethodException;

	/**
	 * @return all of the inter-type constructors declared by this type
	 */
	InterTypeConstructorDeclaration[] getDeclaredITDConstructors();

	/**
	 * @param target the target of the inter-type constructor of interest
	 * @param parameterTypes the types of the parameter of the inter-type constructor of interest
	 * @return the public inter-type constructor matching the given specification
	 * @throws NoSuchMethodException if the inter-type declaration cannot be found
	 */
	InterTypeConstructorDeclaration getITDConstructor(AjType<?> target, AjType<?>... parameterTypes) throws NoSuchMethodException;

	/**
	 * @return all of the public inter-type constructors of this type
	 */
	InterTypeConstructorDeclaration[] getITDConstructors();

	/**
	 * @param name the field name
	 * @param target the target type for the inter-type declaration
	 * @return the inter-type field declared in this type with the given specification
	 * @throws NoSuchFieldException if the inter-type declaration cannot be found
	 */
	InterTypeFieldDeclaration getDeclaredITDField(String name, AjType<?> target) throws NoSuchFieldException;

	/**
	 * @return all of the inter-type fields declared in this type
	 */
	InterTypeFieldDeclaration[] getDeclaredITDFields();

	/**
	 * @param name the field name
	 * @param target the target type for the inter-type declaration
	 * @return the public inter-type field matching the given specification
	 * @throws NoSuchFieldException if the inter-type declaration cannot be found
	 */
	InterTypeFieldDeclaration getITDField(String name, AjType<?> target) throws NoSuchFieldException;

	/**
	 * @return all of the public inter-type fields for this type
	 */
	InterTypeFieldDeclaration[] getITDFields();

	// declare statements
	/**
	 * @return all of the declare error and declare warning members of this type,
	 * including declare error/warning members inherited from super-types
	 */
	DeclareErrorOrWarning[] getDeclareErrorOrWarnings();

	/**
	 * @return all of the declare parents members of this type, including
	 * declare parent members inherited from super-types
	 */
	DeclareParents[] getDeclareParents();

	/**
	 * @return all of the declare soft members of this type, including declare
	 * soft members inherited from super-types
	 */
	DeclareSoft[] getDeclareSofts();

	/**
	 * @return all of the declare annotation members of this type, including declare
	 * annotation members inherited from super-types
	 */
	DeclareAnnotation[] getDeclareAnnotations();

	/**
	 * @return all of the declare precedence members of this type, including declare
	 * precedence members inherited from super-types
	 */
	DeclarePrecedence[] getDeclarePrecedence();

	// misc

	/**
	 * @return the elements of this enum class, or null if this type does not represent
	 * an enum type.
	 */
	T[] getEnumConstants();

    /**
     * @return an array of TypeVariable objects that represent the type variables declared by
     * this type (if any)
     */
	TypeVariable<Class<T>>[] getTypeParameters();

	/**
	 * @return true if this is an enum type
	 */
	boolean isEnum();

	/**
	 * @param o the object to check for assignment compatibility
	 * @return true if the given object is assignment-compatible with an object of the type represented
	 * by this AjType
	 */
	boolean isInstance(Object o);

	/**
	 * @return true if this is an interface type
	 */
	boolean isInterface();

	/**
	 * @return true if and only if the underlying type is a local class
	 */
	boolean isLocalClass();

	/**
	 * @return true if and only if the underlying type is a member class
	 */
	boolean isMemberClass();

	/**
	 * @return true if this is an array type
	 */
	boolean isArray();

	/**
	 * @return true if this object represents a primitive type
	 */
	boolean isPrimitive();

	/**
	 * @return true if this is an aspect type
	 */
	boolean isAspect();

	/**
	 * @return true if and only if the underlying type is a member aspect
	 */
	boolean isMemberAspect();

	/**
	 * @return true if and only if the underlying type is a privileged aspect
	 */
	boolean isPrivileged();

}
