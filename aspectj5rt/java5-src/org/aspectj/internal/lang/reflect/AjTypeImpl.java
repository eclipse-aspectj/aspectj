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
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.aspectj.internal.lang.annotation.ajcDeclareEoW;
import org.aspectj.internal.lang.annotation.ajcPrivileged;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.DeclareError;
import org.aspectj.lang.annotation.DeclareWarning;
import org.aspectj.lang.reflect.Advice;
import org.aspectj.lang.reflect.AdviceType;
import org.aspectj.lang.reflect.AjType;
import org.aspectj.lang.reflect.AjTypeSystem;
import org.aspectj.lang.reflect.DeclareAnnotation;
import org.aspectj.lang.reflect.DeclareErrorOrWarning;
import org.aspectj.lang.reflect.DeclareParents;
import org.aspectj.lang.reflect.DeclarePrecedence;
import org.aspectj.lang.reflect.DeclareSoft;
import org.aspectj.lang.reflect.InterTypeConstructorDeclaration;
import org.aspectj.lang.reflect.InterTypeFieldDeclaration;
import org.aspectj.lang.reflect.InterTypeMethodDeclaration;
import org.aspectj.lang.reflect.NoSuchAdviceException;
import org.aspectj.lang.reflect.NoSuchPointcutException;
import org.aspectj.lang.reflect.PerClause;
import org.aspectj.lang.reflect.PerClauseKind;
import org.aspectj.lang.reflect.Pointcut;


/**
 * @author colyer
 *
 */
public class AjTypeImpl<T> implements AjType {
	
	private static final String ajcMagic = "ajc$";
	
