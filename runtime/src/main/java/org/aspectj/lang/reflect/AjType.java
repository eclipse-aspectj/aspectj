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
	 * The name of this type, in the same format as returned by Class.getName()
	 */
	public String getName();
	
	/**
	 * The package in which this type is declared
	 */
	public Package getPackage();
	
	/**
	 * The interfaces implemented by this type
	 */
	public AjType<?>[] getInterfaces();
		
	/**
	 * The modifiers declared for this type. The return value can be interpreted
	 * using java.lang.reflect.Modifier
	 */
	public int getModifiers();
	
	/**
	 * The java.lang.Class that corresponds to this AjType
	 */
	public Class<T> getJavaClass();

	// scope
	
	/**
	 * The supertype of this type. If this type represents Object or a primitive type
	 * then null is returned.
	 */
	public AjType<?> getSupertype();

	/**
	 * The generic supertype of this type, as defined by Class.getGenericSupertype
	 */
	public Type getGenericSupertype();

	/**
	 * If this type represents a local or anonymous type declared within a method, return 
	 * then enclosing Method object.
	 */
	public Method getEnclosingMethod();
	
	/**
	 * If this type represents a local or anonymous type declared within a constructor, return 
	 * then enclosing Method object.
	 */
	public Constructor getEnclosingConstructor();

	/**
	 * Returns the immediately enclosing type of this type.
	 */
	public AjType<?> getEnclosingType();
	
	/**
	 * If this type is a member of another type, return the AjType representing the type
	 * in which it was declared.
	 */
	public AjType<?> getDeclaringType();

	/**
	 * If this type represents an aspect, returns the associated per-clause.
	 * Returns null for non-aspect types.
	 */
	public PerClause getPerClause(); 
	
	// inner types
	/**
	 * Returns an array containing all the public types that are members of this type
	 */
	public AjType<?>[] getAjTypes();
	
	/**
	 * Returns an array containing all the types declared by this type
	 */
	public AjType<?>[] getDeclaredAjTypes();
	
	// constructors
	
	/**
	 * Returns the constructor object for the specified public constructor of this type
	 */
	public Constructor getConstructor(AjType<?>... parameterTypes) throws NoSuchMethodException;
	
	/**
	 * Returns all of the public constructors of this type
	 */
	public Constructor[] getConstructors();
	
	/**
	 * Returns the constructor object for the specified constructor of this type
	 */
	public Constructor getDeclaredConstructor(AjType<?>... parameterTypes) throws NoSuchMethodException;

	/**
	 * Returns all the constructors declared in this type
	 */
	public Constructor[] getDeclaredConstructors();
	
	// fields
	
	/**
	 * Return the field declared in this type with the given name
	 */
	public Field getDeclaredField(String name) throws NoSuchFieldException;
	
	/** 
	 * Returns all the fields declared in this type
	 */
	public Field[] getDeclaredFields();
	
	/**
	 * Return the public field with the given name 
	 */
	public Field getField(String name) throws NoSuchFieldException;
	
	/**
	 * Return the public fields declared by this type
	 */
	public Field[] getFields();
	
	// methods

	/**
	 * Return the method object for the specified method declared in this type
	 */
	public Method getDeclaredMethod(String name, AjType<?>... parameterTypes) throws NoSuchMethodException;
	
	/**
	 * Return the method object for the specified public method declared in this type 
	 */
	public Method getMethod(String name, AjType<?>... parameterTypes) throws NoSuchMethodException;
	
	/**
	 * Return all the methods declared by this type
	 */
	public Method[] getDeclaredMethods();
	
	/**
	 * Returns all the public methods of this type
	 */
	public Method[] getMethods();
	
	// pointcuts
	
	/**
	 * Return the pointcut object representing the specified pointcut declared by this type
	 */
	public Pointcut getDeclaredPointcut(String name) throws NoSuchPointcutException;
	
	/**
	 * Return the pointcut object representing the specified public pointcut
	 */
	public Pointcut getPointcut(String name) throws NoSuchPointcutException;

	/**
	 * Returns all of the pointcuts declared by this type
	 */
	public Pointcut[] getDeclaredPointcuts();

	/**
	 * Returns all of the public pointcuts of this type
	 */
	public Pointcut[] getPointcuts();
	
	// advice

	/**
	 * Returns all of the advice declared by this type, of an advice kind contained in the
	 * parameter list.
	 */
	public Advice[] getDeclaredAdvice(AdviceKind... ofTypes);
	
	/**
	 * Returns all of the advice for this type, of an advice kind contained in the parameter
	 * list. 
	 */
	public Advice[] getAdvice(AdviceKind... ofTypes);
	
	/**
	 * Returns the advice with the given name. For an @AspectJ declared advice member,
	 * this is the name of the annotated method. For a code-style advice declaration, this
	 * is the name given in the @AdviceName annotation if present.
	 */
	public Advice getAdvice(String name) throws NoSuchAdviceException;
	
	/**
	 * Returns the advice declared in this type with the given name. For an @AspectJ declared advice member,
	 * this is the name of the annotated method. For a code-style advice declaration, this
	 * is the name given in the @AdviceName annotation if present.
	 */
	public Advice getDeclaredAdvice(String name) throws NoSuchAdviceException;
		
	// inter-type declarations
	
	/**
	 * Return the inter-type method declared by this type matching the given specification
	 */
	public InterTypeMethodDeclaration getDeclaredITDMethod(String name, AjType<?> target, AjType<?>... parameterTypes) throws NoSuchMethodException;
	
	/**
	 * Return all of the inter-type methods declared by this type
	 */
	public InterTypeMethodDeclaration[] getDeclaredITDMethods();

	/**
	 * Return the public inter-type method of this type matching the given specification
	 */
	public InterTypeMethodDeclaration getITDMethod(String name, AjType<?> target, AjType<?>... parameterTypes) throws NoSuchMethodException;
	
	/**
	 * Return all of the public inter-type declared methods of this type
	 */
	public InterTypeMethodDeclaration[] getITDMethods();
		
	/**
	 * Return the inter-type constructor declared by this type matching the given specification
	 */
	public InterTypeConstructorDeclaration getDeclaredITDConstructor(AjType<?> target, AjType<?>... parameterTypes) throws NoSuchMethodException;
	
	/**
	 * Returns all of the inter-type constructors declared by this type
	 */
	public InterTypeConstructorDeclaration[] getDeclaredITDConstructors();

	/**
	 * Return the public inter-type constructor matching the given specification
	 */
	public InterTypeConstructorDeclaration getITDConstructor(AjType<?> target, AjType<?>... parameterTypes) throws NoSuchMethodException;

	/**
	 * Return all of the public inter-type constructors of this type
	 */
	public InterTypeConstructorDeclaration[] getITDConstructors();

	/**
	 * Return the inter-type field declared in this type with the given specification
	 */
	public InterTypeFieldDeclaration getDeclaredITDField(String name, AjType<?> target) throws NoSuchFieldException;

	/**
	 * Return all of the inter-type fields declared in this type
	 */
	public InterTypeFieldDeclaration[] getDeclaredITDFields();

	/**
	 * Return the public inter-type field matching the given specification
	 */
	public InterTypeFieldDeclaration getITDField(String name, AjType<?> target) throws NoSuchFieldException;

	/**
	 * Return all of the public inter-type fields for this type
	 */
	public InterTypeFieldDeclaration[] getITDFields();
		
	// declare statements
	/**
	 * Returns all of the declare error and declare warning members of this type,
	 * including declare error/warning members inherited from super-types
	 */
	public DeclareErrorOrWarning[] getDeclareErrorOrWarnings();
	
	/**
	 * Returns all of the declare parents members of this type, including
	 * declare parent members inherited from super-types
	 */
	public DeclareParents[] getDeclareParents();
	
	/**
	 * Return all of the declare soft members of this type, including declare
	 * soft members inherited from super-types
	 */
	public DeclareSoft[] getDeclareSofts();

	/**
	 * Return all of the declare annotation members of this type, including declare
	 * annotation members inherited from super-types
	 */
	public DeclareAnnotation[] getDeclareAnnotations();
	
	/**
	 * Return all of the declare precedence members of this type, including declare
	 * precedence members inherited from super-types
	 */
	public DeclarePrecedence[] getDeclarePrecedence();
	
	// misc
	
	/**
	 * Returns the elements of this enum class, or null if this type does not represent
	 * an enum type.
	 */
    public T[] getEnumConstants();
	
    /**
     * Returns an array of TypeVariable objects that represent the type variables declared by
     * this type (if any)
     */
	public TypeVariable<Class<T>>[] getTypeParameters();

	/**
	 * True if this is an enum type
	 */
	public boolean isEnum();

	/**
	 * True if the given object is assignment-compatible with an object of the type represented
	 * by this AjType
	 */
	public boolean isInstance(Object o);

	/**
	 * True if this is an interface type
	 */
	public boolean isInterface();

	/**
	 * Returns true if and only if the underlying type is a local class
	 */
	public boolean isLocalClass();
	
	/**
	 * Returns true if and only if the underlying type is a member class
	 */
	public boolean isMemberClass();
	
	/**
	 * Return true if this is an array type
	 */
	public boolean isArray();

	/**
	 * Return true if this object represents a primitive type
	 */
	public boolean isPrimitive();

	/**
	 * Return true if this is an aspect type
	 */
	public boolean isAspect();
	
	/**
	 * Returns true if and only if the underlying type is a member aspect
	 */
	public boolean isMemberAspect();

	/**
	 * Returns true if and only if the underlying type is a privileged aspect
	 */
	public boolean isPrivileged();
	
}
