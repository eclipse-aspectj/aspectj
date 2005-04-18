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
package org.aspectj.internal.lang.reflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

import org.aspectj.lang.reflect.Advice;
import org.aspectj.lang.reflect.AdviceType;
import org.aspectj.lang.reflect.AjType;
import org.aspectj.lang.reflect.DeclareAnnotation;
import org.aspectj.lang.reflect.DeclareErrorOrWarning;
import org.aspectj.lang.reflect.DeclareParents;
import org.aspectj.lang.reflect.DeclarePrecedence;
import org.aspectj.lang.reflect.DeclareSoft;
import org.aspectj.lang.reflect.InterTypeConstructorDeclaration;
import org.aspectj.lang.reflect.InterTypeFieldDeclaration;
import org.aspectj.lang.reflect.InterTypeMethodDeclaration;
import org.aspectj.lang.reflect.PerClause;
import org.aspectj.lang.reflect.Pointcut;

/**
 * @author colyer
 *
 */
public class AjTypeImpl<T> implements AjType {
	
	private Class<T> clazz;
	
	public AjTypeImpl(Class<T> fromClass) {
		this.clazz = fromClass;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.lang.reflect.AjType#getName()
	 */
	public String getName() {
		return clazz.getName();
	}

	/* (non-Javadoc)
	 * @see org.aspectj.lang.reflect.AjType#getPackage()
	 */
	public Package getPackage() {
		return clazz.getPackage();
	}

	/* (non-Javadoc)
	 * @see org.aspectj.lang.reflect.AjType#getInterfaces()
	 */
	public Class[] getInterfaces() {
		return clazz.getInterfaces();
	}

	/* (non-Javadoc)
	 * @see org.aspectj.lang.reflect.AjType#getModifiers()
	 */
	public int getModifiers() {
		return clazz.getModifiers();
	}

	/* (non-Javadoc)
	 * @see org.aspectj.lang.reflect.AjType#getSupertype()
	 */
	public AjType getSupertype() {
		return new AjTypeImpl(clazz.getSuperclass());
	}

	/* (non-Javadoc)
	 * @see org.aspectj.lang.reflect.AjType#getGenericSupertype()
	 */
	public Type getGenericSupertype() {
		return clazz.getGenericSuperclass();
	}

	/* (non-Javadoc)
	 * @see org.aspectj.lang.reflect.AjType#getEnclosingMethod()
	 */
	public Method getEnclosingMethod() {
		return clazz.getEnclosingMethod();
	}

	/* (non-Javadoc)
	 * @see org.aspectj.lang.reflect.AjType#getEnclosingConstructor()
	 */
	public Constructor getEnclosingConstructor() {
		return clazz.getEnclosingConstructor();
	}

	/* (non-Javadoc)
	 * @see org.aspectj.lang.reflect.AjType#getEnclosingType()
	 */
	public AjType getEnclosingType() {
		Class<?> enc = clazz.getEnclosingClass();
		return enc != null ? new AjTypeImpl(enc) : null;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.lang.reflect.AjType#getDeclaringType()
	 */
	public AjType getDeclaringType() {
		Class dec = clazz.getDeclaringClass();
		return dec != null ? new AjTypeImpl(dec) : null;
	}
	
	public PerClause getPerClause() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.lang.reflect.AjType#isAnnotationPresent(java.lang.Class)
	 */
	public boolean isAnnotationPresent(Class annotationType) {
		return clazz.isAnnotationPresent(annotationType);
	}

	public Annotation getAnnotation(Class annotationType) {
		return clazz.getAnnotation(annotationType);
	}
	
	/* (non-Javadoc)
	 * @see org.aspectj.lang.reflect.AjType#getAnnotations()
	 */
	public Annotation[] getAnnotations() {
		return clazz.getAnnotations();
	}

	/* (non-Javadoc)
	 * @see org.aspectj.lang.reflect.AjType#getDeclaredAnnotations()
	 */
	public Annotation[] getDeclaredAnnotations() {
		return clazz.getDeclaredAnnotations();
	}

	/* (non-Javadoc)
	 * @see org.aspectj.lang.reflect.AjType#getAspects()
	 */
	public AjType[] getAjTypes() {
		Class[] classes = clazz.getClasses();
		AjType[] ret = new AjType[classes.length];
		for (int i = 0; i < ret.length; i++) {
			ret[i] = new AjTypeImpl(classes[i]);
		}
		return ret;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.lang.reflect.AjType#getDeclaredAspects()
	 */
	public AjType[] getDeclaredAjTypes() {
		Class[] classes = clazz.getDeclaredClasses();
		AjType[] ret = new AjType[classes.length];
		for (int i = 0; i < ret.length; i++) {
			ret[i] = new AjTypeImpl(classes[i]);
		}
		return ret;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.lang.reflect.AjType#getConstructor(java.lang.Class...)
	 */
	public Constructor getConstructor(Class... parameterTypes) throws NoSuchMethodException {
		return clazz.getConstructor(parameterTypes);
	}

	/* (non-Javadoc)
	 * @see org.aspectj.lang.reflect.AjType#getConstructors()
	 */
	public Constructor[] getConstructors() {
		return clazz.getConstructors();
	}

	/* (non-Javadoc)
	 * @see org.aspectj.lang.reflect.AjType#getDeclaredConstructor(java.lang.Class...)
	 */
	public Constructor getDeclaredConstructor(Class... parameterTypes) throws NoSuchMethodException {
		return clazz.getDeclaredConstructor(parameterTypes);
	}

	/* (non-Javadoc)
	 * @see org.aspectj.lang.reflect.AjType#getDeclaredConstructors()
	 */
	public Constructor[] getDeclaredConstructors() {
		return clazz.getDeclaredConstructors();
	}

	/* (non-Javadoc)
	 * @see org.aspectj.lang.reflect.AjType#getDeclaredField(java.lang.String)
	 */
	public Field getDeclaredField(String name) throws NoSuchFieldException {
		return clazz.getDeclaredField(name);
	}

	/* (non-Javadoc)
	 * @see org.aspectj.lang.reflect.AjType#getDeclaredFields()
	 */
	public Field[] getDeclaredFields() {
		return clazz.getDeclaredFields();
	}

	/* (non-Javadoc)
	 * @see org.aspectj.lang.reflect.AjType#getField(java.lang.String)
	 */
	public Field getField(String name)  throws NoSuchFieldException {
		return clazz.getField(name);
	}

	/* (non-Javadoc)
	 * @see org.aspectj.lang.reflect.AjType#getFields()
	 */
	public Field[] getFields() {
		return clazz.getFields();
	}

	/* (non-Javadoc)
	 * @see org.aspectj.lang.reflect.AjType#getDeclaredMethod(java.lang.String, java.lang.Class...)
	 */
	public Method getDeclaredMethod(String name, Class... parameterTypes) throws NoSuchMethodException {
		return clazz.getDeclaredMethod(name,parameterTypes);
	}

	/* (non-Javadoc)
	 * @see org.aspectj.lang.reflect.AjType#getMethod(java.lang.String, java.lang.Class...)
	 */
	public Method getMethod(String name, Class... parameterTypes) throws NoSuchMethodException {
		return clazz.getMethod(name,parameterTypes);
	}

	/* (non-Javadoc)
	 * @see org.aspectj.lang.reflect.AjType#getDeclaredMethods()
	 */
	public Method[] getDeclaredMethods() {
		return clazz.getDeclaredMethods();
	}

	/* (non-Javadoc)
	 * @see org.aspectj.lang.reflect.AjType#getMethods()
	 */
	public Method[] getMethods() {
		return clazz.getMethods();
	}

	/* (non-Javadoc)
	 * @see org.aspectj.lang.reflect.AjType#getDeclaredPointcut(java.lang.String)
	 */
	public Pointcut getDeclaredPointcut(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.lang.reflect.AjType#getPointcut(java.lang.String)
	 */
	public Pointcut getPointcut(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.lang.reflect.AjType#getDeclaredPointcuts()
	 */
	public Pointcut[] getDeclaredPointcuts() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.lang.reflect.AjType#getPointcuts()
	 */
	public Pointcut[] getPointcuts() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.lang.reflect.AjType#getDeclaredAdvice(org.aspectj.lang.reflect.AdviceType)
	 */
	public Advice[] getDeclaredAdvice(AdviceType adviceType) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.lang.reflect.AjType#getAdvice(org.aspectj.lang.reflect.AdviceType)
	 */
	public Advice[] getAdvice(AdviceType adviceType) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.lang.reflect.AjType#getDeclaredITDMethod(java.lang.String, java.lang.Class, java.lang.Class...)
	 */
	public InterTypeMethodDeclaration getDeclaredITDMethod(String name,
			Class target, Class... parameterTypes) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.lang.reflect.AjType#getDeclaredITDMethods()
	 */
	public InterTypeMethodDeclaration[] getDeclaredITDMethods() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.lang.reflect.AjType#getITDMethod(java.lang.String, java.lang.Class, java.lang.Class...)
	 */
	public InterTypeMethodDeclaration getITDMethod(String name, Class target,
			Class... parameterTypes) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.lang.reflect.AjType#getITDMethods()
	 */
	public InterTypeMethodDeclaration[] getITDMethods() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.lang.reflect.AjType#getDeclaredITDConstructor(java.lang.Class, java.lang.Class...)
	 */
	public InterTypeConstructorDeclaration getDeclaredITDConstructor(
			Class target, Class... parameterTypes) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.lang.reflect.AjType#getDeclaredITDConstructors()
	 */
	public InterTypeConstructorDeclaration[] getDeclaredITDConstructors() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.lang.reflect.AjType#getITDConstructor(java.lang.Class, java.lang.Class...)
	 */
	public InterTypeConstructorDeclaration getITDConstructor(Class target,
			Class... parameterTypes) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.lang.reflect.AjType#getITDConstructors()
	 */
	public InterTypeConstructorDeclaration[] getITDConstructors() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.lang.reflect.AjType#getDeclaredITDField(java.lang.String, java.lang.Class)
	 */
	public InterTypeFieldDeclaration getDeclaredITDField(String name,
			Class target) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.lang.reflect.AjType#getDeclaredITDFields()
	 */
	public InterTypeFieldDeclaration[] getDeclaredITDFields() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.lang.reflect.AjType#getITDField(java.lang.String, java.lang.Class)
	 */
	public InterTypeFieldDeclaration getITDField(String name, Class target) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.lang.reflect.AjType#getITDFields()
	 */
	public InterTypeFieldDeclaration[] getITDFields() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.lang.reflect.AjType#getDeclareErrorOrWarnings()
	 */
	public DeclareErrorOrWarning getDeclareErrorOrWarnings() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.lang.reflect.AjType#getDeclareParents()
	 */
	public DeclareParents getDeclareParents() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.lang.reflect.AjType#getDeclareSofts()
	 */
	public DeclareSoft getDeclareSofts() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.lang.reflect.AjType#getDeclareAnnotations()
	 */
	public DeclareAnnotation getDeclareAnnotations() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.lang.reflect.AjType#getDeclarePrecedence()
	 */
	public DeclarePrecedence getDeclarePrecedence() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.lang.reflect.AjType#getEnumConstants()
	 */
	public Object[] getEnumConstants() {
		return clazz.getEnumConstants();
	}

	/* (non-Javadoc)
	 * @see org.aspectj.lang.reflect.AjType#getTypeParameters()
	 */
	public TypeVariable[] getTypeParameters() {
		return clazz.getTypeParameters();
	}

	/* (non-Javadoc)
	 * @see org.aspectj.lang.reflect.AjType#isEnum()
	 */
	public boolean isEnum() {
		return clazz.isEnum();
	}

	/* (non-Javadoc)
	 * @see org.aspectj.lang.reflect.AjType#isInstance(java.lang.Object)
	 */
	public boolean isInstance(Object o) {
		return clazz.isInstance(o);
	}

	/* (non-Javadoc)
	 * @see org.aspectj.lang.reflect.AjType#isInterface()
	 */
	public boolean isInterface() {
		return clazz.isInterface();
	}

	/* (non-Javadoc)
	 * @see org.aspectj.lang.reflect.AjType#isLocalClass()
	 */
	public boolean isLocalClass() {
		return clazz.isLocalClass() && !isAspect();
	}

	/* (non-Javadoc)
	 * @see org.aspectj.lang.reflect.AjType#isMemberClass()
	 */
	public boolean isMemberClass() {
		return clazz.isMemberClass() && !isAspect();
	}

	/* (non-Javadoc)
	 * @see org.aspectj.lang.reflect.AjType#isArray()
	 */
	public boolean isArray() {
		return clazz.isArray();
	}

	/* (non-Javadoc)
	 * @see org.aspectj.lang.reflect.AjType#isPrimitive()
	 */
	public boolean isPrimitive() {
		return clazz.isPrimitive();
	}

	/* (non-Javadoc)
	 * @see org.aspectj.lang.reflect.AjType#isAspect()
	 */
	public boolean isAspect() {
		// 2 tests we could use today... presence of aspectOf method (but what if user defines one in 
		// a class), and presence of @Aspect annotation (@AspectJ style only). 
		// Is the solution to put the @Aspect annotation on a code-style aspect too during weaving?
		// Or should we generate some private structure with all the reflection info in it, including the aspect
		// info?
		return false;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.lang.reflect.AjType#isMemberAspect()
	 */
	public boolean isMemberAspect() {
		return clazz.isMemberClass() && isAspect();
	}

}
