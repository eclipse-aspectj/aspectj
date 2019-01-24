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

public class AnnotationNameValuePair {

	private String name;

	private AnnotationValue val;

	public AnnotationNameValuePair(String name, AnnotationValue val) {
		this.name = name;
		this.val = val;
	}

	public String getName() {
		return name;
	}

	public AnnotationValue getValue() {
		return val;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(name + "=" + val.toString());
		return sb.toString();
	}

	public String stringify() {
		StringBuffer sb = new StringBuffer();
		if (!name.equals("value")) {
			sb.append(name + "=");
		}
		sb.append(val.stringify());
		return sb.toString();
	}
}
