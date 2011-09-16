/* *******************************************************************
 * Copyright (c) 2003,2010 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Mik Kersten     initial implementation 
 *     Andy Clement, IBM, SpringSource    Extensions for better IDE representation
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

/**
 * @author Mik Kersten
 * @author Andy Clement
 */
public class ProgramElement implements IProgramElement {

	public transient AsmManager asm; // which structure model is this node part of
	private static final long serialVersionUID = 171673495267384449L;
	public static boolean shortITDNames = true;

	private final static String UNDEFINED = "<undefined>";
	private final static int AccPublic = 0x0001;
	private final static int AccPrivate = 0x0002;
	private final static int AccProtected = 0x0004;
	private final static int AccPrivileged = 0x0006; // XXX is this right?
	private final static int AccStatic = 0x0008;
	private final static int AccFinal = 0x0010;
	private final static int AccSynchronized = 0x0020;
	private final static int AccVolatile = 0x0040;
	private final static int AccTransient = 0x0080;
	private final static int AccNative = 0x0100;
	// private final static int AccInterface = 0x0200;
	private final static int AccAbstract = 0x0400;
	// private final static int AccStrictfp = 0x0800;

	protected String name;
	private Kind kind;
	protected IProgramElement parent = null;
	protected List<IProgramElement> children = Collections.emptyList();
	public Map<String, Object> kvpairs = Collections.emptyMap();
	protected ISourceLocation sourceLocation = null;
	public int modifiers;
	private String handle = null;

	public AsmManager getModel() {
		return asm;
	}

	/** Used during deserialization */
	public ProgramElement() {
	}

	/** Use to create program element nodes that do not correspond to source locations */
	public ProgramElement(AsmManager asm, String name, Kind kind, List<IProgramElement> children) {
		this.asm = asm;
		if (asm == null && !name.equals("<build to view structure>")) {
			throw new RuntimeException();
		}
		this.name = name;
		this.kind = kind;
		if (children != null) {
			setChildren(children);
		}
	}

	public ProgramElement(AsmManager asm, String name, IProgramElement.Kind kind, ISourceLocation sourceLocation, int modifiers,
			String comment, List<IProgramElement> children) {
		this(asm, name, kind, children);
		this.sourceLocation = sourceLocation;
		setFormalComment(comment);
		// if (comment!=null && comment.length()>0) formalComment = comment;
		this.modifiers = modifiers;
	}

	public int getRawModifiers() {
		return this.modifiers;
	}

	public List<IProgramElement.Modifiers> getModifiers() {
		return genModifiers(this.modifiers);
	}

	public Accessibility getAccessibility() {
		return genAccessibility(this.modifiers);
	}

	public void setDeclaringType(String t) {
		if (t != null && t.length() > 0) {
			fixMap();
			kvpairs.put("declaringType", t);
		}
	}

	public String getDeclaringType() {
		String dt = (String) kvpairs.get("declaringType");
		if (dt == null) {
			return ""; // assumption that not having one means "" is at HtmlDecorator line 111
		}
		return dt;
	}

	public String getPackageName() {
		if (kind == Kind.PACKAGE) {
			return getName();
		}
		if (getParent() == null) {
			return "";
		}
		return getParent().getPackageName();
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
		// this.sourceLocation = sourceLocation;
	}

	public IMessage getMessage() {
		return (IMessage) kvpairs.get("message");
		// return message;
	}

	public void setMessage(IMessage message) {
		fixMap();
		kvpairs.put("message", message);
		// this.message = message;
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
		fixMap();
		if (value) {
			kvpairs.put("isRunnable", "true");
		} else {
			kvpairs.remove("isRunnable");
			// this.runnable = value;
		}
	}

	public boolean isRunnable() {
		return kvpairs.get("isRunnable") != null;
		// return runnable;
	}

	public boolean isImplementor() {
		return kvpairs.get("isImplementor") != null;
		// return implementor;
	}

	public void setImplementor(boolean value) {
		fixMap();
		if (value) {
			kvpairs.put("isImplementor", "true");
		} else {
			kvpairs.remove("isImplementor");
			// this.implementor = value;
		}
	}

