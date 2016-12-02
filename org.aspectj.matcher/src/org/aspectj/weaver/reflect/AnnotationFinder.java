/* *******************************************************************
 * Copyright (c) 2005-2016 Contributors.
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

import java.lang.reflect.Member;
import java.util.Set;

import org.aspectj.weaver.AnnotationAJ;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.World;

/**
 * Used in 1.4 code to access annotations safely.
 * 
 * @author Adrian Colyer 
 * @author Andy Clement
 */
public interface AnnotationFinder {

	void setClassLoader(ClassLoader annotationLoader);

	void setWorld(World aWorld);

	Object getAnnotation(ResolvedType annotationType, Object onObject);

	Object getAnnotationFromMember(ResolvedType annotationType, Member aMember);

	AnnotationAJ getAnnotationOfType(UnresolvedType ofType, Member onMember);

	String getAnnotationDefaultValue(Member onMember);

	Object getAnnotationFromClass(ResolvedType annotationType, Class<?> aClass);

	Set<ResolvedType> getAnnotations(Member onMember);

	ResolvedType[][] getParameterAnnotationTypes(Member onMember);

	Object getParamAnnotation(Member subject, int argsIndex, int paramAnnoIndex);
}
