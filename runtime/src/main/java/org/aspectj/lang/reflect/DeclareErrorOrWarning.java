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
 * AspectJ runtime representation of a declare error or declare warning member
 * in an aspect.
 */
public interface DeclareErrorOrWarning {
	
	/**
	 * @return the type that declared this declare warning or declare error member. 
	 */
	AjType getDeclaringType();
	
	/**
	 * @return the pointcut expression associated with the warning or error
	 */
	PointcutExpression getPointcutExpression();
	
	/**
	 * @return the message associated with the declare warning / declare error
	 */
	String getMessage();
	
	/**
	 * @return true if this is a declare error member, false if it is declare warning
	 */
	boolean isError();
	
}