	public boolean isOverrider() {
		return kvpairs.get("isOverrider") != null;
		// return overrider;
	}

	public void setOverrider(boolean value) {
		fixMap();
		if (value) {
			kvpairs.put("isOverrider", "true");
		} else {
			kvpairs.remove("isOverrider");
			// this.overrider = value;
		}
	}

	public String getFormalComment() {
		return (String) kvpairs.get("formalComment");
		// return formalComment;
	}

	public String toString() {
		return toLabelString();
	}

	private static List<IProgramElement.Modifiers> genModifiers(int modifiers) {
		List<IProgramElement.Modifiers> modifiersList = new ArrayList<IProgramElement.Modifiers>();
		if ((modifiers & AccStatic) != 0) {
			modifiersList.add(IProgramElement.Modifiers.STATIC);
		}
		if ((modifiers & AccFinal) != 0) {
			modifiersList.add(IProgramElement.Modifiers.FINAL);
		}
		if ((modifiers & AccSynchronized) != 0) {
			modifiersList.add(IProgramElement.Modifiers.SYNCHRONIZED);
		}
		if ((modifiers & AccVolatile) != 0) {
			modifiersList.add(IProgramElement.Modifiers.VOLATILE);
		}
		if ((modifiers & AccTransient) != 0) {
			modifiersList.add(IProgramElement.Modifiers.TRANSIENT);
		}
		if ((modifiers & AccNative) != 0) {
			modifiersList.add(IProgramElement.Modifiers.NATIVE);
		}
		if ((modifiers & AccAbstract) != 0) {
			modifiersList.add(IProgramElement.Modifiers.ABSTRACT);
		}
		return modifiersList;
	}

	public static IProgramElement.Accessibility genAccessibility(int modifiers) {
		if ((modifiers & AccPublic) != 0) {
			return IProgramElement.Accessibility.PUBLIC;
		}
		if ((modifiers & AccPrivate) != 0) {
			return IProgramElement.Accessibility.PRIVATE;
		}
		if ((modifiers & AccProtected) != 0) {
			return IProgramElement.Accessibility.PROTECTED;
		}
		if ((modifiers & AccPrivileged) != 0) {
			return IProgramElement.Accessibility.PRIVILEGED;
		} else {
			return IProgramElement.Accessibility.PACKAGE;
		}
	}

	public String getBytecodeName() {
		String s = (String) kvpairs.get("bytecodeName");
		if (s == null) {
			return UNDEFINED;
		}
		return s;
	}

	public void setBytecodeName(String s) {
		fixMap();
		kvpairs.put("bytecodeName", s);
	}

	public void setBytecodeSignature(String s) {
		fixMap();
		// Different kinds of format here. The one worth compressing starts with a '(':
		// (La/b/c/D;Le/f/g/G;)Ljava/lang/String;
		// maybe want to avoid generics initially.
		// boolean worthCompressing = s.charAt(0) == '(' && s.indexOf('<') == -1 && s.indexOf('P') == -1; // starts parentheses and
		// no
		// // generics
		// if (worthCompressing) {
		// kvpairs.put("bytecodeSignatureCompressed", asm.compress(s));
		// } else {
		kvpairs.put("bytecodeSignature", s);
		// }
	}

	public String getBytecodeSignature() {
		String s = (String) kvpairs.get("bytecodeSignature");
		// if (s == null) {
		// List compressed = (List) kvpairs.get("bytecodeSignatureCompressed");
		// if (compressed != null) {
		// return asm.decompress(compressed, '/');
		// }
		// }
		// if (s==null) return UNDEFINED;
		return s;
	}

	public String getSourceSignature() {
		return (String) kvpairs.get("sourceSignature");
	}

	public void setSourceSignature(String string) {
		fixMap();
		// System.err.println(name+" SourceSig=>"+string);
		kvpairs.put("sourceSignature", string);
		// sourceSignature = string;
	}

	public void setKind(Kind kind) {
		this.kind = kind;
	}

