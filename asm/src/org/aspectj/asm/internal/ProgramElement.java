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
 *     Andy Clement    Extensions for better IDE representation
 * ******************************************************************/

package org.aspectj.asm.internal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.aspectj.asm.AsmManager;
import org.aspectj.asm.HierarchyWalker;
import org.aspectj.asm.IProgramElement;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.ISourceLocation;


/**
 * @author Mik Kersten
 */
public class ProgramElement implements IProgramElement {
		
	private static final long serialVersionUID = 171673495267384449L;
	
	protected IProgramElement parent = null;
	protected String name = "";
	// children.listIterator() should support remove() operation
	protected List children = new ArrayList();
	protected IMessage message = null;
	protected ISourceLocation sourceLocation = null;

	private List modifiers = new ArrayList();
	private List relations = new ArrayList();

	private Kind kind;
	private Accessibility accessibility;
	private String declaringType = "";
	private String formalComment = "";
	//private String packageName = null;
	private boolean runnable = false;
	private boolean implementor = false; 
	private boolean overrider = false;
    
	private String bytecodeName;
	private String bytecodeSignature;
//	private String fullSignature;
	private String returnType;
	
	private List parameterNames = null;
	private List parameterTypes = null;
	
	private String details = null;
	
	private ExtraInformation info;
    
	/**
	 * Used during de-externalization.
	 */
	public ProgramElement() { }

	/**
	 * Use to create program element nodes that do not correspond to source locations.
	 */
	public ProgramElement(
		String name, 
		Kind kind, 
		List children) {
		this.name = name;
		this.kind = kind;
		setChildren(children);
	}
	
	public ProgramElement(
		String name,
		IProgramElement.Kind kind,
		ISourceLocation sourceLocation,
		int modifiers,
		String formalComment,
		List children)
	{
		this(name, kind, children);
		this.sourceLocation = sourceLocation;
		this.kind = kind;
		this.formalComment = formalComment;
		this.modifiers = genModifiers(modifiers);
		this.accessibility = genAccessibility(modifiers);
		cacheByHandle();
	}
	
	/**
	 * Use to create program element nodes that correspond to source locations.
	 */
	public ProgramElement(
		String name, 
		Kind kind, 
		int modifiers, 
		Accessibility accessibility,
		String declaringType, 
		String packageName, 
		String formalComment, 
		ISourceLocation sourceLocation,
		List relations, 
		List children, 
		boolean member) {

		this(name, kind, children);
		this.sourceLocation = sourceLocation;
		this.kind = kind;
		this.modifiers = genModifiers(modifiers);
		this.accessibility = accessibility;
		this.declaringType = declaringType;
		//this.packageName = packageName;
		this.formalComment = formalComment;
		this.relations = relations;
		cacheByHandle();
	}

	public List getModifiers() {
		return modifiers;
	}

	public Accessibility getAccessibility() {
		return accessibility;
	}
	
	public void setAccessibility(Accessibility a) {
		accessibility=a;
	}

	public String getDeclaringType() {
		return declaringType;
	}

	public String getPackageName() {
		if (kind == Kind.PACKAGE) return getName();
		if (getParent() == null || !(getParent() instanceof IProgramElement)) {
			return "";
		}
		return ((IProgramElement)getParent()).getPackageName();
	}

	public Kind getKind() {
		return kind;
	}

	public boolean isCode() {
		return kind.equals(Kind.CODE);
	}

	public ISourceLocation getSourceLocation() {
		return sourceLocation;
	}

	public void setSourceLocation(ISourceLocation sourceLocation) {
		this.sourceLocation = sourceLocation;
	}

	public IMessage getMessage() {
		return message;
	}

	public void setMessage(IMessage message) {
		this.message = message;
	}

	public IProgramElement getParent() {
		return parent;
	}

	public void setParent(IProgramElement parent) {
		this.parent = parent;
	}

	public boolean isMemberKind() {
		return kind.isMember();
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
		return toLabelString();
	}

	private static List genModifiers(int modifiers) {
		List modifiersList = new ArrayList();
		if ((modifiers & AccStatic) != 0) modifiersList.add(IProgramElement.Modifiers.STATIC);
		if ((modifiers & AccFinal) != 0) modifiersList.add(IProgramElement.Modifiers.FINAL);
		if ((modifiers & AccSynchronized) != 0) modifiersList.add(IProgramElement.Modifiers.SYNCHRONIZED);
		if ((modifiers & AccVolatile) != 0) modifiersList.add(IProgramElement.Modifiers.VOLATILE);
		if ((modifiers & AccTransient) != 0) modifiersList.add(IProgramElement.Modifiers.TRANSIENT);
		if ((modifiers & AccNative) != 0) modifiersList.add(IProgramElement.Modifiers.NATIVE);
		if ((modifiers & AccAbstract) != 0) modifiersList.add(IProgramElement.Modifiers.ABSTRACT);
		return modifiersList;		  
	}

