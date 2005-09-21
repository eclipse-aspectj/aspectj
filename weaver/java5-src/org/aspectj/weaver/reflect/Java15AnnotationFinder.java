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
package org.aspectj.weaver.reflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.aspectj.apache.bcel.classfile.JavaClass;
import org.aspectj.apache.bcel.util.Repository;
import org.aspectj.apache.bcel.util.ClassLoaderRepository;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.World;

/**
 * Find the given annotation (if present) on the given object
 *
 */
public class Java15AnnotationFinder implements AnnotationFinder {
	
	private Repository bcelRepository;
	
	public Java15AnnotationFinder() {
		this.bcelRepository = new ClassLoaderRepository(getClass().getClassLoader());
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.reflect.AnnotationFinder#getAnnotation(org.aspectj.weaver.ResolvedType, java.lang.Object)
	 */
	public Object getAnnotation(ResolvedType annotationType, Object onObject) {
		try {
			Class annotationClass = Class.forName(annotationType.getName());
			if (onObject.getClass().isAnnotationPresent(annotationClass)) {
				return onObject.getClass().getAnnotation(annotationClass);
			}
		} catch (ClassNotFoundException ex) {
			// just return null
		}
		return null;
	}

	public Object getAnnotationFromClass(ResolvedType annotationType, Class aClass) {
		try {
			Class annotationClass = Class.forName(annotationType.getName());
			if (aClass.isAnnotationPresent(annotationClass)) {
				return aClass.getAnnotation(annotationClass);
			}
		} catch (ClassNotFoundException ex) {
			// just return null
		}
		return null;
	}
	
	public Object getAnnotationFromMember(ResolvedType annotationType, Member aMember) {
		if (!(aMember instanceof AccessibleObject)) return null;
		AccessibleObject ao = (AccessibleObject) aMember;
		try {
			Class annotationClass = Class.forName(annotationType.getName());
			if (ao.isAnnotationPresent(annotationClass)) {
				return ao.getAnnotation(annotationClass);
			}
		} catch (ClassNotFoundException ex) {
			// just return null
		}
		return null;
	}
	
	public Set getAnnotations(Member onMember) {
		if (!(onMember instanceof AccessibleObject)) return Collections.EMPTY_SET;
		// here we really want both the runtime visible AND the class visible annotations
		// so we bail out to Bcel and then chuck away the JavaClass so that we don't hog
		// memory.
		try {
			JavaClass jc = bcelRepository.loadClass(onMember.getDeclaringClass());
			org.aspectj.apache.bcel.classfile.annotation.Annotation[] anns = new org.aspectj.apache.bcel.classfile.annotation.Annotation[0];
			if (onMember instanceof Method) {
				org.aspectj.apache.bcel.classfile.Method bcelMethod = jc.getMethod((Method)onMember);
				anns = bcelMethod.getAnnotations();
			} else if (onMember instanceof Constructor) {
				org.aspectj.apache.bcel.classfile.Method bcelCons = jc.getMethod((Constructor)onMember);
				anns = bcelCons.getAnnotations();
			} else if (onMember instanceof Field) {
				org.aspectj.apache.bcel.classfile.Field bcelField = jc.getField((Field)onMember);
				anns = bcelField.getAnnotations();
			}
			// the answer is cached and we don't want to hold on to memory
			bcelRepository.clear();
			if (anns == null) anns = new org.aspectj.apache.bcel.classfile.annotation.Annotation[0];
			// convert to our Annotation type
			Set<UnresolvedType> annSet = new HashSet<UnresolvedType>();
			for (int i = 0; i < anns.length; i++) {
				annSet.add(UnresolvedType.forName(anns[i].getTypeName()));
			}
			return annSet;
		} catch (ClassNotFoundException cnfEx) {
			// just use reflection then
		}
		
		
		AccessibleObject ao = (AccessibleObject) onMember;
		Annotation[] anns = ao.getDeclaredAnnotations();
		Set<UnresolvedType> annSet = new HashSet<UnresolvedType>();
		for (int i = 0; i < anns.length; i++) {
			annSet.add(UnresolvedType.forName(anns[i].annotationType().getName()));
		}
		return annSet;
	}
	
	public ResolvedType[] getAnnotations(Class forClass, World inWorld) {
		// here we really want both the runtime visible AND the class visible annotations
		// so we bail out to Bcel and then chuck away the JavaClass so that we don't hog
		// memory.
		try {
			JavaClass jc = bcelRepository.loadClass(forClass);
			org.aspectj.apache.bcel.classfile.annotation.Annotation[] anns =jc.getAnnotations();
			bcelRepository.clear();
			if (anns == null) return new ResolvedType[0];
			ResolvedType[] ret = new ResolvedType[anns.length];
			for (int i = 0; i < ret.length; i++) {
				ret[i] = inWorld.resolve(anns[i].getTypeName());
			}
			return ret;
		} catch (ClassNotFoundException cnfEx) {
			// just use reflection then
		}
		
		Annotation[] classAnnotations = forClass.getAnnotations();
		ResolvedType[] ret = new ResolvedType[classAnnotations.length];
		for (int i = 0; i < classAnnotations.length; i++) {
			ret[i] = inWorld.resolve(classAnnotations[i].annotationType().getName());
		}
		
		return ret;
	}
	
}
