/* *******************************************************************
 * Copyright (c) 2003 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Mik Kersten     initial implementation 
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

	public void setChildren(List children);	
	public void addChild(IProgramElement child);
	public boolean removeChild(IProgramElement child);
	
	// Extra stuff
	// Could be just a string but may prove more useful as an object in the long run ...
	public static class ExtraInformation implements Serializable {
		private static final long serialVersionUID = -3880735494840820638L;
		private String extraInfo;
		public ExtraInformation() { extraInfo = "";}
	
		public void   setExtraAdviceInformation(String string) {extraInfo = string;}
		public String getExtraAdviceInformation()              {return extraInfo;}
		
		public String toString() {
			return "ExtraInformation: ["+extraInfo+"]";
		}
	}
	
	public void setExtraInfo(ExtraInformation info);
	public ExtraInformation getExtraInfo();

	public IProgramElement getParent();
	public void setParent(IProgramElement parent);

	public String getName();
	public void setName(String name);

	public String getDetails();
	public void setDetails(String details);
	
	public IProgramElement.Kind getKind();
	public void setKind(Kind kind);
		
	public List getModifiers();
	public void setModifiers(int i);

	public Accessibility getAccessibility();

	public String getDeclaringType();  // TODO: remove (Emacs uses it)
	public String getPackageName();

	/**
	 * @param method return types or field types
	 */
	public void setCorrespondingType(String returnType);

	/** 
	 * This correponds to both method return types and field types.
	 */
	public String getCorrespondingType();	
	public String getCorrespondingType(boolean getFullyQualifiedType);
	
	public String toSignatureString();
	public String toSignatureString(boolean getFullyQualifiedArgTypes);
	
	public void setRunnable(boolean value);
	public boolean isRunnable();
	
	public boolean isImplementor();
	public void setImplementor(boolean value);
	
	public boolean isOverrider();
	public void setOverrider(boolean value);

	public IMessage getMessage();
	public void setMessage(IMessage message);

	public ISourceLocation getSourceLocation();
	public void setSourceLocation(ISourceLocation sourceLocation);
	
	public String toString();

	/**
	 * @return the javadoc comment for this program element, null if not available
	 */
	public String getFormalComment();
	public void setFormalComment(String comment);
	  
	/**
	 * Includes information about the origin of the node.
	 */
	public String toLinkLabelString();
	public String toLinkLabelString(boolean getFullyQualifiedArgTypes);

	/**
	 * Includes name, parameter types (if any) and details (if any).
	 */
	public String toLabelString();
	public String toLabelString(boolean getFullyQualifiedArgTypes);

	public List getParameterNames();
	public void setParameterNames(List list);
	
	public List getParameterSignatures();
	public void setParameterSignatures(List list);
	public List getParameterTypes();
	
	/**
	 * The format of the string handle is not specified, but is stable across 
	 * compilation sessions.
	 * 
	 * @return	a string representation of this element
	 */
	public String getHandleIdentifier();
	public String getHandleIdentifier(boolean create);
	public void setHandleIdentifier(String handle);
	
	/**
	 * @return	a string representation of this node and all of its children (recursive)
	 */
	public String toLongString();
	
	public String getBytecodeName();
	public String getBytecodeSignature();
	public void setBytecodeName(String bytecodeName);
	public void setBytecodeSignature(String bytecodeSignature);

	/**
	 * @return the full signature of this element, as it appears in the source
	 */
	public String getSourceSignature();
	public void setSourceSignature(String string);
	
	public IProgramElement walk(HierarchyWalker walker);
	
	/**
	 * Uses "typesafe enum" pattern.
	 */
	public static class Modifiers implements Serializable {
		
		private static final long serialVersionUID = -8279300899976607927L;
		
		public static final Modifiers STATIC = new Modifiers("static");
		public static final Modifiers FINAL = new Modifiers("final");
		public static final Modifiers ABSTRACT = new Modifiers("abstract");
		public static final Modifiers SYNCHRONIZED = new Modifiers("synchronized");
		public static final Modifiers VOLATILE = new Modifiers("volatile");
		public static final Modifiers STRICTFP = new Modifiers("strictfp");
		public static final Modifiers TRANSIENT = new Modifiers("transient");
		public static final Modifiers NATIVE = new Modifiers("native");
		public static final Modifiers[] ALL = { STATIC, FINAL, ABSTRACT, SYNCHRONIZED, VOLATILE, STRICTFP, TRANSIENT, NATIVE };
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
		
		private static final long serialVersionUID = 5371838588180918519L;
		
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
		
		private static final long serialVersionUID = -1963553877479266124L;

		public static final Kind PROJECT = new Kind("project");
		public static final Kind PACKAGE = new Kind("package");
		public static final Kind FILE = new Kind("file");
		public static final Kind FILE_JAVA = new Kind("java source file");
		public static final Kind FILE_ASPECTJ = new Kind("aspect source file");
		public static final Kind FILE_LST = new Kind("build configuration file");
		public static final Kind IMPORT_REFERENCE = new Kind("import reference");
		public static final Kind CLASS = new Kind("class");
		public static final Kind INTERFACE = new Kind("interface");
		public static final Kind ASPECT = new Kind("aspect");
		public static final Kind ENUM = new Kind("enum");
		public static final Kind ENUM_VALUE = new Kind("enumvalue");
		public static final Kind ANNOTATION = new Kind("annotation");
		public static final Kind INITIALIZER = new Kind("initializer");
		public static final Kind INTER_TYPE_FIELD = new Kind("inter-type field");
		public static final Kind INTER_TYPE_METHOD = new Kind("inter-type method");
		public static final Kind INTER_TYPE_CONSTRUCTOR = new Kind("inter-type constructor");
		public static final Kind INTER_TYPE_PARENT = new Kind("inter-type parent");
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
		public static final Kind CODE = new Kind("code");
		public static final Kind ERROR = new Kind("error");
		public static final Kind DECLARE_ANNOTATION_AT_CONSTRUCTOR = new Kind("declare @constructor");
		public static final Kind DECLARE_ANNOTATION_AT_FIELD = new Kind("declare @field");
		public static final Kind DECLARE_ANNOTATION_AT_METHOD = new Kind("declare @method");
		public static final Kind DECLARE_ANNOTATION_AT_TYPE = new Kind("declare @type");

 

		public static final Kind[] ALL =
			{
				PROJECT,
				PACKAGE,
				FILE,
				FILE_JAVA,
				FILE_ASPECTJ,
				FILE_LST,
				IMPORT_REFERENCE,
				CLASS,
				INTERFACE,
				ASPECT,
				ENUM,
				ENUM_VALUE,
				ANNOTATION,
				INITIALIZER,
				INTER_TYPE_FIELD,
				INTER_TYPE_METHOD,
				INTER_TYPE_CONSTRUCTOR,
				INTER_TYPE_PARENT,
				CONSTRUCTOR,
				METHOD,
				FIELD,
				POINTCUT,
				ADVICE,
				DECLARE_PARENTS,
				DECLARE_WARNING,
				DECLARE_ERROR,
				DECLARE_SOFT,
				DECLARE_PRECEDENCE,
				CODE,
				ERROR,
				DECLARE_ANNOTATION_AT_CONSTRUCTOR,
				DECLARE_ANNOTATION_AT_FIELD,
				DECLARE_ANNOTATION_AT_METHOD,
				DECLARE_ANNOTATION_AT_TYPE

			};
		
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
			list.add(ENUM_VALUE);
			list.add(FIELD);
			list.add(CONSTRUCTOR);
			return list;
		}
		
		public boolean isMember() {
			return this == FIELD
				|| this == METHOD
				|| this == CONSTRUCTOR
				|| this == POINTCUT
				|| this == ADVICE
				|| this == ENUM_VALUE;
		}

		public boolean isInterTypeMember() {
			return this == INTER_TYPE_CONSTRUCTOR
				|| this == INTER_TYPE_FIELD
				|| this == INTER_TYPE_METHOD;
		}
		
		public boolean isType() {
			return this == CLASS
				|| this == INTERFACE
				|| this == ASPECT
				|| this == ANNOTATION 
				|| this == ENUM;
		}

		public boolean isSourceFile() {
			return this == FILE_ASPECTJ
				|| this == FILE_JAVA;
		}
		
		public boolean isDeclare() {
			return name.startsWith("declare");	
		} 

		public boolean isDeclareAnnotation() {
			return name.startsWith("declare @");	
		}
		
		// The 4 declarations below are necessary for serialization
		private static int nextOrdinal = 0;
		private final int ordinal = nextOrdinal++;
		private Object readResolve() throws ObjectStreamException {
			return ALL[ordinal];
		}
	}
}