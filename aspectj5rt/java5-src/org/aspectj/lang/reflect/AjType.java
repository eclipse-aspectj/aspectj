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

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Set;

/**
 * The runtime representation of a type (Aspect, Class, Interface, Annotation, Enum, or Array) in an AspectJ
 * program.
 */
public interface AjType<T> extends Type {
	
	public String getName();
	
	public Package getPackage();
	
	public Class[] getInterfaces();
		
	public int getModifiers();

	// scope
	
	public AjType getSupertype();

	public Type getGenericSupertype();

	public Method getEnclosingMethod();
	
	public Constructor getEnclosingConstructor();

	public AjType getEnclosingType();
	
	public AjType getDeclaringType();

	public PerClause getPerClause(); 
	
	// annotations
	
	public boolean isAnnotationPresent(Class<? extends Annotation> annotationType);
	
	public <A extends Annotation> A getAnnotation(Class<A> annotationType);
	
	public Annotation[] getAnnotations();
	
	public Annotation[] getDeclaredAnnotations();
	
	// inner types
	
	public AjType[] getAjTypes();
	
	public AjType[] getDeclaredAjTypes();
	
	// constructors
	
	public Constructor getConstructor(Class... parameterTypes) throws NoSuchMethodException;
	
	public Constructor[] getConstructors();
	
	public Constructor getDeclaredConstructor(Class... parameterTypes) throws NoSuchMethodException;
	
	public Constructor[] getDeclaredConstructors();
	
	// fields
	
	public Field getDeclaredField(String name) throws NoSuchFieldException;
	
	public Field[] getDeclaredFields();
	
	public Field getField(String name) throws NoSuchFieldException;
	
	public Field[] getFields();
	
	// methods
	
	public Method getDeclaredMethod(String name, Class... parameterTypes) throws NoSuchMethodException;
	
	public Method getMethod(String name, Class... parameterTypes) throws NoSuchMethodException;
	
	public Method[] getDeclaredMethods();
	
	public Method[] getMethods();
	
	// pointcuts
	
	public Pointcut getDeclaredPointcut(String name) throws NoSuchPointcutException;
	
	public Pointcut getPointcut(String name) throws NoSuchPointcutException;
	
	public Pointcut[] getDeclaredPointcuts();
	
	public Pointcut[] getPointcuts();
	
	// advice
	
	public Advice[] getDeclaredAdvice(AdviceType... ofTypes);
	
	public Advice[] getAdvice(AdviceType... ofTypes);
	
	public Advice getAdvice(String name) throws NoSuchAdviceException;
	
	public Advice getDeclaredAdvice(String name) throws NoSuchAdviceException;
		
	// inter-type declarations
	
	public InterTypeMethodDeclaration getDeclaredITDMethod(String name, Class target, Class... parameterTypes);
	
	public InterTypeMethodDeclaration[] getDeclaredITDMethods();
	
	public InterTypeMethodDeclaration getITDMethod(String name, Class target, Class... parameterTypes);
	
	public InterTypeMethodDeclaration[] getITDMethods();
		
	public InterTypeConstructorDeclaration getDeclaredITDConstructor(Class target, Class... parameterTypes);
	
	public InterTypeConstructorDeclaration[] getDeclaredITDConstructors();

	public InterTypeConstructorDeclaration getITDConstructor(Class target, Class... parameterTypes);
	
	public InterTypeConstructorDeclaration[] getITDConstructors();
	
	public InterTypeFieldDeclaration getDeclaredITDField(String name, Class target);
	
	public InterTypeFieldDeclaration[] getDeclaredITDFields();
	
	public InterTypeFieldDeclaration getITDField(String name, Class target);
	
	public InterTypeFieldDeclaration[] getITDFields();
		
	// declare statements
	
	public DeclareErrorOrWarning getDeclareErrorOrWarnings();
	
	public DeclareParents getDeclareParents();
	
	public DeclareSoft getDeclareSofts();
	
	public DeclareAnnotation getDeclareAnnotations();
	
	public DeclarePrecedence getDeclarePrecedence();
	
	// misc
	
    public T[] getEnumConstants();
	
	public TypeVariable<Class<T>>[] getTypeParameters();
	
	public boolean isEnum();
	
	public boolean isInstance(Object o);
	
	public boolean isInterface();
	
	public boolean isLocalClass();
	
	public boolean isMemberClass();
	
	public boolean isArray();
	
	public boolean isPrimitive();
	
	public boolean isAspect();
	
	public boolean isMemberAspect();
	
	public boolean isPrivileged();
	
}
