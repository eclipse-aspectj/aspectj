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
package org.aspectj.lang.reflect;

/**
 * Represents an anonymous pointcut expression as used in pointcuts, advice declarations,
 * declares, and per-clauses
 */
public interface PointcutExpression {

	/**
	 * Returns a String representation of the pointcut expression 
	 */
	String asString();
}
