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

import org.aspectj.internal.lang.annotation.ajcDeclareAnnotation;
import org.aspectj.internal.lang.annotation.ajcDeclareEoW;
import org.aspectj.internal.lang.annotation.ajcDeclareParents;
import org.aspectj.internal.lang.annotation.ajcDeclarePrecedence;
import org.aspectj.internal.lang.annotation.ajcDeclareSoft;
import org.aspectj.internal.lang.annotation.ajcITD;
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
import org.aspectj.lang.reflect.AdviceKind;
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
public class AjTypeImpl<T> implements AjType<T> {
	
	private static final String ajcMagic = "ajc$";
	
	private Class<T> clazz;
	private Pointcut[] declaredPointcuts = null;
	private Pointcut[] pointcuts = null;
	private Advice[] declaredAdvice = null;
	private Advice[] advice = null;
	private InterTypeMethodDeclaration[] declaredITDMethods = null;
	private InterTypeMethodDeclaration[] itdMethods = null;
	private InterTypeFieldDeclaration[] declaredITDFields = null;
	private InterTypeFieldDeclaration[] itdFields = null;
	private InterTypeConstructorDeclaration[] itdCons = null;
	private InterTypeConstructorDeclaration[] declaredITDCons = null;
	
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
	public AjType<?>[] getInterfaces() {
		Class<?>[] baseInterfaces = clazz.getInterfaces();
		return toAjTypeArray(baseInterfaces);
	}

	/* (non-Javadoc)
	 * @see org.aspectj.lang.reflect.AjType#getModifiers()
	 */
	public int getModifiers() {
		return clazz.getModifiers();
	}
	