	public static IProgramElement.Accessibility genAccessibility(int modifiers) {
		if ((modifiers & AccPublic) != 0) return IProgramElement.Accessibility.PUBLIC;
		if ((modifiers & AccPrivate) != 0) return IProgramElement.Accessibility.PRIVATE;
		if ((modifiers & AccProtected) != 0) return IProgramElement.Accessibility.PROTECTED;
		if ((modifiers & AccPrivileged) != 0) return IProgramElement.Accessibility.PRIVILEGED;
		else return IProgramElement.Accessibility.PACKAGE;
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
//	private static int AccInterface = 0x0200;
	private static int AccAbstract = 0x0400;
//	private static int AccStrictfp = 0x0800;

	private String sourceSignature;
	
	
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
 
	public String getSourceSignature() {
		return sourceSignature;
	}

	public void setSourceSignature(String string) {
		sourceSignature = string;
	}
	
	public void setKind(Kind kind) {
		this.kind = kind;
	}

	public void setCorrespondingType(String returnType) {
		this.returnType = returnType;
	}

	public String getCorrespondingType() {
		return getCorrespondingType(false);
	}
	
	public String getCorrespondingType(boolean getFullyQualifiedType) {
		if (getFullyQualifiedType) {
			return returnType;
		}
		int index = returnType.lastIndexOf(".");
		if (index != -1) {
			return returnType.substring(index);
		}
		return returnType;
	}

	public String getName() {
		return name;
	}

	public List getChildren() {
		return children;
	}

	public void setChildren(List children) {
		this.children = children;
		if (children == null) return;
		for (Iterator it = children.iterator(); it.hasNext(); ) {
			((IProgramElement)it.next()).setParent(this);	
		}
	}

	public void addChild(IProgramElement child) {
		if (children == null) {
			children = new ArrayList();
		}
		children.add(child);
		child.setParent(this);
	}
    
	public void addChild(int position, IProgramElement child) {
		if (children == null) {
			children = new ArrayList();
		}
		children.add(position, child);
		child.setParent(this);
	}
    
	public boolean removeChild(IProgramElement child) {
		child.setParent(null);
		return children.remove(child);	
	}
	
	public void setName(String string) {
		name = string;
	}

	public IProgramElement walk(HierarchyWalker walker) {
		if (children!=null) {
		for (Iterator it = children.iterator(); it.hasNext(); ) {
			IProgramElement child = (IProgramElement)it.next();
			walker.process(child);	
		} 
		}
		return this;
	}
	
	public String toLongString() {
		final StringBuffer buffer = new StringBuffer();
		HierarchyWalker walker = new HierarchyWalker() {
			private int depth = 0;
			
			public void preProcess(IProgramElement node) { 
				for (int i = 0; i < depth; i++) buffer.append(' ');
				buffer.append(node.toString());
				buffer.append('\n');
				depth += 2;
			}
			
			public void postProcess(IProgramElement node) { 
				depth -= 2;
			}
		};
		walker.process(this);
		return buffer.toString();
	}
	
	public void setModifiers(int i) {
		this.modifiers = genModifiers(i);
	}

	public String toSignatureString() {
		StringBuffer sb = new StringBuffer();
		sb.append(name);
		
		if (parameterTypes != null ) {
			sb.append('('); 
			for (Iterator it = parameterTypes.iterator(); it.hasNext(); ) {
				sb.append((String)it.next());
				if (it.hasNext()) sb.append(", ");
			}
			sb.append(')');
		}
		
		return sb.toString();
	}

	
	public static boolean shortITDNames = true;
	
	/**
	 * TODO: move the "parent != null"==>injar heuristic to more explicit 
	 */
	public String toLinkLabelString() {
		String label;
		if (kind == Kind.CODE || kind == Kind.INITIALIZER) {
			label = parent.getParent().getName() + ": ";
		} else if (kind.isInterTypeMember()) {
			if (shortITDNames) {
				// if (name.indexOf('.')!=-1) return toLabelString().substring(name.indexOf('.')+1);
				label="";
			} else {
			  int dotIndex = name.indexOf('.');  
			  if (dotIndex != -1) {
				return parent.getName() + ": " + toLabelString().substring(dotIndex+1);
			  } else {
				label = parent.getName() + '.';	
			  }
			}
		} else if (kind == Kind.CLASS || kind == Kind.ASPECT || kind == Kind.INTERFACE) {
			label = "";
		} else if (kind.equals(Kind.DECLARE_PARENTS)) {
			label = "";
		} else { 
			if (parent != null) {
				label = parent.getName() + '.';
			} else { 
				label = "injar aspect: ";  
			}
		}
		label += toLabelString();
		return label;
	}

	public String toLabelString() {
		String label = toSignatureString();
		if (details != null) {
			label += ": " + details;
		} 
		return label;
	}

	private String handle = null;
	public String getHandleIdentifier() {
	    if (null == handle) {
			if (sourceLocation != null) {
                return AsmManager.getDefault().getHandleProvider().createHandleIdentifier(sourceLocation);
//			    return genHandleIdentifier(sourceLocation);
			} 
	    }
	    return handle;
	}
	
	public List getParameterNames() {
		return parameterNames;
	}

	public List getParameterTypes() {
		return parameterTypes;
	}

	public void setParameterNames(List list) {
		parameterNames = list;
	}

	public void setParameterTypes(List list) {
		parameterTypes = list;
	}

	public String getDetails() {
		return details;
	}

	public void setDetails(String string) {
		details = string;
	}
	
	public void setFormalComment(String formalComment) {
		this.formalComment = formalComment;
	}
	
	/** AMC added to speed up findByHandle lookups in AspectJElementHierarchy */
	private void cacheByHandle() {
		String handle = getHandleIdentifier();
		if (handle != null) {
			AspectJElementHierarchy hierarchy = (AspectJElementHierarchy) 
				AsmManager.getDefault().getHierarchy();
			hierarchy.cache(handle,this);
		}
	}

	public void setExtraInfo(ExtraInformation info) {
		this.info = info;
		
	}

	public ExtraInformation getExtraInfo() {
		return info;
	}
}

