/* *******************************************************************
 * Copyright (c) 2003 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     Mik Kersten     initial implementation 
 * ******************************************************************/


package org.aspectj.asm;

import java.io.*;
import java.util.List;

/**
 * @author Mik Kersten
 */
public interface IRelationship extends Serializable {

	public String getName();
	
	public List/*String*/ getTargets();
	
	public String getSourceHandle();
	
	public Kind getKind();
		
	/**
	 * Uses "typesafe enum" pattern.
	 */
	public static class Kind implements Serializable {
		
		public static final Kind ADVICE = new Kind("advice");
		public static final Kind DECLARE = new Kind("declare");
		public static final Kind DECLARE_INTER_TYPE = new Kind("inter-type declaration");
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
