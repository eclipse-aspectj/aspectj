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

import java.util.*;
import java.io.*;

import org.aspectj.bridge.ISourceLocation;


/**
 * @author Mik Kersten
 */
public class ProgramElementNode extends StructureNode {
		
    private List modifiers = new ArrayList();
    private List relations = new ArrayList();

	private Kind kind;
	private Accessibility accessibility;
    private String declaringType = "";
    private String formalComment = "";
    private String packageName = null;
    private boolean runnable = false;
    private boolean implementor = false; 
    private boolean overrider = false;
    
    private String bytecodeName;
    private String bytecodeSignature;
    
    
    /**
     * Used during de-externalization.
     */
    public ProgramElementNode() { }

	/**
	 * Use to create program element nodes that do not correspond to source locations.
	 */
	public ProgramElementNode(
		String signature, 
		Kind kind, 
		List children) {
		super(signature, kind.toString(), children);
		this.kind = kind;
	}
	
	public ProgramElementNode(
		String signature,
		ProgramElementNode.Kind kind,
		ISourceLocation sourceLocation,
		int modifiers,
		String formalComment,
		List children)
	{
        super(signature, kind.toString(), children);
        super.sourceLocation = sourceLocation;
        this.kind = kind;
        this.formalComment = formalComment;
        this.modifiers = genModifiers(modifiers);
        this.accessibility = genAccessibility(modifiers);
    }
	
	/**
	 * Use to create program element nodes that correspond to source locations.
	 */
    public ProgramElementNode(
    	String signature, 
    	Kind kind, 
    	List modifiers, 
    	Accessibility accessibility,
        String declaringType, 
        String packageName, 
        String formalComment, 
        ISourceLocation sourceLocation,
        List relations, 
        List children, 
        boolean member) {

        super(signature, kind.toString(), children);
        super.sourceLocation = sourceLocation;
        this.kind = kind;
        this.modifiers = modifiers;
        this.accessibility = accessibility;
        this.declaringType = declaringType;
        this.packageName = packageName;
        this.formalComment = formalComment;
        this.relations = relations;
    }

	public Kind getProgramElementKind() {
		return kind;	
	}

    public List getModifiers() {
        return modifiers;
    }

    public Accessibility getAccessibility() {
        return accessibility;
    }

    public String getDeclaringType() {
        return declaringType;
    }

    public String getPackageName() {
    	if (kind == Kind.PACKAGE) return getSignature();
    	if (getParent() == null || !(getParent() instanceof ProgramElementNode)) {
    		return "";
    	}
    	return ((ProgramElementNode)getParent()).getPackageName();
    }

    public String getKind() {
        return super.kind;
    }

    public String getSignature() {
        return super.name;
    }

    public boolean isCode() {
        return kind.equals(Kind.CODE);
    }

    public boolean isMemberKind() {
        return kind.isMemberKind();
    }

	public void setRunnable(boolean value) {
		this.runnable = value;	
	}

	public boolean isRunnable() {
		return runnable;	
	}

	public boolean isImplementor() {
		return implementor;	
	}

	public void setImplementor(boolean value) {
		this.implementor = value;	
	}
	
	public boolean isOverrider() {
		return overrider;		
	}

	public void setOverrider(boolean value) {
		this.overrider = value;	
	}

    public List getRelations() {
        return relations;
    }

    public void setRelations(List relations) {
        if (relations.size() > 0) {
            this.relations = relations;
        }
    }

    public String getFormalComment() {
        return formalComment;
    }

    public String toString() {
        return super.name;
    }

	public static List genModifiers(int modifiers) {
		List modifiersList = new ArrayList();
		if ((modifiers & AccStatic) != 0) modifiersList.add(ProgramElementNode.Modifiers.STATIC);
		if ((modifiers & AccFinal) != 0) modifiersList.add(ProgramElementNode.Modifiers.STATIC);
		if ((modifiers & AccSynchronized) != 0) modifiersList.add(ProgramElementNode.Modifiers.STATIC);
		if ((modifiers & AccVolatile) != 0) modifiersList.add(ProgramElementNode.Modifiers.STATIC);
		if ((modifiers & AccTransient) != 0) modifiersList.add(ProgramElementNode.Modifiers.STATIC);
		if ((modifiers & AccNative) != 0) modifiersList.add(ProgramElementNode.Modifiers.STATIC);
		if ((modifiers & AccAbstract) != 0) modifiersList.add(ProgramElementNode.Modifiers.STATIC);
		return modifiersList;		
	}

	public static ProgramElementNode.Accessibility genAccessibility(int modifiers) {
		if ((modifiers & AccPublic) != 0) return ProgramElementNode.Accessibility.PUBLIC;
		if ((modifiers & AccPrivate) != 0) return ProgramElementNode.Accessibility.PRIVATE;
		if ((modifiers & AccProtected) != 0) return ProgramElementNode.Accessibility.PROTECTED;
		if ((modifiers & AccPrivileged) != 0) return ProgramElementNode.Accessibility.PRIVILEGED;
		else return ProgramElementNode.Accessibility.PACKAGE;
	}

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
		public static final Kind INTRODUCTION = new Kind("introduction");
		public static final Kind CONSTRUCTOR = new Kind("constructor");
		public static final Kind METHOD = new Kind("method");
		public static final Kind FIELD = new Kind("field");  
		public static final Kind POINTCUT = new Kind("pointcut");
		public static final Kind ADVICE = new Kind("advice");
		public static final Kind DECLARE_PARENTS = new Kind("declare parents");
		public static final Kind DECLARE_WARNING = new Kind("declare warning");
		public static final Kind DECLARE_ERROR = new Kind("declare error");
		public static final Kind DECLARE_SOFT = new Kind("declare soft");
		public static final Kind CODE = new Kind("decBodyElement");
		public static final Kind ERROR = new Kind("error");

		public static final Kind[] ALL = { PROJECT, PACKAGE, FILE, FILE_JAVA, 
			FILE_ASPECTJ, FILE_LST, CLASS, INTERFACE, ASPECT,
			INITIALIZER, INTRODUCTION, CONSTRUCTOR, METHOD, FIELD, POINTCUT, ADVICE, 
			DECLARE_PARENTS, DECLARE_WARNING, DECLARE_ERROR, DECLARE_SOFT, CODE, ERROR };
		
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
	
	// XXX these names and values are from org.eclipse.jdt.internal.compiler.env.IConstants
	private static int AccPublic = 0x0001;
	private static int AccPrivate = 0x0002;
	private static int AccProtected = 0x0004;
	private static int AccPrivileged = 0x0006;  // XXX is this right?
	private static int AccStatic = 0x0008;
	private static int AccFinal = 0x0010;
	private static int AccSynchronized = 0x0020;
	private static int AccVolatile = 0x0040;
	private static int AccTransient = 0x0080;
	private static int AccNative = 0x0100;
	private static int AccInterface = 0x0200;
	private static int AccAbstract = 0x0400;
	private static int AccStrictfp = 0x0800;
	
	
	public String getBytecodeName() {
		return bytecodeName;
	}

	public String getBytecodeSignature() {
		return bytecodeSignature;
	}

	public void setBytecodeName(String bytecodeName) {
		this.bytecodeName = bytecodeName;
	}

	public void setBytecodeSignature(String bytecodeSignature) {
		this.bytecodeSignature = bytecodeSignature;
	}

}

