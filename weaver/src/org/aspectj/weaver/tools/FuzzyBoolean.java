/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 *               2004 IBM Corporation.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * ******************************************************************/


package org.aspectj.weaver.tools;

/** This class implements a boolean that includes a "maybe"
 */
public class FuzzyBoolean {
	
	// Note :- this implementation is not safe under serialization / deserialization
	
    public static final FuzzyBoolean YES   = new FuzzyBoolean();
    public static final FuzzyBoolean NO    = new FuzzyBoolean();
    public static final FuzzyBoolean MAYBE = new FuzzyBoolean();

	public static final FuzzyBoolean fromBoolean(boolean b) {
		return b ? YES : NO;
	}

	private FuzzyBoolean() {}
}
