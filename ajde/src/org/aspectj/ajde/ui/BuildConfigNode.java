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


package org.aspectj.ajde.ui;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.List;

import org.aspectj.asm.StructureNode;

/**
 * @author Mik Kersten
 */
public class BuildConfigNode extends StructureNode {

	private String resourcePath;
	private Kind kind;
	private boolean isActive = true;
		
	public BuildConfigNode(String name, Kind kind, String resourcePath) {	
		super(name, kind.toString());
		this.kind = kind;
		this.resourcePath = resourcePath;	
	}

	public BuildConfigNode(String name, String kind, String resourcePath, List children) {
		super(name, kind, children);
		this.resourcePath = resourcePath;
	}
	
	public String getResourcePath() {
		return resourcePath;
	}

	public void setResourcePath(String resourcePath) {
		this.resourcePath = resourcePath;
	}
	
	public boolean isValidResource() {
		return name.endsWith(".java") 
			|| name.endsWith(".aj") 
			|| name.endsWith(".lst");	
	}
	
	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

	/**
	 * Uses "typesafe enum" pattern.
	 */
	public static class Kind implements Serializable {
		
		public static final Kind FILE_JAVA = new Kind("Java source file");
		public static final Kind FILE_ASPECTJ = new Kind("AspectJ source file");
		public static final Kind FILE_LST = new Kind("build configuration file");
		public static final Kind ERROR = new Kind("error");
		public static final Kind DIRECTORY = new Kind("directory");


		public static final Kind[] ALL = { FILE_JAVA, FILE_ASPECTJ, FILE_LST, DIRECTORY };
		
		private final String name;
		
		private Kind(String name) {
			this.name = name;
		}
		
		public String toString() {
			return name;
		}	
		
		public boolean equals(Object o) {
			return o.equals(name);	
		}
		
		public boolean isDeclareKind() {
			return name.startsWith("declare");	
		} 

		// The 4 declarations below are necessary for serialization
		private static int nextOrdinal = 0;
		private final int ordinal = nextOrdinal++;
		private Object readResolve() throws ObjectStreamException {
			return ALL[ordinal];
		}
	}
	
	public Kind getBuildConfigNodeKind() {
		return kind;
	}
}



