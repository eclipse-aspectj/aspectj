/* *******************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
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
