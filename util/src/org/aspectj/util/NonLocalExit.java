/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.util;

/**
 * Throw this when a non-local exit is required (suggested for tests only).
 */
public class NonLocalExit extends RuntimeException {

	public static final int SUCCEESS = 0;
	public static final int FAULURE = 1;

	private int exitCode;

	public NonLocalExit(int exitCode) {
		this();
		this.exitCode = exitCode;		
	}

	public NonLocalExit() {
		super();
	}

	public int getExitCode() {
		return exitCode;
	}

}
