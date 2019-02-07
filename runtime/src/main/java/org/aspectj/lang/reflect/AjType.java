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
	public String getName();
	
	/**
	 * @return the package in which this type is declared
	 */
	public Package getPackage();
	
	/**
	 * @return the interfaces implemented by this type
	 */
	public AjType<?>[] getInterfaces();
		
	/**
	 * @return the modifiers declared for this type. The return value can be interpreted
	 * using java.lang.reflect.Modifier
	 */
	public int getModifiers();
	
	/**
	 * @return the java.lang.Class that corresponds to this AjType
	 */
	public Class<T> getJavaClass();

	// scope
	
	/**
	 * @return the supertype of this type. If this type represents Object or a primitive type
	 * then null is returned.
	 */
	public AjType<?> getSupertype();

	/**
	 * @return the generic supertype of this type, as defined by Class.getGenericSupertype
	 */
	public Type getGenericSupertype();

	/**
	 * @return the enclosing Method if this type represents a local or anonymous type declared within a method
	 */
	public Method getEnclosingMethod();
	
	/**
	 * @return the enclosing Method if this type represents a local or anonymous type declared within a constructor
	 */
	public Constructor getEnclosingConstructor();

	/**
	 * @return the immediately enclosing type of this type.
	 */
	public AjType<?> getEnclosingType();
	
	/**
	 * @return the AjType representing the typei n which it was declared (if this type is a member of another type)
	 */
	public AjType<?> getDeclaringType();

	/**
	 * @return the per-clause if this is an aspect, otherwise null
	 */
	public PerClause getPerClause(); 
	
	// inner types
	/**
	 * @return an array containing all the public types that are members of this type
	 */
	public AjType<?>[] getAjTypes();
	
	/**
	 * @return an array containing all the types declared by this type
	 */
	public AjType<?>[] getDeclaredAjTypes();
	
	// constructors
	
	/**
	 * @param parameterTypes the types of the constructor parameters
	 * @return the constructor object for the specified public constructor of this type
	 * @throws NoSuchMethodException if constructor not found
	 */
	public Constructor getConstructor(AjType<?>... parameterTypes) throws NoSuchMethodException;
	
	/**
	 * @return all of the public constructors of this type
	 */
	public Constructor[] getConstructors();
	
	/**
	 * @param parameterTypes the types of the constructor parameters
	 * @return the constructor object for the specified constructor of this type
	 * @throws NoSuchMethodException if constructor not found
	 */
	public Constructor getDeclaredConstructor(AjType<?>... parameterTypes) throws NoSuchMethodException;

	/**
	 * @return all the constructors declared in this type
	 */
	public Constructor[] getDeclaredConstructors();
	
	// fields
	
	/**
	 * @param name the field name
	 * @return the declared field
	 * @throws NoSuchFieldException if no field of that name is found
	 */
	public Field getDeclaredField(String name) throws NoSuchFieldException;
	
	/** 
	 * @return all the fields declared in this type
	 */
	public Field[] getDeclaredFields();
	
	/**
	 * @param name the field name
	 * @return the public field with the given name 
	 * @throws NoSuchFieldException if field not found
	 */
	public Field getField(String name) throws NoSuchFieldException;
	
	/**
	 * @return the public fields declared by this type
	 */
	public Field[] getFields();
	
	// methods

	/**
	 * @param name the method name
	 * @param parameterTypes the types of the method parameters
	 * @return the method object for the specified method declared in this type
	 * @throws NoSuchMethodException if the method cannot be found
	 */
	public Method getDeclaredMethod(String name, AjType<?>... parameterTypes) throws NoSuchMethodException;
	
	/**
	 * @param name the method name
	 * @param parameterTypes the types of the method parameters
	 * @return the method object for the specified public method declared in this type 
	 * @throws NoSuchMethodException if the method cannot be found
	 */
	public Method getMethod(String name, AjType<?>... parameterTypes) throws NoSuchMethodException;
	
	/**
	 * @return all the methods declared by this type
	 */
	public Method[] getDeclaredMethods();
	
	/**
	 * @return all the public methods of this type
	 */
	public Method[] getMethods();
	
	// pointcuts
	
	/**
	 * @param name the pointcut name
	 * @return the pointcut object representing the specified pointcut declared by this type
	 * @throws NoSuchPointcutException if no pointcut of that name can be found
	 */
	public Pointcut getDeclaredPointcut(String name) throws NoSuchPointcutException;
	
	/**
	 * @param name the pointcut name
	 * @return the pointcut object representing the specified public pointcut
	 * @throws NoSuchPointcutException if no pointcut of that name can be found
	 */
	public Pointcut getPointcut(String name) throws NoSuchPointcutException;

	/**
	 * @return all of the pointcuts declared by this type
	 */
	public Pointcut[] getDeclaredPointcuts();

	/**
	 * @return all of the public pointcuts of this type
	 */
	public Pointcut[] getPointcuts();
	
	// advice

	/**
	 * @param ofTypes the {@link AdviceKind}s of interest
	 * @return all of the advice declared by this type, of an advice kind contained in the
	 * parameter list.
	 */
	public Advice[] getDeclaredAdvice(AdviceKind... ofTypes);
	
	/**
	 * @param ofTypes the {@link AdviceKind}s of interest
	 * @return all of the advice for this type, of an advice kind contained in the parameter
	 * list. 
	 */
	public Advice[] getAdvice(AdviceKind... ofTypes);
	
	/**
	 * For an annotation style advice member,
	 * this is the name of the annotated method. For a code-style advice declaration, this
	 * is the name given in the @AdviceName annotation if present.
	 * 
	 * @param name the advice name
	 * @return the advice with the given name.
	 * @throws NoSuchAdviceException if no advice can be found with that name
	 */
	public Advice getAdvice(String name) throws NoSuchAdviceException;
	
	/** For an annotation style advice member,
	 * this is the name of the annotated method. For a code-style advice declaration, this
	 * is the name given in the @AdviceName annotation if present.
	 * 
	 * @param name the advice name
	 * @return the advice declared in this type with the given name.
	 * @throws NoSuchAdviceException if no advice can be found with that name
	 */
	public Advice getDeclaredAdvice(String name) throws NoSuchAdviceException;
		
	// inter-type declarations
	
	/**
	 * @param name the method name
	 * @param target the target of the inter-type declaration
	 * @param parameterTypes the types of the inter-type method declaration
	 * @return the inter-type method declared by this type matching the given specification
	 * @throws NoSuchMethodException if the inter-type declaration cannot be found
	 */
	public InterTypeMethodDeclaration getDeclaredITDMethod(String name, AjType<?> target, AjType<?>... parameterTypes) throws NoSuchMethodException;
	
	/**
	 * @return all of the inter-type methods declared by this type
	 */
	public InterTypeMethodDeclaration[] getDeclaredITDMethods();

	/**
	 * @param name the method name
	 * @param target the target of the inter-type declaration
	 * @param parameterTypes the types of the inter-type method declaration
	 * @return the public inter-type method of this type matching the given specification
	 * @throws NoSuchMethodException if the inter-type declaration cannot be found
	 */
	public InterTypeMethodDeclaration getITDMethod(String name, AjType<?> target, AjType<?>... parameterTypes) throws NoSuchMethodException;
	
	/**
	 * @return all of the public inter-type declared methods of this type
	 */
	public InterTypeMethodDeclaration[] getITDMethods();
		
	/**
	 * @param target the target of the inter-type constructor of interest
	 * @param parameterTypes the types of the parameter of the inter-type constructor of interest
	 * @return the inter-type constructor declared by this type matching the given specification
	 * @throws NoSuchMethodException if the inter-type declaration cannot be found
	 */
	public InterTypeConstructorDeclaration getDeclaredITDConstructor(AjType<?> target, AjType<?>... parameterTypes) throws NoSuchMethodException;
	
	/**
	 * @return all of the inter-type constructors declared by this type
	 */
	public InterTypeConstructorDeclaration[] getDeclaredITDConstructors();

	/**
	 * @param target the target of the inter-type constructor of interest
	 * @param parameterTypes the types of the parameter of the inter-type constructor of interest
	 * @return the public inter-type constructor matching the given specification
	 * @throws NoSuchMethodException if the inter-type declaration cannot be found
	 */
	public InterTypeConstructorDeclaration getITDConstructor(AjType<?> target, AjType<?>... parameterTypes) throws NoSuchMethodException;

	/**
	 * @return all of the public inter-type constructors of this type
	 */
	public InterTypeConstructorDeclaration[] getITDConstructors();

	/**
	 * @param name the field name
	 * @param target the target type for the inter-type declaration
	 * @return the inter-type field declared in this type with the given specification
	 * @throws NoSuchFieldException if the inter-type declaration cannot be found
	 */
	public InterTypeFieldDeclaration getDeclaredITDField(String name, AjType<?> target) throws NoSuchFieldException;

	/**
	 * @return all of the inter-type fields declared in this type
	 */
	public InterTypeFieldDeclaration[] getDeclaredITDFields();

	/**
	 * @param name the field name
	 * @param target the target type for the inter-type declaration
	 * @return the public inter-type field matching the given specification
	 * @throws NoSuchFieldException if the inter-type declaration cannot be found
	 */
	public InterTypeFieldDeclaration getITDField(String name, AjType<?> target) throws NoSuchFieldException;

	/**
	 * @return all of the public inter-type fields for this type
	 */
	public InterTypeFieldDeclaration[] getITDFields();
		
	// declare statements
	/**
	 * @return all of the declare error and declare warning members of this type,
	 * including declare error/warning members inherited from super-types
	 */
	public DeclareErrorOrWarning[] getDeclareErrorOrWarnings();
	
	/**
	 * @return all of the declare parents members of this type, including
	 * declare parent members inherited from super-types
	 */
	public DeclareParents[] getDeclareParents();
	
	/**
	 * @return all of the declare soft members of this type, including declare
	 * soft members inherited from super-types
	 */
	public DeclareSoft[] getDeclareSofts();

	/**
	 * @return all of the declare annotation members of this type, including declare
	 * annotation members inherited from super-types
	 */
	public DeclareAnnotation[] getDeclareAnnotations();
	
	/**
	 * @return all of the declare precedence members of this type, including declare
	 * precedence members inherited from super-types
	 */
	public DeclarePrecedence[] getDeclarePrecedence();
	
	// misc
	
	/**
	 * @return the elements of this enum class, or null if this type does not represent
	 * an enum type.
	 */
    public T[] getEnumConstants();
	
    /**
     * @return an array of TypeVariable objects that represent the type variables declared by
     * this type (if any)
     */
	public TypeVariable<Class<T>>[] getTypeParameters();

	/**
	 * @return true if this is an enum type
	 */
	public boolean isEnum();

	/**
	 * @param o the object to check for assignment compatibility
	 * @return true if the given object is assignment-compatible with an object of the type represented
	 * by this AjType
	 */
	public boolean isInstance(Object o);

	/**
	 * @return true if this is an interface type
	 */
	public boolean isInterface();

	/**
	 * @return true if and only if the underlying type is a local class
	 */
	public boolean isLocalClass();
	
	/**
	 * @return true if and only if the underlying type is a member class
	 */
	public boolean isMemberClass();
	
	/**
	 * @return true if this is an array type
	 */
	public boolean isArray();

	/**
	 * @return true if this object represents a primitive type
	 */
	public boolean isPrimitive();

	/**
	 * @return true if this is an aspect type
	 */
	public boolean isAspect();
	
	/**
	 * @return true if and only if the underlying type is a member aspect
	 */
	public boolean isMemberAspect();

	/**
	 * @return true if and only if the underlying type is a privileged aspect
	 */
	public boolean isPrivileged();
	
}
