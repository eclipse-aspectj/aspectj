/* *******************************************************************
 * Copyright (c) 2005, 2017 Contributors.
 *
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * ******************************************************************/
package org.aspectj.weaver.reflect;

import java.lang.reflect.Member;

import org.aspectj.weaver.AnnotationAJ;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.World;

/**
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

	ResolvedType[] getAnnotations(Member onMember, boolean runtimeAnnotationsOnly);

	ResolvedType[][] getParameterAnnotationTypes(Member onMember);
}
