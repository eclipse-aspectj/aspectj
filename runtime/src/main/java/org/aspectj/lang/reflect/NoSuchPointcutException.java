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
 * Thrown when AjType.getDeclaredPointcut is called with a pointcut name, and no
 * matching pointcut declaration can be found.
 */
public class NoSuchPointcutException extends Exception {

	private static final long serialVersionUID = 3256444698657634352L;
	private String name;
	
	public NoSuchPointcutException(String name) {
		this.name = name;
	}
	
	/**
	 * The name of the pointcut that could not be found.
	 */
	public String getName() {
		return name;
	}

}
