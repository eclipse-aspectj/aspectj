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
 *     Andy Clement    Extensions for better IDE representation
 * ******************************************************************/

package org.aspectj.asm.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.aspectj.asm.AsmManager;
import org.aspectj.asm.HierarchyWalker;
import org.aspectj.asm.IProgramElement;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.ISourceLocation;
import org.aspectj.util.CharOperation;
import org.aspectj.util.NameConvertor;


/**
 * @author Mik Kersten
 */
public class ProgramElement implements IProgramElement {
	
	private static final long serialVersionUID = 171673495267384449L;
	public static boolean shortITDNames = true;
	
	private final static String UNDEFINED = "<undefined>";
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

	
	
	protected String name;
	private Kind kind;
	protected IProgramElement parent = null;
	protected List children = Collections.EMPTY_LIST;
	private Map kvpairs = Collections.EMPTY_MAP;	
	protected ISourceLocation sourceLocation = null;
	private int modifiers;
	private String handle = null;


    
	// --- ctors
	
	/** Used during de-externalization */
	public ProgramElement() { }

	/** Use to create program element nodes that do not correspond to source locations */
	public ProgramElement (String name,Kind kind,List children) {
		this.name = name;
		this.kind = kind;
		if (children!=null) setChildren(children);
	}
	
	public ProgramElement (String name, IProgramElement.Kind kind, ISourceLocation sourceLocation,
		                    int modifiers, String comment, List children) {
		this(name, kind, children);
		this.sourceLocation = sourceLocation;
		setFormalComment(comment);
//		if (comment!=null && comment.length()>0) formalComment = comment;
		this.modifiers = modifiers;
//		this.accessibility = genAccessibility(modifiers);
	}
	
	/**
	 * Use to create program element nodes that correspond to source locations.
	 */
	public ProgramElement(
		String name, 
		Kind kind, 
		int modifiers, 
		//Accessibility accessibility,
		String declaringType, 
		String packageName, 
		String comment, 
		ISourceLocation sourceLocation,
		List relations, 
		List children, 
		boolean member) {

		this(name, kind, children);
		this.sourceLocation = sourceLocation;
		this.kind = kind;
		this.modifiers = modifiers;
//		this.accessibility = accessibility;
		setDeclaringType(declaringType);//this.declaringType = declaringType;
		//this.packageName = packageName;
		setFormalComment(comment);
//		if (comment!=null && comment.length()>0) formalComment = comment;
		if (relations!=null && relations.size()!=0) setRelations(relations);
//		this.relations = relations;
	}

	public List getModifiers() {
		return genModifiers(modifiers);
	}

	public Accessibility getAccessibility() {
		return genAccessibility(modifiers); // accessibility
	}
	
//	public void setAccessibility(Accessibility a) {
//		
//		//accessibility=a;
//	}
	
	public void setDeclaringType(String t) {
		if (t!=null && t.length()>0) {
			if (kvpairs==Collections.EMPTY_MAP) kvpairs = new HashMap();
			kvpairs.put("declaringType",t);
		}
	}