	public Class<T> getJavaClass() {
		return clazz;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.lang.reflect.AjType#getSupertype()
	 */
	public AjType<? super T> getSupertype() {
		Class<? super T> superclass = clazz.getSuperclass();
		return superclass==null ? null : (AjType<? super T>) new AjTypeImpl(superclass);
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
	public AjType<?> getEnclosingType() {
		Class<?> enc = clazz.getEnclosingClass();
		return enc != null ? new AjTypeImpl(enc) : null;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.lang.reflect.AjType#getDeclaringType()
	 */
	public AjType<?> getDeclaringType() {
		Class dec = clazz.getDeclaringClass();
		return dec != null ? new AjTypeImpl(dec) : null;
	}
	
	public PerClause getPerClause() {
		if (isAspect()) {
			Aspect aspectAnn = clazz.getAnnotation(Aspect.class);
			String perClause = aspectAnn.value();
			if (perClause.equals("")) {
				if (getSupertype().isAspect()) {
					return getSupertype().getPerClause();
				} 
				return new PerClauseImpl(PerClauseKind.SINGLETON);
			} else if (perClause.startsWith("perthis(")) {
				return new PointcutBasedPerClauseImpl(PerClauseKind.PERTHIS,perClause.substring("perthis(".length(),perClause.length() - 1));
			} else if (perClause.startsWith("pertarget(")) {
				return new PointcutBasedPerClauseImpl(PerClauseKind.PERTARGET,perClause.substring("pertarget(".length(),perClause.length() - 1));				
			} else if (perClause.startsWith("percflow(")) {
				return new PointcutBasedPerClauseImpl(PerClauseKind.PERCFLOW,perClause.substring("percflow(".length(),perClause.length() - 1));								
			} else if (perClause.startsWith("percflowbelow(")) {
				return new PointcutBasedPerClauseImpl(PerClauseKind.PERCFLOWBELOW,perClause.substring("percflowbelow(".length(),perClause.length() - 1));
			} else if (perClause.startsWith("pertypewithin")) {
				return new TypePatternBasedPerClauseImpl(PerClauseKind.PERTYPEWITHIN,perClause.substring("pertypewithin(".length(),perClause.length() - 1));				
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
	public boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
		return clazz.isAnnotationPresent(annotationType);
	}

	public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
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
	public AjType<?>[] getAjTypes() {
		Class[] classes = clazz.getClasses();
		return toAjTypeArray(classes);
	}

	/* (non-Javadoc)
	 * @see org.aspectj.lang.reflect.AjType#getDeclaredAspects()
	 */
	public AjType<?>[] getDeclaredAjTypes() {
		Class[] classes = clazz.getDeclaredClasses();
		return toAjTypeArray(classes);
	}

	/* (non-Javadoc)
	 * @see org.aspectj.lang.reflect.AjType#getConstructor(java.lang.Class...)
	 */
	public Constructor getConstructor(AjType<?>... parameterTypes) throws NoSuchMethodException {
		return clazz.getConstructor(toClassArray(parameterTypes));
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
	public Constructor getDeclaredConstructor(AjType<?>... parameterTypes) throws NoSuchMethodException {
		return clazz.getDeclaredConstructor(toClassArray(parameterTypes));
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
		List<Field> filteredFields = new ArrayList<>();
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
		Field f =  clazz.getField(name);
		if (f.getName().startsWith(ajcMagic)) throw new NoSuchFieldException(name);
		return f;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.lang.reflect.AjType#getFields()
	 */
	public Field[] getFields() {
		Field[] fields = clazz.getFields();
		List<Field> filteredFields = new ArrayList<>();
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
	public Method getDeclaredMethod(String name, AjType<?>... parameterTypes) throws NoSuchMethodException {
		Method m =  clazz.getDeclaredMethod(name,toClassArray(parameterTypes));
		if (!isReallyAMethod(m)) throw new NoSuchMethodException(name);
		return m;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.lang.reflect.AjType#getMethod(java.lang.String, java.lang.Class...)
	 */
	public Method getMethod(String name, AjType<?>... parameterTypes) throws NoSuchMethodException {
		Method m =  clazz.getMethod(name,toClassArray(parameterTypes));
		if (!isReallyAMethod(m)) throw new NoSuchMethodException(name);
		return m;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.lang.reflect.AjType#getDeclaredMethods()
	 */
	public Method[] getDeclaredMethods() {
		Method[] methods = clazz.getDeclaredMethods();
		List<Method> filteredMethods = new ArrayList<>();
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
		List<Method> filteredMethods = new ArrayList<>();
		for (Method method : methods) {
			if (isReallyAMethod(method)) filteredMethods.add(method);
		}
		Method[] ret = new Method[filteredMethods.size()];
		filteredMethods.toArray(ret);
		return ret;
	}

	private boolean isReallyAMethod(Method method) {
		if (method.getName().startsWith(ajcMagic)) return false;
		if (method.getAnnotations().length==0) return true;
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
		Pointcut[] pcs = getPointcuts();
		for (Pointcut pc : pcs)
			if (pc.getName().equals(name)) return pc;
		throw new NoSuchPointcutException(name);
	}

	/* (non-Javadoc)
	 * @see org.aspectj.lang.reflect.AjType#getDeclaredPointcuts()
	 */
	public Pointcut[] getDeclaredPointcuts() {
		if (declaredPointcuts != null) return declaredPointcuts;
		List<Pointcut> pointcuts = new ArrayList<>();
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
		List<Pointcut> pcuts = new ArrayList<>();
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
			return new PointcutImpl(name,pcAnn.value(),method,AjTypeSystem.getAjType(method.getDeclaringClass()),pcAnn.argNames());
		} else {
			return null;
		}
	}
	
	
	public Advice[] getDeclaredAdvice(AdviceKind... ofType) {
		Set<AdviceKind> types;
		if (ofType.length == 0) {
			types = EnumSet.allOf(AdviceKind.class);
		} else {
			types = EnumSet.noneOf(AdviceKind.class);
			types.addAll(Arrays.asList(ofType));
		}
		return getDeclaredAdvice(types);
	}
	
	public Advice[] getAdvice(AdviceKind... ofType) {
		Set<AdviceKind> types;
		if (ofType.length == 0) {
			types = EnumSet.allOf(AdviceKind.class);
		} else {
			types = EnumSet.noneOf(AdviceKind.class);
			types.addAll(Arrays.asList(ofType));
		}
		return getAdvice(types);
	}

	/* (non-Javadoc)
	 * @see org.aspectj.lang.reflect.AjType#getDeclaredAdvice(org.aspectj.lang.reflect.AdviceType)
	 */
	private Advice[] getDeclaredAdvice(Set ofAdviceTypes) {
		if (declaredAdvice == null) initDeclaredAdvice();
		List<Advice> adviceList = new ArrayList<>();
		for (Advice a : declaredAdvice) {
			if (ofAdviceTypes.contains(a.getKind())) adviceList.add(a);
		}
		Advice[] ret = new Advice[adviceList.size()];
		adviceList.toArray(ret);
		return ret;
	}

	private void initDeclaredAdvice() {
		Method[] methods = clazz.getDeclaredMethods();
		List<Advice> adviceList = new ArrayList<>();
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
		List<Advice> adviceList = new ArrayList<>();
		for (Advice a : advice) {
			if (ofAdviceTypes.contains(a.getKind())) adviceList.add(a);
		}
		Advice[] ret = new Advice[adviceList.size()];
		adviceList.toArray(ret);
		return ret;
	}

	private void initAdvice() {
		Method[] methods = clazz.getMethods();
		List<Advice> adviceList = new ArrayList<>();
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
		if (beforeAnn != null) return new AdviceImpl(method,beforeAnn.value(),AdviceKind.BEFORE);
		After afterAnn = method.getAnnotation(After.class);
		if (afterAnn != null) return new AdviceImpl(method,afterAnn.value(),AdviceKind.AFTER);
		AfterReturning afterReturningAnn = method.getAnnotation(AfterReturning.class);
		if (afterReturningAnn != null) {
			String pcExpr = afterReturningAnn.pointcut();
			if (pcExpr.equals("")) pcExpr = afterReturningAnn.value();
			return new AdviceImpl(method,pcExpr,AdviceKind.AFTER_RETURNING,afterReturningAnn.returning());
		}
		AfterThrowing afterThrowingAnn = method.getAnnotation(AfterThrowing.class);
		if (afterThrowingAnn != null) {
			String pcExpr = afterThrowingAnn.pointcut();
			if (pcExpr == null) pcExpr = afterThrowingAnn.value();
			return new AdviceImpl(method,pcExpr,AdviceKind.AFTER_THROWING,afterThrowingAnn.throwing());
		}
		Around aroundAnn = method.getAnnotation(Around.class);
		if (aroundAnn != null) return new AdviceImpl(method,aroundAnn.value(),AdviceKind.AROUND);
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.aspectj.lang.reflect.AjType#getDeclaredITDMethod(java.lang.String, java.lang.Class, java.lang.Class...)
	 */
	public InterTypeMethodDeclaration getDeclaredITDMethod(String name,
			AjType<?> target, AjType<?>... parameterTypes) throws NoSuchMethodException {
		InterTypeMethodDeclaration[] itdms = getDeclaredITDMethods();
		outer: for (InterTypeMethodDeclaration itdm : itdms) {
			try {
				if (!itdm.getName().equals(name)) continue;
				AjType<?> itdTarget = itdm.getTargetType();
				if (itdTarget.equals(target)) {
					AjType<?>[] ptypes = itdm.getParameterTypes();
					if (ptypes.length == parameterTypes.length) {
						for (int i = 0; i < ptypes.length; i++) {
							if (!ptypes[i].equals(parameterTypes[i]))
								continue outer;
						}
						return itdm;
					}
				}
			} catch (ClassNotFoundException cnf) {
				// just move on to the next one
			}
		}
		throw new NoSuchMethodException(name);
	}

	/* (non-Javadoc)
	 * @see org.aspectj.lang.reflect.AjType#getDeclaredITDMethods()
	 */
	public InterTypeMethodDeclaration[] getDeclaredITDMethods() {
		if (this.declaredITDMethods == null) {
			List<InterTypeMethodDeclaration> itdms = new ArrayList<>();
			Method[] baseMethods = clazz.getDeclaredMethods();
			for (Method m : baseMethods) {
				if (!m.getName().contains("ajc$interMethodDispatch1$")) continue;
				if (m.isAnnotationPresent(ajcITD.class)) {
					ajcITD ann = m.getAnnotation(ajcITD.class);
					InterTypeMethodDeclaration itdm = 
						new InterTypeMethodDeclarationImpl(
								this,ann.targetType(),ann.modifiers(),
								ann.name(),m);
					itdms.add(itdm);
				}				
			}
			addAnnotationStyleITDMethods(itdms,false);
			this.declaredITDMethods = new InterTypeMethodDeclaration[itdms.size()];
			itdms.toArray(this.declaredITDMethods);
		}
		return this.declaredITDMethods;
	}

	public InterTypeMethodDeclaration getITDMethod(String name, AjType<?> target,
			AjType<?>... parameterTypes) 
	throws NoSuchMethodException {
		InterTypeMethodDeclaration[] itdms = getITDMethods();
		outer: for (InterTypeMethodDeclaration itdm : itdms) {
			try {
				if (!itdm.getName().equals(name)) continue;
				AjType<?> itdTarget = itdm.getTargetType();
				if (itdTarget.equals(target)) {
					AjType<?>[] ptypes = itdm.getParameterTypes();
					if (ptypes.length == parameterTypes.length) {
						for (int i = 0; i < ptypes.length; i++) {
							if (!ptypes[i].equals(parameterTypes[i]))
								continue outer;
						}
						return itdm;
					}
				}
			} catch (ClassNotFoundException cnf) {
				// just move on to the next one
			}
		}
		throw new NoSuchMethodException(name);
	}

	public InterTypeMethodDeclaration[] getITDMethods() {
		if (this.itdMethods == null) {
			List<InterTypeMethodDeclaration> itdms = new ArrayList<>();
			Method[] baseMethods = clazz.getDeclaredMethods();
			for (Method m : baseMethods) {
				if (!m.getName().contains("ajc$interMethod$")) continue;
				if (m.isAnnotationPresent(ajcITD.class)) {
					ajcITD ann = m.getAnnotation(ajcITD.class);
					if (!Modifier.isPublic(ann.modifiers())) continue;
					InterTypeMethodDeclaration itdm = 
						new InterTypeMethodDeclarationImpl(
								this,ann.targetType(),ann.modifiers(),
								ann.name(),m);
					itdms.add(itdm);
				}				
			}
			addAnnotationStyleITDMethods(itdms,true);
			this.itdMethods = new InterTypeMethodDeclaration[itdms.size()];
			itdms.toArray(this.itdMethods);
		}
		return this.itdMethods;
	}
	
	private void addAnnotationStyleITDMethods(List<InterTypeMethodDeclaration> toList, boolean publicOnly) {
		if (isAspect()) {
            for (Field f : clazz.getDeclaredFields()) {
                if (!f.getType().isInterface()) continue;
                if (f.isAnnotationPresent(org.aspectj.lang.annotation.DeclareParents.class)) {
                	Class<org.aspectj.lang.annotation.DeclareParents> decPAnnClass = org.aspectj.lang.annotation.DeclareParents.class;
                	org.aspectj.lang.annotation.DeclareParents decPAnn = f.getAnnotation(decPAnnClass);
               	    if (decPAnn.defaultImpl() == decPAnnClass) continue; // doesn't contribute members...
                    for (Method itdM : f.getType().getDeclaredMethods()) {
                        if (!Modifier.isPublic(itdM.getModifiers()) && publicOnly) continue;
                        InterTypeMethodDeclaration itdm = new InterTypeMethodDeclarationImpl(
                                    this, AjTypeSystem.getAjType(f.getType()), itdM,
                                    Modifier.PUBLIC
                        );
                        toList.add(itdm);
                    }
                }
            }
		}
	}

	private void addAnnotationStyleITDFields(List<InterTypeFieldDeclaration> toList, boolean publicOnly) {
        //AV: I think it is meaningless
        //@AJ decp is interface driven ie no field
		return;
	}

	public InterTypeConstructorDeclaration getDeclaredITDConstructor(
			AjType<?> target, AjType<?>... parameterTypes) throws NoSuchMethodException {
		InterTypeConstructorDeclaration[] itdcs = getDeclaredITDConstructors();
		outer: for (InterTypeConstructorDeclaration itdc : itdcs) {
			try {
				AjType<?> itdTarget = itdc.getTargetType();
				if (itdTarget.equals(target)) {
					AjType<?>[] ptypes = itdc.getParameterTypes();
					if (ptypes.length == parameterTypes.length) {
						for (int i = 0; i < ptypes.length; i++) {
							if (!ptypes[i].equals(parameterTypes[i]))
								continue outer;
						}
						return itdc;
					}
				}
			} catch (ClassNotFoundException cnf) {
				// just move on to the next one
			}
		}
		throw new NoSuchMethodException();
	}

	public InterTypeConstructorDeclaration[] getDeclaredITDConstructors() {
		if (this.declaredITDCons == null) {
			List<InterTypeConstructorDeclaration> itdcs = new ArrayList<>();
			Method[] baseMethods = clazz.getDeclaredMethods();
			for (Method m : baseMethods) {
				if (!m.getName().contains("ajc$postInterConstructor")) continue;
				if (m.isAnnotationPresent(ajcITD.class)) {
					ajcITD ann = m.getAnnotation(ajcITD.class);
					InterTypeConstructorDeclaration itdc = 
						new InterTypeConstructorDeclarationImpl(this,ann.targetType(),ann.modifiers(),m);
					itdcs.add(itdc);
				}				
			}
			this.declaredITDCons = new InterTypeConstructorDeclaration[itdcs.size()];
			itdcs.toArray(this.declaredITDCons);
		}
		return this.declaredITDCons;
	}

	public InterTypeConstructorDeclaration getITDConstructor(AjType<?> target,
			AjType<?>... parameterTypes) throws NoSuchMethodException {
		InterTypeConstructorDeclaration[] itdcs = getITDConstructors();
		outer: for (InterTypeConstructorDeclaration itdc : itdcs) {
			try {
				AjType<?> itdTarget = itdc.getTargetType();
				if (itdTarget.equals(target)) {
					AjType<?>[] ptypes = itdc.getParameterTypes();
					if (ptypes.length == parameterTypes.length) {
						for (int i = 0; i < ptypes.length; i++) {
							if (!ptypes[i].equals(parameterTypes[i]))
								continue outer;
						}
						return itdc;
					}
				}
			} catch (ClassNotFoundException cnf) {
				// just move on to the next one
			}
		}
		throw new NoSuchMethodException();
	}

	public InterTypeConstructorDeclaration[] getITDConstructors() {
		if (this.itdCons == null) {
			List<InterTypeConstructorDeclaration> itdcs = new ArrayList<>();
			Method[] baseMethods = clazz.getMethods();
			for (Method m : baseMethods) {
				if (!m.getName().contains("ajc$postInterConstructor")) continue;
				if (m.isAnnotationPresent(ajcITD.class)) {
					ajcITD ann = m.getAnnotation(ajcITD.class);
					if (!Modifier.isPublic(ann.modifiers())) continue;
					InterTypeConstructorDeclaration itdc = 
						new InterTypeConstructorDeclarationImpl(this,ann.targetType(),ann.modifiers(),m);
					itdcs.add(itdc);
				}				
			}
			this.itdCons = new InterTypeConstructorDeclaration[itdcs.size()];
			itdcs.toArray(this.itdCons);
		}
		return this.itdCons;	}

	public InterTypeFieldDeclaration getDeclaredITDField(String name,
			AjType<?> target) throws NoSuchFieldException {
		InterTypeFieldDeclaration[] itdfs = getDeclaredITDFields();
		for (InterTypeFieldDeclaration itdf : itdfs) {
			if (itdf.getName().equals(name)) {
				try {
					AjType<?> itdTarget = itdf.getTargetType();
					if (itdTarget.equals(target)) return itdf;
				} catch (ClassNotFoundException cnfEx) { 
					// move on to next field
				}				
			}
		}
		throw new NoSuchFieldException(name);
	}

	public InterTypeFieldDeclaration[] getDeclaredITDFields() {
		List<InterTypeFieldDeclaration> itdfs = new ArrayList<>();
		if (this.declaredITDFields == null) {
			Method[] baseMethods = clazz.getDeclaredMethods();
			for(Method m : baseMethods) {
				if (m.isAnnotationPresent(ajcITD.class)) {
					if (!m.getName().contains("ajc$interFieldInit")) continue;
					ajcITD ann = m.getAnnotation(ajcITD.class);
					String interFieldInitMethodName = m.getName();
					String interFieldGetDispatchMethodName = 
						interFieldInitMethodName.replace("FieldInit","FieldGetDispatch");
					try {
						Method dispatch = clazz.getDeclaredMethod(interFieldGetDispatchMethodName, m.getParameterTypes());
						InterTypeFieldDeclaration itdf = new InterTypeFieldDeclarationImpl(
								this,ann.targetType(),ann.modifiers(),ann.name(),
								AjTypeSystem.getAjType(dispatch.getReturnType()),
								dispatch.getGenericReturnType());
						itdfs.add(itdf);
					} catch (NoSuchMethodException nsmEx) {
						throw new IllegalStateException("Can't find field get dispatch method for " + m.getName());
					}
				}
			}
			addAnnotationStyleITDFields(itdfs, false);
			this.declaredITDFields = new InterTypeFieldDeclaration[itdfs.size()];
			itdfs.toArray(this.declaredITDFields);
		}
		return this.declaredITDFields;
	}

	public InterTypeFieldDeclaration getITDField(String name, AjType<?> target) 
	throws NoSuchFieldException {
		InterTypeFieldDeclaration[] itdfs = getITDFields();
		for (InterTypeFieldDeclaration itdf : itdfs) {
			if (itdf.getName().equals(name)) {
				try {
					AjType<?> itdTarget = itdf.getTargetType();
					if (itdTarget.equals(target)) return itdf;
				} catch (ClassNotFoundException cnfEx) { 
					// move on to next field
				}				
			}
		}
		throw new NoSuchFieldException(name);
	}

	public InterTypeFieldDeclaration[] getITDFields() {
		List<InterTypeFieldDeclaration> itdfs = new ArrayList<>();
		if (this.itdFields == null) {
			Method[] baseMethods = clazz.getMethods();
			for(Method m : baseMethods) {
				if (m.isAnnotationPresent(ajcITD.class)) {
					ajcITD ann = m.getAnnotation(ajcITD.class);
					if (!m.getName().contains("ajc$interFieldInit")) continue;
					if (!Modifier.isPublic(ann.modifiers())) continue;
					String interFieldInitMethodName = m.getName();
					String interFieldGetDispatchMethodName = 
						interFieldInitMethodName.replace("FieldInit","FieldGetDispatch");
					try {
						Method dispatch = m.getDeclaringClass().getDeclaredMethod(interFieldGetDispatchMethodName, m.getParameterTypes());
						InterTypeFieldDeclaration itdf = new InterTypeFieldDeclarationImpl(
								this,ann.targetType(),ann.modifiers(),ann.name(),
								AjTypeSystem.getAjType(dispatch.getReturnType()),
								dispatch.getGenericReturnType());
						itdfs.add(itdf);
					} catch (NoSuchMethodException nsmEx) {
						throw new IllegalStateException("Can't find field get dispatch method for " + m.getName());
					}
				}
			}
			addAnnotationStyleITDFields(itdfs, true);
			this.itdFields = new InterTypeFieldDeclaration[itdfs.size()];
			itdfs.toArray(this.itdFields);
		}
		return this.itdFields;
	}

	public DeclareErrorOrWarning[] getDeclareErrorOrWarnings() {
		List<DeclareErrorOrWarning> deows = new ArrayList<>();
		for (Field field : clazz.getDeclaredFields()) {
			try {
				if (field.isAnnotationPresent(DeclareWarning.class)) {
					 DeclareWarning dw = field.getAnnotation(DeclareWarning.class);
					 if (Modifier.isPublic(field.getModifiers()) && Modifier.isStatic(field.getModifiers())) {
						 String message = (String) field.get(null);
						 DeclareErrorOrWarningImpl deow = new DeclareErrorOrWarningImpl(dw.value(),message,false,this);
						 deows.add(deow);
					 } 
				} else if (field.isAnnotationPresent(DeclareError.class)) {
					 DeclareError de = field.getAnnotation(DeclareError.class);
					 if (Modifier.isPublic(field.getModifiers()) && Modifier.isStatic(field.getModifiers())) {
						 String message = (String) field.get(null);
						 DeclareErrorOrWarningImpl deow = new DeclareErrorOrWarningImpl(de.value(),message,true,this);
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
				DeclareErrorOrWarning deow = new DeclareErrorOrWarningImpl(deowAnn.pointcut(),deowAnn.message(),deowAnn.isError(),this);
				deows.add(deow);
			}
		}
		DeclareErrorOrWarning[] ret = new DeclareErrorOrWarning[deows.size()];
		deows.toArray(ret);
		return ret;
	}

	public DeclareParents[] getDeclareParents() {
		List<DeclareParents> decps = new ArrayList<>();
		for (Method method : clazz.getDeclaredMethods()) {
			if (method.isAnnotationPresent(ajcDeclareParents.class)) {
				ajcDeclareParents decPAnn = method.getAnnotation(ajcDeclareParents.class);
				DeclareParentsImpl decp = new DeclareParentsImpl(
						decPAnn.targetTypePattern(),
						decPAnn.parentTypes(),
						decPAnn.isExtends(),
						this
						);
				decps.add(decp);
			}
		}
		addAnnotationStyleDeclareParents(decps);
		if (getSupertype().isAspect()) {
			decps.addAll(Arrays.asList(getSupertype().getDeclareParents()));
		}
		DeclareParents[] ret = new DeclareParents[decps.size()];
		decps.toArray(ret);
		return ret;
	}
	
	private void addAnnotationStyleDeclareParents(List<DeclareParents> toList) {
        for (Field f : clazz.getDeclaredFields()) {
            if (f.isAnnotationPresent(org.aspectj.lang.annotation.DeclareParents.class)) {
                if (!f.getType().isInterface()) continue;
                org.aspectj.lang.annotation.DeclareParents ann = f.getAnnotation(org.aspectj.lang.annotation.DeclareParents.class);
                String parentType = f.getType().getName();
                DeclareParentsImpl decp = new DeclareParentsImpl(
                        ann.value(),
                        parentType,
                        false,
                        this
                );
                toList.add(decp);
            }
        }
	}

	public DeclareSoft[] getDeclareSofts() {
		List<DeclareSoft> decs = new ArrayList<>();
		for (Method method : clazz.getDeclaredMethods()) {
			if (method.isAnnotationPresent(ajcDeclareSoft.class)) {
				ajcDeclareSoft decSAnn = method.getAnnotation(ajcDeclareSoft.class);
				DeclareSoftImpl ds = new DeclareSoftImpl(
						this,
						decSAnn.pointcut(),
						decSAnn.exceptionType()
						);
				decs.add(ds);
			}
		}
		if (getSupertype().isAspect()) {
			decs.addAll(Arrays.asList(getSupertype().getDeclareSofts()));
		}
		DeclareSoft[] ret = new DeclareSoft[decs.size()];
		decs.toArray(ret);
		return ret;
	}

	public DeclareAnnotation[] getDeclareAnnotations() {
		List<DeclareAnnotation> decAs = new ArrayList<>();
		for (Method method : clazz.getDeclaredMethods()) {
			if (method.isAnnotationPresent(ajcDeclareAnnotation.class)) {
				ajcDeclareAnnotation decAnn = method.getAnnotation(ajcDeclareAnnotation.class);
				// the target annotation is on this method...
				Annotation targetAnnotation = null;
				Annotation[] anns = method.getAnnotations();
				for (Annotation ann: anns) {
					if (ann.annotationType() != ajcDeclareAnnotation.class) {
						// this must be the one...
						targetAnnotation = ann;
						break;
					}
				}
				DeclareAnnotationImpl da = new DeclareAnnotationImpl(
						this,
						decAnn.kind(),
						decAnn.pattern(),
						targetAnnotation,
						decAnn.annotation()
						);
				decAs.add(da);
			}
		}
		if (getSupertype().isAspect()) {
			decAs.addAll(Arrays.asList(getSupertype().getDeclareAnnotations()));
		}
		DeclareAnnotation[] ret = new DeclareAnnotation[decAs.size()];
		decAs.toArray(ret);
		return ret;
	}

	public DeclarePrecedence[] getDeclarePrecedence() {
		List<DeclarePrecedence> decps = new ArrayList<>();
		
		// @AspectJ Style
		if (clazz.isAnnotationPresent(org.aspectj.lang.annotation.DeclarePrecedence.class)) {
			org.aspectj.lang.annotation.DeclarePrecedence ann = 
				clazz.getAnnotation(org.aspectj.lang.annotation.DeclarePrecedence.class);
			DeclarePrecedenceImpl decp = new DeclarePrecedenceImpl(
					ann.value(),
					this
					);
			decps.add(decp);
		}
		
		// annotated code-style
		for (Method method : clazz.getDeclaredMethods()) {
			if (method.isAnnotationPresent(ajcDeclarePrecedence.class)) {
				ajcDeclarePrecedence decPAnn = method.getAnnotation(ajcDeclarePrecedence.class);
				DeclarePrecedenceImpl decp = new DeclarePrecedenceImpl(
						decPAnn.value(),
						this
						);
				decps.add(decp);
			}
		}
		if (getSupertype().isAspect()) {
			decps.addAll(Arrays.asList(getSupertype().getDeclarePrecedence()));
		}
		DeclarePrecedence[] ret = new DeclarePrecedence[decps.size()];
		decps.toArray(ret);
		return ret;
	}

	public T[] getEnumConstants() {
		return clazz.getEnumConstants();
	}

	public TypeVariable<Class<T>>[] getTypeParameters() {
		return clazz.getTypeParameters();
	}

	public boolean isEnum() {
		return clazz.isEnum();
	}

	public boolean isInstance(Object o) {
		return clazz.isInstance(o);
	}

	public boolean isInterface() {
		return clazz.isInterface();
	}

	public boolean isLocalClass() {
		return clazz.isLocalClass() && !isAspect();
	}

	public boolean isMemberClass() {
		return clazz.isMemberClass() && !isAspect();
	}

	public boolean isArray() {
		return clazz.isArray();
	}

	public boolean isPrimitive() {
		return clazz.isPrimitive();
	}

	public boolean isAspect() {
		return clazz.getAnnotation(Aspect.class) != null;
	}

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
	
	private AjType<?>[] toAjTypeArray(Class<?>[] classes) {
		AjType<?>[] ajtypes = new AjType<?>[classes.length];
		for (int i = 0; i < ajtypes.length; i++) {
			ajtypes[i] = AjTypeSystem.getAjType(classes[i]);
		}
		return ajtypes;
	}
	
	private Class<?>[] toClassArray(AjType<?>[] ajTypes) {
		Class<?>[] classes = new Class<?>[ajTypes.length];
		for (int i = 0; i < classes.length; i++) {
			classes[i] = ajTypes[i].getJavaClass();
		}
		return classes;
	}
	
	public String toString() { return getName(); }

}
