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

import java.lang.reflect.Type;

/**
 * Runtime representation of an inter-type constructor member declared within an
 * aspect.
 */
public interface InterTypeConstructorDeclaration extends InterTypeDeclaration {

	/**
	 * @return the constructor parameters
	 */
	AjType<?>[] getParameterTypes();

	/**
	 * @return the generic constructor parameters
	 */
	Type[] getGenericParameterTypes();

	/**
	 * @return the declared exceptions thrown by this constructor
	 */
	AjType<?>[] getExceptionTypes();
}
