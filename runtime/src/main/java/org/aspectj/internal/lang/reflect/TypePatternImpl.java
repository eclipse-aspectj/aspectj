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

import org.aspectj.lang.reflect.TypePattern;

/**
 * Default impl of a type pattern.
 *
 */
public class TypePatternImpl implements TypePattern {

	private String typePattern;
	
	public TypePatternImpl(String pattern) {
		this.typePattern = pattern;
	}

	public String asString() {
		return this.typePattern;
	}
	
	public String toString() { return asString(); }

}
