/* *******************************************************************
 * Copyright (c) 1999-2001 Xerox Corporation, 
 *               2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.asm;

import java.io.*;
import java.util.List;

/**
 * @author Mik Kersten
 */
public interface IRelationship extends Serializable {

	public String getName();
	
	public List getTargets();
	
	public IProgramElement getSource();
	
	public Kind getKind();
		
	/**
	 * Uses "typesafe enum" pattern.
	 */
	public static class Kind implements Serializable {
		
		public static final Kind ADVICE = new Kind("advice");
		public static final Kind DECLARE = new Kind("declare");
		public static final Kind[] ALL = { ADVICE, DECLARE };
		private final String name;
		
		private Kind(String name) {
			this.name = name; 
		}
		
		public String toString() {
			return name;
		}	

		// The 4 declarations below are necessary for serialization
		private static int nextOrdinal = 0;
		private final int ordinal = nextOrdinal++;
		private Object readResolve() throws ObjectStreamException {
			return ALL[ordinal];
		}
	}
}
