/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 * ******************************************************************/

package org.aspectj.weaver;

public interface IHasPosition {
	/**
	 * The starting index of this location in the character stream.
	 */
	int getStart();

	/**
	 * The ending index of this location in the character stream
	 * 
	 * This points to the last character in this token.
	 * 
	 * If a location truly had no contents, then start == end + 1. We don't recommend this.
	 */
	int getEnd();
	//
	// String getFileName();

}