	private Class<T> clazz;
	private Pointcut[] declaredPointcuts = null;
	private Pointcut[] pointcuts = null;
	private Advice[] declaredAdvice = null;
	private Advice[] advice = null;
	
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
		if (isAspect()) {
			Aspect aspectAnn = clazz.getAnnotation(Aspect.class);
			String perClause = aspectAnn.value();
			if (perClause.equals("")) {
				return new PerClauseImpl(PerClauseKind.SINGLETON,"");
			} else if (perClause.startsWith("perthis(")) {
				return new PerClauseImpl(PerClauseKind.PERTHIS,perClause.substring("perthis(".length(),perClause.length() - 1));
			} else if (perClause.startsWith("pertarget(")) {
				return new PerClauseImpl(PerClauseKind.PERTARGET,perClause.substring("pertarget(".length(),perClause.length() - 1));				
			} else if (perClause.startsWith("percflow(")) {
				return new PerClauseImpl(PerClauseKind.PERCFLOW,perClause.substring("percflow(".length(),perClause.length() - 1));								
			} else if (perClause.startsWith("percflowbelow(")) {
				return new PerClauseImpl(PerClauseKind.PERCFLOWBELOW,perClause.substring("percflowbelow(".length(),perClause.length() - 1));
			} else if (perClause.startsWith("pertypewithin")) {
				return new PerClauseImpl(PerClauseKind.PERTYPEWITHIN,perClause.substring("pertypewithin(".length(),perClause.length() - 1));				
			} else {
				throw new IllegalStateException("Per-clause not recognized: " + perClause);
			}
		} else {
			return null;
		}
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
		Field f =  clazz.getDeclaredField(name);
		if (f.getName().startsWith(ajcMagic)) throw new NoSuchFieldException(name);
		return f;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.lang.reflect.AjType#getDeclaredFields()
	 */
	public Field[] getDeclaredFields() {
		Field[] fields = clazz.getDeclaredFields();
		List<Field> filteredFields = new ArrayList<Field>();
		for (Field field : fields) 
			if (!field.getName().startsWith(ajcMagic) 
				&& !field.isAnnotationPresent(DeclareWarning.class)
				&& !field.isAnnotationPresent(DeclareError.class)) {
				filteredFields.add(field);
			}
		Field[] ret = new Field[filteredFields.size()];
		filteredFields.toArray(ret);
		return ret;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.lang.reflect.AjType#getField(java.lang.String)
	 */
	public Field getField(String name)  throws NoSuchFieldException {
		Field f =  clazz.getDeclaredField(name);
		if (f.getName().startsWith(ajcMagic)) throw new NoSuchFieldException(name);
		return f;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.lang.reflect.AjType#getFields()
	 */
	public Field[] getFields() {
		Field[] fields = clazz.getFields();
		List<Field> filteredFields = new ArrayList<Field>();
		for (Field field : fields)
			if (!field.getName().startsWith(ajcMagic) 
					&& !field.isAnnotationPresent(DeclareWarning.class)
					&& !field.isAnnotationPresent(DeclareError.class)) {
					filteredFields.add(field);
				}
		Field[] ret = new Field[filteredFields.size()];
		filteredFields.toArray(ret);
		return ret;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.lang.reflect.AjType#getDeclaredMethod(java.lang.String, java.lang.Class...)
	 */
	public Method getDeclaredMethod(String name, Class... parameterTypes) throws NoSuchMethodException {
		Method m =  clazz.getDeclaredMethod(name,parameterTypes);
		if (!isReallyAMethod(m)) throw new NoSuchMethodException(name);
		return m;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.lang.reflect.AjType#getMethod(java.lang.String, java.lang.Class...)
	 */
	public Method getMethod(String name, Class... parameterTypes) throws NoSuchMethodException {
		Method m =  clazz.getMethod(name,parameterTypes);
		if (!isReallyAMethod(m)) throw new NoSuchMethodException(name);
		return m;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.lang.reflect.AjType#getDeclaredMethods()
	 */
	public Method[] getDeclaredMethods() {
		Method[] methods = clazz.getDeclaredMethods();
		List<Method> filteredMethods = new ArrayList<Method>();
		for (Method method : methods) {
			if (isReallyAMethod(method)) filteredMethods.add(method);
		}
		Method[] ret = new Method[filteredMethods.size()];
		filteredMethods.toArray(ret);
		return ret;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.lang.reflect.AjType#getMethods()
	 */
	public Method[] getMethods() {
		Method[] methods = clazz.getMethods();
		List<Method> filteredMethods = new ArrayList<Method>();
		for (Method method : methods) {
			if (isReallyAMethod(method)) filteredMethods.add(method);
		}
		Method[] ret = new Method[filteredMethods.size()];
		filteredMethods.toArray(ret);
		return ret;
	}

	private boolean isReallyAMethod(Method method) {
		if (method.getName().startsWith(ajcMagic)) return false;
		if (method.isAnnotationPresent(org.aspectj.lang.annotation.Pointcut.class)) return false;
		if (method.isAnnotationPresent(Before.class)) return false;
		if (method.isAnnotationPresent(After.class)) return false;
		if (method.isAnnotationPresent(AfterReturning.class)) return false;
		if (method.isAnnotationPresent(AfterThrowing.class)) return false;
		if (method.isAnnotationPresent(Around.class)) return false;
		return true;
	}
	
	/* (non-Javadoc)
	 * @see org.aspectj.lang.reflect.AjType#getDeclaredPointcut(java.lang.String)
	 */
	public Pointcut getDeclaredPointcut(String name) throws NoSuchPointcutException {
		Pointcut[] pcs = getDeclaredPointcuts();
		for (Pointcut pc : pcs)
			if (pc.getName().equals(name)) return pc;
		throw new NoSuchPointcutException(name);
	}

	/* (non-Javadoc)
	 * @see org.aspectj.lang.reflect.AjType#getPointcut(java.lang.String)
	 */
	public Pointcut getPointcut(String name) throws NoSuchPointcutException {
		Pointcut[] pcs = getDeclaredPointcuts();
		for (Pointcut pc : pcs)
			if (pc.getName().equals(name)) return pc;
		throw new NoSuchPointcutException(name);
	}

	/* (non-Javadoc)
	 * @see org.aspectj.lang.reflect.AjType#getDeclaredPointcuts()
	 */
	public Pointcut[] getDeclaredPointcuts() {
		if (declaredPointcuts != null) return declaredPointcuts;
		List<Pointcut> pointcuts = new ArrayList<Pointcut>();
		Method[] methods = clazz.getDeclaredMethods();
		for (Method method : methods) {
			Pointcut pc = asPointcut(method);
			if (pc != null) pointcuts.add(pc);
		}
		Pointcut[] ret = new Pointcut[pointcuts.size()];
		pointcuts.toArray(ret);
		declaredPointcuts = ret;
		return ret;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.lang.reflect.AjType#getPointcuts()
	 */
	public Pointcut[] getPointcuts() {
		if (pointcuts != null) return pointcuts;
		List<Pointcut> pcuts = new ArrayList<Pointcut>();
		Method[] methods = clazz.getMethods();
		for (Method method : methods) {
			Pointcut pc = asPointcut(method);
			if (pc != null) pcuts.add(pc);
		}
		Pointcut[] ret = new Pointcut[pcuts.size()];
		pcuts.toArray(ret);
		pointcuts  = ret;
		return ret;
	}

	private Pointcut asPointcut(Method method) {
		org.aspectj.lang.annotation.Pointcut pcAnn = method.getAnnotation(org.aspectj.lang.annotation.Pointcut.class);
		if (pcAnn != null) {
			String name = method.getName();
			if (name.startsWith(ajcMagic)) {
				// extract real name
				int nameStart = name.indexOf("$$");
				name = name.substring(nameStart +2,name.length());
				int nextDollar = name.indexOf("$");
				if (nextDollar != -1) name = name.substring(0,nextDollar);
			}
			return new PointcutImpl(name,pcAnn.value(),method,AjTypeSystem.getAjType(method.getDeclaringClass()));
		} else {
			return null;
		}
	}
	
	
	public Advice[] getDeclaredAdvice(AdviceType... ofType) {
		Set<AdviceType> types;
		if (ofType.length == 0) {
			types = EnumSet.allOf(AdviceType.class);
		} else {
			types = EnumSet.noneOf(AdviceType.class);
			types.addAll(Arrays.asList(ofType));
		}
		return getDeclaredAdvice(types);
	}
	
	public Advice[] getAdvice(AdviceType... ofType) {
		Set<AdviceType> types;
		if (ofType.length == 0) {
			types = EnumSet.allOf(AdviceType.class);
		} else {
			types = EnumSet.noneOf(AdviceType.class);
			types.addAll(Arrays.asList(ofType));
		}
		return getAdvice(types);
	}

	/* (non-Javadoc)
	 * @see org.aspectj.lang.reflect.AjType#getDeclaredAdvice(org.aspectj.lang.reflect.AdviceType)
	 */
	private Advice[] getDeclaredAdvice(Set ofAdviceTypes) {
		if (declaredAdvice == null) initDeclaredAdvice();
		List<Advice> adviceList = new ArrayList<Advice>();
		for (Advice a : declaredAdvice) {
			if (ofAdviceTypes.contains(a.getKind())) adviceList.add(a);
		}
		Advice[] ret = new Advice[adviceList.size()];
		adviceList.toArray(ret);
		return ret;
	}

	private void initDeclaredAdvice() {
		Method[] methods = clazz.getDeclaredMethods();
		List<Advice> adviceList = new ArrayList<Advice>();
		for (Method method : methods) {
			Advice advice = asAdvice(method);
			if (advice != null) adviceList.add(advice);
		}
		declaredAdvice = new Advice[adviceList.size()];
		adviceList.toArray(declaredAdvice);
	}

	/* (non-Javadoc)
	 * @see org.aspectj.lang.reflect.AjType#getDeclaredAdvice(org.aspectj.lang.reflect.AdviceType)
	 */
	private Advice[] getAdvice(Set ofAdviceTypes) {
		if (advice == null) initAdvice();
		List<Advice> adviceList = new ArrayList<Advice>();
		for (Advice a : advice) {
			if (ofAdviceTypes.contains(a.getKind())) adviceList.add(a);
		}
		Advice[] ret = new Advice[adviceList.size()];
		adviceList.toArray(ret);
		return ret;
	}

	private void initAdvice() {
		Method[] methods = clazz.getDeclaredMethods();
		List<Advice> adviceList = new ArrayList<Advice>();
		for (Method method : methods) {
			Advice advice = asAdvice(method);
			if (advice != null) adviceList.add(advice);
		}
		advice = new Advice[adviceList.size()];
		adviceList.toArray(advice);
	}


	public Advice getAdvice(String name) throws NoSuchAdviceException {
		if (name.equals("")) throw new IllegalArgumentException("use getAdvice(AdviceType...) instead for un-named advice");
		if (advice == null) initAdvice();
		for (Advice a : advice) {
			if (a.getName().equals(name)) return a;
		}
		throw new NoSuchAdviceException(name);
	}
	
	public Advice getDeclaredAdvice(String name) throws NoSuchAdviceException {
		if (name.equals("")) throw new IllegalArgumentException("use getAdvice(AdviceType...) instead for un-named advice");
		if (declaredAdvice == null) initDeclaredAdvice();
		for (Advice a : declaredAdvice) {
			if (a.getName().equals(name)) return a;
		}
		throw new NoSuchAdviceException(name);
	}
	
	private Advice asAdvice(Method method) {
		if (method.getAnnotations().length == 0) return null;
		Before beforeAnn = method.getAnnotation(Before.class);
		if (beforeAnn != null) return new AdviceImpl(method,beforeAnn.value(),AdviceType.BEFORE);
		After afterAnn = method.getAnnotation(After.class);
		if (afterAnn != null) return new AdviceImpl(method,afterAnn.value(),AdviceType.AFTER);
		AfterReturning afterReturningAnn = method.getAnnotation(AfterReturning.class);
		if (afterReturningAnn != null) {
			String pcExpr = afterReturningAnn.pointcut();
			if (pcExpr.equals("")) pcExpr = afterReturningAnn.value();
			return new AdviceImpl(method,pcExpr,AdviceType.AFTER_RETURNING);
		}
		AfterThrowing afterThrowingAnn = method.getAnnotation(AfterThrowing.class);
		if (afterThrowingAnn != null) {
			String pcExpr = afterThrowingAnn.pointcut();
			if (pcExpr == null) pcExpr = afterThrowingAnn.value();
			return new AdviceImpl(method,pcExpr,AdviceType.AFTER_THROWING);
		}
		Around aroundAnn = method.getAnnotation(Around.class);
		if (aroundAnn != null) return new AdviceImpl(method,aroundAnn.value(),AdviceType.AROUND);
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
	public DeclareErrorOrWarning[] getDeclareErrorOrWarnings() {
		List<DeclareErrorOrWarning> deows = new ArrayList<DeclareErrorOrWarning>();
		for (Field field : clazz.getDeclaredFields()) {
			try {
				if (field.isAnnotationPresent(DeclareWarning.class)) {
					 DeclareWarning dw = field.getAnnotation(DeclareWarning.class);
					 if (Modifier.isPublic(field.getModifiers()) && Modifier.isStatic(field.getModifiers())) {
						 String message = (String) field.get(null);
						 DeclareErrorOrWarningImpl deow = new DeclareErrorOrWarningImpl(dw.value(),message,false);
						 deows.add(deow);
					 } 
				} else if (field.isAnnotationPresent(DeclareError.class)) {
					 DeclareError de = field.getAnnotation(DeclareError.class);
					 if (Modifier.isPublic(field.getModifiers()) && Modifier.isStatic(field.getModifiers())) {
						 String message = (String) field.get(null);
						 DeclareErrorOrWarningImpl deow = new DeclareErrorOrWarningImpl(de.value(),message,true);
						 deows.add(deow);
					 } 				
				}
			} catch (IllegalArgumentException e) {
				// just move on to the next field
			} catch (IllegalAccessException e) {
				// just move on to the next field
			}
		}
		for (Method method : clazz.getDeclaredMethods()) {
			if (method.isAnnotationPresent(ajcDeclareEoW.class)) {
				ajcDeclareEoW deowAnn = method.getAnnotation(ajcDeclareEoW.class);
				DeclareErrorOrWarning deow = new DeclareErrorOrWarningImpl(deowAnn.pointcut(),deowAnn.message(),deowAnn.isError());
				deows.add(deow);
			}
		}
		DeclareErrorOrWarning[] ret = new DeclareErrorOrWarning[deows.size()];
		deows.toArray(ret);
		return ret;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.lang.reflect.AjType#getDeclareParents()
	 */
	public DeclareParents[] getDeclareParents() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.lang.reflect.AjType#getDeclareSofts()
	 */
	public DeclareSoft[] getDeclareSofts() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.lang.reflect.AjType#getDeclareAnnotations()
	 */
	public DeclareAnnotation[] getDeclareAnnotations() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.lang.reflect.AjType#getDeclarePrecedence()
	 */
	public DeclarePrecedence[] getDeclarePrecedence() {
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
		return clazz.getAnnotation(Aspect.class) != null;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.lang.reflect.AjType#isMemberAspect()
	 */
	public boolean isMemberAspect() {
		return clazz.isMemberClass() && isAspect();
	}

	public boolean isPrivileged() {
		return isAspect() && clazz.isAnnotationPresent(ajcPrivileged.class);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof AjTypeImpl)) return false;
		AjTypeImpl other = (AjTypeImpl) obj;
		return other.clazz.equals(clazz);
	}
	
	@Override
	public int hashCode() {
		return clazz.hashCode();
	}

}
