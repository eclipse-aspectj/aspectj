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
