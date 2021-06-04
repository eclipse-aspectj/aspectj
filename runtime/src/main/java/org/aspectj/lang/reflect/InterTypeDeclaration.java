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
package org.aspectj.lang.reflect;

/**
 * Represents an inter-type method, field, or constructor declared in an aspect.
 */
public interface InterTypeDeclaration {

	/**
	 * @return the declaring aspect
	 */
	AjType<?> getDeclaringType();

	/**
	 * @return the target type of this ITD
	 * @throws ClassNotFoundException if the type cannot be found
	 */
	AjType<?> getTargetType() throws ClassNotFoundException;

	/**
	 * @return member modifiers, can be interpreted using java.lang.reflect.Modifier
	 */
	int getModifiers();
}
