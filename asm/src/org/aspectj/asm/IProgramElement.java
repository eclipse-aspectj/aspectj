/* *******************************************************************
 * Copyright (c) 1999-2001 Xerox Corporation, 
 *               2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 * ******************************************************************/

package org.aspectj.asm;

import java.io.*;
import java.util.*;

import org.aspectj.bridge.*;

/**
 * Represents program elements in the AspectJ containment hierarchy.
 * 
 * @author Mik Kersten
 */
public interface IProgramElement extends Serializable {
	
	public List/*IProgramElement*/ getChildren();
	public void addChild(IProgramElement child);
	public Kind getKind();
	public List getModifiers();
	public Accessibility getAccessibility();
	public String getDeclaringType();
	public String getPackageName();
	public String getSignature();
	public String getName();
	public boolean isCode();
	public boolean isMemberKind();
	public void setRunnable(boolean value);
	public boolean isRunnable();
	public boolean isImplementor();
	public void setImplementor(boolean value);
	public boolean isOverrider();
	public void setOverrider(boolean value);
	public List getRelations();
	public void setRelations(List relations);
	public String getFormalComment();
	public String toString();
	public String getBytecodeName();
	public String getBytecodeSignature();
	public void setBytecodeName(String bytecodeName);
	public void setBytecodeSignature(String bytecodeSignature);
	public String getFullSignature();
	public void setFullSignature(String string);
	public void setKind(Kind kind);
	public void setReturnType(String returnType);
	public String getReturnType();
	public ISourceLocation getSourceLocation();
	public void setSourceLocation(ISourceLocation sourceLocation);
	public IMessage getMessage();
	public void setMessage(IMessage message);
	public IProgramElement getParent();
	public void setParent(IProgramElement parent);
	public IProgramElement walk(HierarchyWalker walker);
	public void setName(String name);
	public void setChildren(List children);
	
	/**
	 * Uses "typesafe enum" pattern.
	 */
	public static class Modifiers implements Serializable {
		
		public static final Modifiers STATIC = new Modifiers("static");
		public static final Modifiers FINAL = new Modifiers("final");
		public static final Modifiers ABSTRACT = new Modifiers("abstract");
		public static final Modifiers SYNCHRONIZED = new Modifiers("synchronized");
		public static final Modifiers VOLATILE = new Modifiers("volatile");
		public static final Modifiers STRICTFP = new Modifiers("strictfp");
		public static final Modifiers TRANSIENT = new Modifiers("transient");
		public static final Modifiers NATIVE = new Modifiers("native");
		public static final Modifiers[] ALL = { STATIC, FINAL, ABSTRACT, SYNCHRONIZED, TRANSIENT, VOLATILE, STRICTFP, NATIVE };
		private final String name;
		
		private Modifiers(String name) {
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

	/**
	 * Uses "typesafe enum" pattern.
	 */
	public static class Accessibility implements Serializable {
		
		public static final Accessibility PUBLIC = new Accessibility("public");
		public static final Accessibility PACKAGE = new Accessibility("package");
		public static final Accessibility PROTECTED = new Accessibility("protected");
		public static final Accessibility PRIVATE = new Accessibility("private");
		public static final Accessibility PRIVILEGED = new Accessibility("privileged");
		public static final Accessibility[] ALL = { PUBLIC, PACKAGE, PROTECTED, PRIVATE, PRIVILEGED };
		private final String name;
		
		private Accessibility(String name) {
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

	/**
	 * Uses "typesafe enum" pattern.
	 */
	public static class Kind implements Serializable {
		
		public static final Kind PROJECT = new Kind("project");
		public static final Kind PACKAGE = new Kind("package");
		public static final Kind FILE = new Kind("file");
		public static final Kind FILE_JAVA = new Kind("java source file");
		public static final Kind FILE_ASPECTJ = new Kind("aspect source file");
		public static final Kind FILE_LST = new Kind("build configuration file");
		public static final Kind CLASS = new Kind("class");
		public static final Kind INTERFACE = new Kind("interface");
		public static final Kind ASPECT = new Kind("aspect");
		public static final Kind INITIALIZER = new Kind("initializer");
		public static final Kind INTER_TYPE_FIELD = new Kind("inter-type field");
		public static final Kind INTER_TYPE_METHOD = new Kind("inter-type method");
		public static final Kind INTER_TYPE_CONSTRUCTOR = new Kind("inter-type constructor");
		public static final Kind CONSTRUCTOR = new Kind("constructor");
		public static final Kind METHOD = new Kind("method");
		public static final Kind FIELD = new Kind("field");  
		public static final Kind POINTCUT = new Kind("pointcut");
		public static final Kind ADVICE = new Kind("advice");
		public static final Kind DECLARE_PARENTS = new Kind("declare parents");
		public static final Kind DECLARE_WARNING = new Kind("declare warning");
		public static final Kind DECLARE_ERROR = new Kind("declare error");
		public static final Kind DECLARE_SOFT = new Kind("declare soft");
		public static final Kind DECLARE_PRECEDENCE= new Kind("declare precedence");
		public static final Kind CODE = new Kind("decBodyElement");
		public static final Kind ERROR = new Kind("error");

		public static final Kind[] ALL = { PROJECT, PACKAGE, FILE, FILE_JAVA, 
			FILE_ASPECTJ, FILE_LST, CLASS, INTERFACE, ASPECT,
			INITIALIZER, INTER_TYPE_FIELD, INTER_TYPE_METHOD, INTER_TYPE_CONSTRUCTOR, 
			CONSTRUCTOR, METHOD, FIELD, POINTCUT, ADVICE, DECLARE_PARENTS, 
			DECLARE_WARNING, DECLARE_ERROR, DECLARE_SOFT, CODE, ERROR };
		
		public static Kind getKindForString(String kindString) {
			for (int i = 0; i < ALL.length; i++) {
				if (ALL[i].toString().equals(kindString)) return ALL[i];	
			}
			return ERROR;
		}
		
		private final String name;
		
		private Kind(String name) {
			this.name = name;
		}
		
		public String toString() {
			return name;
		}	
		
		public static List getNonAJMemberKinds() {
			List list = new ArrayList();
			list.add(METHOD);
			list.add(FIELD);
			list.add(CONSTRUCTOR);
			return list;
		}
		
		public boolean isMemberKind() {
			return this == FIELD
				|| this == METHOD
				|| this == CONSTRUCTOR
				|| this == POINTCUT
				|| this == ADVICE;
		}
		
		public boolean isTypeKind() {
			return this == CLASS
				|| this == INTERFACE
				|| this == ASPECT;	
		}

		public boolean isSourceFileKind() {
			return this == FILE_ASPECTJ
				|| this == FILE_JAVA;
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
}