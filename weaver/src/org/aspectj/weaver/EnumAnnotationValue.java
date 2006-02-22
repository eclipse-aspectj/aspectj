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

public class EnumAnnotationValue extends AnnotationValue {

	private String type;
	private String value;
	
	public EnumAnnotationValue(String type,String value) {
		super(AnnotationValue.ENUM_CONSTANT);
		this.type = type;
		this.value = value;
	}
	
	public String getType() {
		return type;
	}
	
	public String stringify() {
		return value;
	}	
	
	public String toString() {
		return value;
	}

}