	public void setCorrespondingType(String s) {
		fixMap();
		kvpairs.put("returnType", s);
		// this.returnType = s;
	}

	public void setParentTypes(List<String> ps) {
		fixMap();
		kvpairs.put("parentTypes", ps);
	}

	@SuppressWarnings("unchecked")
	public List<String> getParentTypes() {
		return (List<String>) (kvpairs == null ? null : kvpairs.get("parentTypes"));
	}

	/**
	 * {@inheritDoc}
	 */
	public void setAnnotationType(String fullyQualifiedAnnotationType) {
		fixMap();
		kvpairs.put("annotationType", fullyQualifiedAnnotationType);
	}

	public void setAnnotationRemover(boolean isRemover) {
		fixMap();
		kvpairs.put("annotationRemover", isRemover);
	}

	public String getAnnotationType() {
		if (isAnnotationRemover()) {
			return null;
		}
		return (String) (kvpairs == null ? null : kvpairs.get("annotationType"));
	}

	public boolean isAnnotationRemover() {
		if (kvpairs == null) {
			return false;
		}
		Boolean b = (Boolean) kvpairs.get("annotationRemover");
		if (b == null) {
			return false;
		}
		return b.booleanValue();
	}

	public String[] getRemovedAnnotationTypes() {
		if (!isAnnotationRemover()) {
			return null;
		}
		String annotype = (String) (kvpairs == null ? null : kvpairs.get("annotationType"));
		if (annotype == null) {
			return null;
		} else {
			return new String[] { annotype };
		}
	}

	public String getCorrespondingType() {
		return getCorrespondingType(false);
	}

	public String getCorrespondingTypeSignature() {
		String typename = (String) kvpairs.get("returnType");
		if (typename == null) {
			return null;
		}
		return nameToSignature(typename);
	}

	public static String nameToSignature(String name) {
		int len = name.length();
		if (len < 8) {
			if (name.equals("byte")) {
				return "B";
			}
			if (name.equals("char")) {
				return "C";
			}
			if (name.equals("double")) {
				return "D";
			}
			if (name.equals("float")) {
				return "F";
			}
			if (name.equals("int")) {
				return "I";
			}
			if (name.equals("long")) {
				return "J";
			}
			if (name.equals("short")) {
				return "S";
			}
			if (name.equals("boolean")) {
				return "Z";
			}
			if (name.equals("void")) {
				return "V";
			}
			if (name.equals("?")) {
				return name;
			}
		}
		if (name.endsWith("[]")) {
			return "[" + nameToSignature(name.substring(0, name.length() - 2));
		}
		if (len != 0) {
			// check if someone is calling us with something that is a signature already
			assert name.charAt(0) != '[';

			if (name.indexOf("<") == -1) {
				// not parameterized
				return new StringBuilder("L").append(name.replace('.', '/')).append(';').toString();
			} else {
				StringBuffer nameBuff = new StringBuffer();
				int nestLevel = 0;
				nameBuff.append("L");
				for (int i = 0; i < name.length(); i++) {
					char c = name.charAt(i);
					switch (c) {
					case '.':
						nameBuff.append('/');
						break;
					case '<':
						nameBuff.append("<");
						nestLevel++;
						StringBuffer innerBuff = new StringBuffer();
						while (nestLevel > 0) {
							c = name.charAt(++i);
							if (c == '<') {
								nestLevel++;
							}
							if (c == '>') {
								nestLevel--;
							}
							if (c == ',' && nestLevel == 1) {
								nameBuff.append(nameToSignature(innerBuff.toString()));
								innerBuff = new StringBuffer();
							} else {
								if (nestLevel > 0) {
									innerBuff.append(c);
								}
							}
						}
						nameBuff.append(nameToSignature(innerBuff.toString()));
						nameBuff.append('>');
						break;
					case '>':
						throw new IllegalStateException("Should by matched by <");
					case ',':
						throw new IllegalStateException("Should only happen inside <...>");
					default:
						nameBuff.append(c);
					}
				}
				nameBuff.append(";");
				return nameBuff.toString();
			}
		} else {
			throw new IllegalArgumentException("Bad type name: " + name);
		}
	}