	public String getDeclaringType() {
		String dt = (String)kvpairs.get("declaringType");
		if (dt==null) return ""; // assumption that not having one means "" is at HtmlDecorator line 111
		return dt;
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

	// not really sure why we have this setter ... how can we be in the situation where we didn't
	// know the location when we built the node but we learned it later on?
	public void setSourceLocation(ISourceLocation sourceLocation) {
//		this.sourceLocation = sourceLocation;
	}

	public IMessage getMessage() {
		return (IMessage)kvpairs.get("message");
//		return message;
	}

	public void setMessage(IMessage message) {
		if (kvpairs==Collections.EMPTY_MAP) kvpairs = new HashMap();
		kvpairs.put("message",message);
//		this.message = message;
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
		if (kvpairs==Collections.EMPTY_MAP) kvpairs = new HashMap();
		if (value) kvpairs.put("isRunnable","true");
		else       kvpairs.remove("isRunnable");
//		this.runnable = value;	
	}

	public boolean isRunnable() {
		return kvpairs.get("isRunnable")!=null;
//		return runnable;	
	}

	public boolean isImplementor() {
		return kvpairs.get("isImplementor")!=null;
//		return implementor;	
	}

	public void setImplementor(boolean value) {
		if (kvpairs==Collections.EMPTY_MAP) kvpairs = new HashMap();
		if (value) kvpairs.put("isImplementor","true");
		else       kvpairs.remove("isImplementor");
//		this.implementor = value;	
	}
	
	public boolean isOverrider() {
		return kvpairs.get("isOverrider")!=null;
//		return overrider;		
	}

	public void setOverrider(boolean value) {
		if (kvpairs==Collections.EMPTY_MAP) kvpairs = new HashMap();
		if (value) kvpairs.put("isOverrider","true");
		else       kvpairs.remove("isOverrider");
//		this.overrider = value;	
	}

	public List getRelations() {
		return (List)kvpairs.get("relations");
//		return relations;
	}

	public void setRelations(List relations) {
		if (relations.size() > 0) {
			if (kvpairs==Collections.EMPTY_MAP) kvpairs = new HashMap();
			kvpairs.put("relations",relations);
//			this.relations = relations;
		}
	}

	public String getFormalComment() {
		return (String)kvpairs.get("formalComment");
//		return formalComment;
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
	

	
	
	public String getBytecodeName() {
		String s = (String)kvpairs.get("bytecodeName");
		if (s==null) return UNDEFINED;
		return s;
	}

	public String getBytecodeSignature() {
		String s = (String)kvpairs.get("bytecodeSignature");
//		if (s==null) return UNDEFINED;
		return s;
	}

	public void setBytecodeName(String s) {
		if (kvpairs==Collections.EMPTY_MAP) kvpairs = new HashMap();
		kvpairs.put("bytecodeName",s);
	}

	public void setBytecodeSignature(String s) {
		if (kvpairs==Collections.EMPTY_MAP) kvpairs = new HashMap();
		kvpairs.put("bytecodeSignature",s);
	}
 
	public String getSourceSignature() {
		return (String)kvpairs.get("sourceSignature");
	}

	public void setSourceSignature(String string) {
		if (kvpairs==Collections.EMPTY_MAP) kvpairs = new HashMap();
//		System.err.println(name+" SourceSig=>"+string);
		kvpairs.put("sourceSignature",string);
//		sourceSignature = string;
	}
	
	public void setKind(Kind kind) {
		this.kind = kind;
	}

	public void setCorrespondingType(String s) {
		if (kvpairs==Collections.EMPTY_MAP) kvpairs = new HashMap();
		kvpairs.put("returnType",s);
//		this.returnType = s;
	}

	public String getCorrespondingType() {
		return getCorrespondingType(false);
	}
	
	public String getCorrespondingType(boolean getFullyQualifiedType) {
		String returnType = (String)kvpairs.get("returnType");
		if (returnType==null) returnType="";
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
		if (children == null || children==Collections.EMPTY_LIST) children = new ArrayList();
		children.add(child);
		child.setParent(this);
	}
    
	public void addChild(int position, IProgramElement child) {
		if (children == null || children==Collections.EMPTY_LIST) children = new ArrayList();
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
		this.modifiers = i;
	}

	public String toSignatureString() {
		return toSignatureString(true);
	}

	public String toSignatureString(boolean getFullyQualifiedArgTypes) {
		StringBuffer sb = new StringBuffer();
		sb.append(name);
		
		List ptypes = getParameterTypes();
		if (ptypes != null && (!ptypes.isEmpty() 
				|| this.kind.equals(IProgramElement.Kind.METHOD))
				|| this.kind.equals(IProgramElement.Kind.CONSTRUCTOR)
				|| this.kind.equals(IProgramElement.Kind.ADVICE)
				|| this.kind.equals(IProgramElement.Kind.POINTCUT)
				|| this.kind.equals(IProgramElement.Kind.INTER_TYPE_METHOD)
				|| this.kind.equals(IProgramElement.Kind.INTER_TYPE_CONSTRUCTOR)) {
			sb.append('('); 
			for (Iterator it = ptypes.iterator(); it.hasNext(); ) {
				char[] arg = (char[])it.next();
				if (getFullyQualifiedArgTypes) {
					sb.append(arg);
				} else {
					int index = CharOperation.lastIndexOf('.',arg);
					if (index != -1) {
						sb.append(CharOperation.subarray(arg,index+1,arg.length));
					} else {
						sb.append(arg);
					}
				}
				if (it.hasNext()) sb.append(",");
			}
			sb.append(')');
		}
		
		return sb.toString();		
	}
	
	
	/**
	 * TODO: move the "parent != null"==>injar heuristic to more explicit 
	 */
	public String toLinkLabelString() {
		return toLinkLabelString(true);
	}

	public String toLinkLabelString(boolean getFullyQualifiedArgTypes) {
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
		label += toLabelString(getFullyQualifiedArgTypes);
		return label;
	}
	
	public String toLabelString() {
		return toLabelString(true);
	}

	public String toLabelString(boolean getFullyQualifiedArgTypes) {
		String label = toSignatureString(getFullyQualifiedArgTypes);
		String details = getDetails();
		if (details != null) {
			label += ": " + details;
		} 
		return label;
	}
	
	public String getHandleIdentifier() {
		return getHandleIdentifier(true);
	}
	
	public String getHandleIdentifier(boolean create) {
		if (null == handle && create) {
            handle = AsmManager.getDefault().getHandleProvider().createHandleIdentifier(this);			
		}
		return handle;
	}
	
	public void setHandleIdentifier(String handle) {
		this.handle = handle;
	}
	
	public List getParameterNames() { 
		List parameterNames = (List)kvpairs.get("parameterNames");
		return parameterNames; 
	}
	
	public void setParameterNames(List list) { 
		if (list==null || list.size()==0) return;
		if (kvpairs==Collections.EMPTY_MAP) kvpairs = new HashMap();
		kvpairs.put("parameterNames",list);
		//parameterNames = list; 
	}

	public List getParameterTypes() {
		List l = getParameterSignatures();
		if (l == null || l.isEmpty()) {
			return Collections.EMPTY_LIST;
		}
		List params = new ArrayList();
		for (Iterator iter = l.iterator(); iter.hasNext();) {
			char[] param = (char[])iter.next();
			params.add(NameConvertor.convertFromSignature(param));
		}
		return params;
	}
	
	public List getParameterSignatures() {
		List parameters = (List)kvpairs.get("parameterSigs");
		return parameters;
	}

	public void setParameterSignatures(List list) {
		if (kvpairs==Collections.EMPTY_MAP) kvpairs = new HashMap();
		if (list==null || list.size()==0) kvpairs.put("parameterSigs",Collections.EMPTY_LIST);
		else kvpairs.put("parameterSigs",list);
	}
	
	public String getDetails() {
		String details = (String)kvpairs.get("details");
		return details; 
	}
	public void setDetails(String string) { 
		if (kvpairs==Collections.EMPTY_MAP) kvpairs = new HashMap();
		kvpairs.put("details",string);
	}
	
	public void setFormalComment(String txt) { 
		if (txt!=null && txt.length()>0) {
			if (kvpairs==Collections.EMPTY_MAP) kvpairs = new HashMap();
			kvpairs.put("formalComment",txt);
		}
	}
	
	/** AMC added to speed up findByHandle lookups in AspectJElementHierarchy */
	private void cacheByHandle() {
		String handle = getHandleIdentifier();
		if (handle != null) {
			AspectJElementHierarchy hierarchy = (AspectJElementHierarchy) 
				AsmManager.getDefault().getHierarchy();
			hierarchy.cache(handle,this);
			//System.err.println("Cache size now "+hierarchy.handleMap.size());
		}
	}

	public void setExtraInfo(ExtraInformation info) { 
		if (kvpairs==Collections.EMPTY_MAP) kvpairs = new HashMap();
		kvpairs.put("ExtraInformation",info);
		}
	public ExtraInformation getExtraInfo() {
		return (ExtraInformation)kvpairs.get("ExtraInformation");
	}
}

