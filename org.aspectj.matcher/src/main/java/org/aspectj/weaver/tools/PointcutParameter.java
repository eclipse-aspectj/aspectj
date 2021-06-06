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
package org.aspectj.weaver.tools;

/**
 * @author colyer
 * Represents a parameter in a pointcut expression.
 * For example pointcut pc(String s) : .....; has a PointcutParameter of
 * name "s" and type String.
 */
public interface PointcutParameter {

	/**
	 * The name of this parameter
	 */
	String getName();

	/**
	 * The type of the parameter
	 */
	Class getType();

	/**
	 * At a matched join point, the parameter binding.
	 */
	Object getBinding();
}
