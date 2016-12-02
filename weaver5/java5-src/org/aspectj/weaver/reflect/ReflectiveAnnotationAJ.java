/* *******************************************************************
 * Copyright (c) 2016 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *   Andy Clement			Initial implementation
 * ******************************************************************/
package org.aspectj.weaver.reflect;

import java.lang.annotation.Annotation;
import java.util.Set;

import org.aspectj.weaver.AnnotationAJ;
import org.aspectj.weaver.ResolvedType;

/**
 * An AnnotationAJ that wraps a java.lang.reflect Annotation.
 *
 * @author Andy Clement
 */
public class ReflectiveAnnotationAJ implements AnnotationAJ {
	
	private Annotation anno;

	public ReflectiveAnnotationAJ(Annotation anno) {
		this.anno = anno;
	}

	public String getTypeSignature() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getTypeName() {
		// TODO Auto-generated method stub
		return null;
	}

	public ResolvedType getType() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean allowedOnAnnotationType() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean allowedOnField() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean allowedOnRegularType() {
		// TODO Auto-generated method stub
		return false;
	}

	public Set<String> getTargets() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean hasNamedValue(String name) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean hasNameValuePair(String name, String value) {
		// TODO Auto-generated method stub
		return false;
	}

	public String getValidTargets() {
		// TODO Auto-generated method stub
		return null;
	}

	public String stringify() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean specifiesTarget() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isRuntimeVisible() {
		// TODO Auto-generated method stub
		return false;
	}

	public String getStringFormOfValue(String name) {
		// TODO Auto-generated method stub
		return null;
	}

}