	public String getCorrespondingType(boolean getFullyQualifiedType) {
		String returnType = (String) kvpairs.get("returnType");
		if (returnType == null) {
			returnType = "";
		}
		if (getFullyQualifiedType) {
			return returnType;
		}
		return trim(returnType);
	}

	/**
	 * Trim down fully qualified types to their short form (e.g. a.b.c.D<e.f.G> becomes D<G>)
	 */
	public static String trim(String fqname) {
		int i = fqname.indexOf("<");
		if (i == -1) {
			int lastdot = fqname.lastIndexOf('.');
			if (lastdot == -1) {
				return fqname;
			} else {
				return fqname.substring(lastdot + 1);
			}
		}
		char[] charArray = fqname.toCharArray();
		StringBuilder candidate = new StringBuilder(charArray.length);
		StringBuilder complete = new StringBuilder(charArray.length);
		for (char c : charArray) {
			switch (c) {
			case '.':
				candidate.setLength(0);
				break;
			case '<':
			case ',':
			case '>':
				complete.append(candidate).append(c);
				candidate.setLength(0);
				break;
			default:
				candidate.append(c);
			}
		}
		complete.append(candidate);
		return complete.toString();
	}

	public String getName() {
		return name;
	}

	public List<IProgramElement> getChildren() {
		return children;
	}

	public void setChildren(List<IProgramElement> children) {
		this.children = children;
		if (children == null) {
			return;
		}
		for (Iterator<IProgramElement> it = children.iterator(); it.hasNext();) {
			(it.next()).setParent(this);
		}
	}

	public void addChild(IProgramElement child) {
		if (children == null || children == Collections.EMPTY_LIST) {
			children = new ArrayList<IProgramElement>();
		}
		children.add(child);
		child.setParent(this);
	}

