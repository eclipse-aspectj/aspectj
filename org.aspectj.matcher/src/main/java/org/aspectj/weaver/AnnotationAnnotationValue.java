/* *******************************************************************
 * Copyright (c) 2006 Contributors
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Andy Clement IBM     initial implementation 
 * ******************************************************************/
package org.aspectj.weaver;

public class AnnotationAnnotationValue extends AnnotationValue {

	private AnnotationAJ value;

	public AnnotationAnnotationValue(AnnotationAJ value) {
		super(AnnotationValue.ANNOTATION);
		this.value = value;
	}

	public AnnotationAJ getAnnotation() {
		return value;
	}

	public String stringify() {
		return value.stringify();
	}

	public String toString() {
		return value.toString();
	}

}
