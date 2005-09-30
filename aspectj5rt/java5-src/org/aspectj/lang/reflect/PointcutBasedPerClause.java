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
 * Representation of a pointcut based per-clause associated with an aspect
 * (perthis/target/cflow/cflowbelow)
 *
 */
public interface PointcutBasedPerClause extends PerClause {

	/**
	 * Get the associated pointcut expression
	 */
	PointcutExpression getPointcutExpression();
}