	public void addChild(int position, IProgramElement child) {
		if (children == null || children == Collections.EMPTY_LIST) {
			children = new ArrayList<IProgramElement>();
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
		if (children != null) {
			for (IProgramElement child : children) {
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
				for (int i = 0; i < depth; i++) {
					buffer.append(' ');
				}
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

	/**
	 * Convenience mechanism for setting new modifiers which do not require knowledge of the private internal representation
	 * 
	 * @param newModifier
	 */
	public void addModifiers(IProgramElement.Modifiers newModifier) {
		modifiers |= newModifier.getBit();
	}

	public String toSignatureString() {
		return toSignatureString(true);
	}

	public String toSignatureString(boolean getFullyQualifiedArgTypes) {
		StringBuffer sb = new StringBuffer();
		sb.append(name);

		List<char[]> ptypes = getParameterTypes();
		if (ptypes != null && (!ptypes.isEmpty() || this.kind.equals(IProgramElement.Kind.METHOD))
				|| this.kind.equals(IProgramElement.Kind.CONSTRUCTOR) || this.kind.equals(IProgramElement.Kind.ADVICE)
				|| this.kind.equals(IProgramElement.Kind.POINTCUT) || this.kind.equals(IProgramElement.Kind.INTER_TYPE_METHOD)
				|| this.kind.equals(IProgramElement.Kind.INTER_TYPE_CONSTRUCTOR)) {
			sb.append('(');
			for (Iterator<char[]> it = ptypes.iterator(); it.hasNext();) {
				char[] arg = it.next();
				if (getFullyQualifiedArgTypes) {
					sb.append(arg);
				} else {
					int index = CharOperation.lastIndexOf('.', arg);
					if (index != -1) {
						sb.append(CharOperation.subarray(arg, index + 1, arg.length));
					} else {
						sb.append(arg);
					}
				}
				if (it.hasNext()) {
					sb.append(",");
				}
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
				label = "";
			} else {
				int dotIndex = name.indexOf('.');
				if (dotIndex != -1) {
					return parent.getName() + ": " + toLabelString().substring(dotIndex + 1);
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
		String h = handle;
		if (null == handle && create) {
			if (asm == null && name.equals("<build to view structure>")) {
				h = "<build to view structure>";
			} else {
				try {
					h = asm.getHandleProvider().createHandleIdentifier(this);
				} catch (ArrayIndexOutOfBoundsException aioobe) {
					throw new RuntimeException("AIOOBE whilst building handle for " + this, aioobe);
				}
			}
		}
		setHandleIdentifier(h);
		return h;
	}

	public void setHandleIdentifier(String handle) {
		this.handle = handle;
	}

	@SuppressWarnings("unchecked")
	public List<String> getParameterNames() {
		List<String> parameterNames = (List<String>) kvpairs.get("parameterNames");
		return parameterNames;
	}

	public void setParameterNames(List<String> list) {
		if (list == null || list.size() == 0) {
			return;
		}
		fixMap();
		kvpairs.put("parameterNames", list);
		// parameterNames = list;
	}

	public List<char[]> getParameterTypes() {
		List<char[]> l = getParameterSignatures();
		if (l == null || l.isEmpty()) {
			return Collections.emptyList();
		}
		List<char[]> params = new ArrayList<char[]>();
		for (Iterator<char[]> iter = l.iterator(); iter.hasNext();) {
			char[] param = iter.next();
			params.add(NameConvertor.convertFromSignature(param));
		}
		return params;
	}

	@SuppressWarnings("unchecked")
	public List<char[]> getParameterSignatures() {
		List<char[]> parameters = (List<char[]>) kvpairs.get("parameterSigs");
		return parameters;
	}

	@SuppressWarnings("unchecked")
	public List<String> getParameterSignaturesSourceRefs() {
		List<String> parameters = (List<String>) kvpairs.get("parameterSigsSourceRefs");
		return parameters;
	}

	/**
	 * Set the parameter signatures for this method/constructor. The bit flags tell us if any were not singletypereferences in the
	 * the source. A singletypereference would be 'String' - whilst a qualifiedtypereference would be 'java.lang.String' - this has
	 * an effect on the handles.
	 */
	public void setParameterSignatures(List<char[]> list, List<String> sourceRefs) {
		fixMap();
		if (list == null || list.size() == 0) {
			kvpairs.put("parameterSigs", Collections.EMPTY_LIST);
		} else {
			kvpairs.put("parameterSigs", list);
		}
		if (sourceRefs != null && sourceRefs.size() != 0) {
			kvpairs.put("parameterSigsSourceRefs", sourceRefs);
		}
	}

	public String getDetails() {
		String details = (String) kvpairs.get("details");
		return details;
	}

	public void setDetails(String string) {
		fixMap();
		kvpairs.put("details", string);
	}

	public void setFormalComment(String txt) {
		if (txt != null && txt.length() > 0) {
			fixMap();
			kvpairs.put("formalComment", txt);
		}
	}

	private void fixMap() {
		if (kvpairs == Collections.EMPTY_MAP) {
			kvpairs = new HashMap<String, Object>();
		}
	}

	public void setExtraInfo(ExtraInformation info) {
		fixMap();
		kvpairs.put("ExtraInformation", info);
	}

	public ExtraInformation getExtraInfo() {
		return (ExtraInformation) kvpairs.get("ExtraInformation");
	}

	public boolean isAnnotationStyleDeclaration() {
		return kvpairs.get("annotationStyleDeclaration") != null;
	}

	public void setAnnotationStyleDeclaration(boolean b) {
		if (b) {
			fixMap();
			kvpairs.put("annotationStyleDeclaration", "true");
		}
	}

	public Map<String, List<String>> getDeclareParentsMap() {
		Map<String, List<String>> s = (Map<String, List<String>>) kvpairs.get("declareparentsmap");
		return s;
	}

	public void setDeclareParentsMap(Map<String, List<String>> newmap) {
		fixMap();
		kvpairs.put("declareparentsmap", newmap);
	}

	public void addFullyQualifiedName(String fqname) {
		fixMap();
		kvpairs.put("itdfqname", fqname);
	}

	public String getFullyQualifiedName() {
		return (String) kvpairs.get("itdfqname");
	}
}
