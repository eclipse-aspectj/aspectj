/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 *               2004 IBM Corporation.
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * ******************************************************************/


package org.aspectj.weaver.tools;

/**
 * This class implements a boolean that includes a "maybe"
 */
public final class FuzzyBoolean {

	// Note :- this implementation is not safe under serialization / deserialization
	private String name;

    public static final FuzzyBoolean YES   = new FuzzyBoolean("YES");
    public static final FuzzyBoolean NO    = new FuzzyBoolean("NO");
    public static final FuzzyBoolean MAYBE = new FuzzyBoolean("MAYBE");


	public static final FuzzyBoolean fromBoolean(boolean b) {
		return b ? YES : NO;
	}

	public String toString() { return name; }

	private FuzzyBoolean() {}

	private FuzzyBoolean(String n) { this.name = n; }
}
