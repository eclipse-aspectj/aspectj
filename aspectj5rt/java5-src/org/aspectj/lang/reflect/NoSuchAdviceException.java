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
 * @author colyer
 *
 */
public class NoSuchAdviceException extends Exception {

	private static final long serialVersionUID = 3256444698657634352L;
	private String name;
	
	public NoSuchAdviceException(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}

}
