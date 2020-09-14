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

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.ISourceLocation;

/**
 * Represents program elements in the AspectJ containment hierarchy.
 *
 * @author Mik Kersten
 */
public interface IProgramElement extends Serializable {

	List<IProgramElement> getChildren();

	void setChildren(List<IProgramElement> children);

	void addChild(IProgramElement child);

	boolean removeChild(IProgramElement child);

	// Extra stuff
	// Could be just a string but may prove more useful as an object in the long
	// run ...
	class ExtraInformation implements Serializable {
		private static final long serialVersionUID = -3880735494840820638L;
		private String extraInfo;

		public ExtraInformation() {
			extraInfo = "";
		}

		public void setExtraAdviceInformation(String string) {
			extraInfo = string;
		}

		public String getExtraAdviceInformation() {
			return extraInfo;
		}

		public String toString() {
			return "ExtraInformation: [" + extraInfo + "]";
		}
	}

	void setExtraInfo(ExtraInformation info);

	ExtraInformation getExtraInfo();

	IProgramElement getParent();

	void setParent(IProgramElement parent);

	void setParentTypes(List<String> parentTypes);

	List<String> getParentTypes();

	String getName();

	void setName(String name);

	String getDetails();

	void setDetails(String details);

	IProgramElement.Kind getKind();

	void setKind(Kind kind);

	List<IProgramElement.Modifiers> getModifiers();

	void setModifiers(int i);

	Accessibility getAccessibility();

	String getDeclaringType(); // TODO: remove (Emacs uses it)

	String getPackageName();

	/**
	 * @param returnType
	 *            return types or field types
	 */
	void setCorrespondingType(String returnType);

	/**
	 * This correponds to both method return types and field types.
	 */
	String getCorrespondingType();

	String getCorrespondingType(boolean getFullyQualifiedType);

	String toSignatureString();

	String toSignatureString(boolean getFullyQualifiedArgTypes);

	void setRunnable(boolean value);

	boolean isRunnable();

	boolean isImplementor();

	void setImplementor(boolean value);

	boolean isOverrider();

	void setOverrider(boolean value);

	IMessage getMessage();

	void setMessage(IMessage message);

	ISourceLocation getSourceLocation();

	void setSourceLocation(ISourceLocation sourceLocation);

	String toString();

	/**
	 * @return the javadoc comment for this program element, null if not available
	 */
	String getFormalComment();

	void setFormalComment(String comment);

	/**
	 * Includes information about the origin of the node.
	 */
	String toLinkLabelString();

	String toLinkLabelString(boolean getFullyQualifiedArgTypes);

	/**
	 * Includes name, parameter types (if any) and details (if any).
	 */
	String toLabelString();

	String toLabelString(boolean getFullyQualifiedArgTypes);

	List<String> getParameterNames();

	void setParameterNames(List<String> list);

	List<char[]> getParameterSignatures();

	List<String> getParameterSignaturesSourceRefs();

	void setParameterSignatures(List<char[]> list, List<String> paramSourceRefs);

	List<char[]> getParameterTypes();

	/**
	 * The format of the string handle is not specified, but is stable across compilation sessions.
	 *
	 * @return a string representation of this element
	 */
	String getHandleIdentifier();

	String getHandleIdentifier(boolean create);

	void setHandleIdentifier(String handle);

	/**
	 * @return a string representation of this node and all of its children (recursive)
	 */
	String toLongString();

	String getBytecodeName();

	String getBytecodeSignature();

	void setBytecodeName(String bytecodeName);

	void setBytecodeSignature(String bytecodeSignature);

	/**
	 * @return the full signature of this element, as it appears in the source
	 */
	String getSourceSignature();

	void setSourceSignature(String string);

	IProgramElement walk(HierarchyWalker walker);

	AsmManager getModel();

	int getRawModifiers();

	/**
	 * Uses "typesafe enum" pattern.
	 */
	class Modifiers implements Serializable {

		private static final long serialVersionUID = -8279300899976607927L;

		public static final Modifiers STATIC = new Modifiers("static", 0x0008);
		public static final Modifiers FINAL = new Modifiers("final", 0x0010);
		public static final Modifiers ABSTRACT = new Modifiers("abstract", 0x0400);
		public static final Modifiers SYNCHRONIZED = new Modifiers("synchronized", 0x0020);
		public static final Modifiers VOLATILE = new Modifiers("volatile", 0x0040);
		public static final Modifiers STRICTFP = new Modifiers("strictfp", 0x0800);
		public static final Modifiers TRANSIENT = new Modifiers("transient", 0x0080);
		public static final Modifiers NATIVE = new Modifiers("native", 0x0100);
		public static final Modifiers[] ALL = { STATIC, FINAL, ABSTRACT, SYNCHRONIZED, VOLATILE, STRICTFP, TRANSIENT, NATIVE };
		private final String name;
		private final int bit;

		private Modifiers(String name, int bit) {
			this.name = name;
			this.bit = bit;
		}

		public String toString() {
			return name;
		}

		public int getBit() {
			return bit;
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
	class Accessibility implements Serializable {

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
	class Kind implements Serializable {

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
		public static final Kind DECLARE_PRECEDENCE = new Kind("declare precedence");
		public static final Kind CODE = new Kind("code");
		public static final Kind ERROR = new Kind("error");
		public static final Kind DECLARE_ANNOTATION_AT_CONSTRUCTOR = new Kind("declare @constructor");
		public static final Kind DECLARE_ANNOTATION_AT_FIELD = new Kind("declare @field");
		public static final Kind DECLARE_ANNOTATION_AT_METHOD = new Kind("declare @method");
		public static final Kind DECLARE_ANNOTATION_AT_TYPE = new Kind("declare @type");
		public static final Kind SOURCE_FOLDER = new Kind("source folder");
		public static final Kind PACKAGE_DECLARATION = new Kind("package declaration");

		public static final Kind[] ALL = { PROJECT, PACKAGE, FILE, FILE_JAVA, FILE_ASPECTJ, FILE_LST, IMPORT_REFERENCE, CLASS,
				INTERFACE, ASPECT, ENUM, ENUM_VALUE, ANNOTATION, INITIALIZER, INTER_TYPE_FIELD, INTER_TYPE_METHOD,
				INTER_TYPE_CONSTRUCTOR, INTER_TYPE_PARENT, CONSTRUCTOR, METHOD, FIELD, POINTCUT, ADVICE, DECLARE_PARENTS,
				DECLARE_WARNING, DECLARE_ERROR, DECLARE_SOFT, DECLARE_PRECEDENCE, CODE, ERROR, DECLARE_ANNOTATION_AT_CONSTRUCTOR,
				DECLARE_ANNOTATION_AT_FIELD, DECLARE_ANNOTATION_AT_METHOD, DECLARE_ANNOTATION_AT_TYPE, SOURCE_FOLDER,
				PACKAGE_DECLARATION

		};

		public static Kind getKindForString(String kindString) {
            for (Kind kind : ALL) {
                if (kind.toString().equals(kindString)) {
                    return kind;
                }
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

		public static List<Kind> getNonAJMemberKinds() {
			List<Kind> list = new ArrayList<>();
			list.add(METHOD);
			list.add(ENUM_VALUE);
			list.add(FIELD);
			list.add(CONSTRUCTOR);
			return list;
		}

		public boolean isMember() {
			return this == FIELD || this == METHOD || this == CONSTRUCTOR || this == POINTCUT || this == ADVICE
					|| this == ENUM_VALUE;
		}

		public boolean isInterTypeMember() {
			return this == INTER_TYPE_CONSTRUCTOR || this == INTER_TYPE_FIELD || this == INTER_TYPE_METHOD;
		}

		public boolean isType() {
			return this == CLASS || this == INTERFACE || this == ASPECT || this == ANNOTATION || this == ENUM;
		}

		public boolean isSourceFile() {
			return this == FILE_ASPECTJ || this == FILE_JAVA;
		}

		public boolean isFile() {
			return this == FILE;
		}

		public boolean isDeclare() {
			return name.startsWith("declare");
		}

		public boolean isDeclareAnnotation() {
			return name.startsWith("declare @");
		}

		public boolean isDeclareParents() {
			return name.startsWith("declare parents");
		}

		public boolean isDeclareSoft() {
			return name.startsWith("declare soft");
		}

		public boolean isDeclareWarning() {
			return name.startsWith("declare warning");
		}

		public boolean isDeclareError() {
			return name.startsWith("declare error");
		}

		public boolean isDeclarePrecedence() {
			return name.startsWith("declare precedence");
		}

		// The 4 declarations below are necessary for serialization
		private static int nextOrdinal = 0;
		private final int ordinal = nextOrdinal++;

		private Object readResolve() throws ObjectStreamException {
			return ALL[ordinal];
		}

		public boolean isPackageDeclaration() {
			return this == PACKAGE_DECLARATION;
		}

	}

	void setAnnotationStyleDeclaration(boolean b);

	boolean isAnnotationStyleDeclaration();

	/**
	 * @param fullyQualifiedannotationType
	 *            the annotation type, eg. p.q.r.Foo
	 */
	void setAnnotationType(String fullyQualifiedannotationType);

	/**
	 * @return the fully qualified annotation type, eg. p.q.r.Foo
	 */
	String getAnnotationType();

	String[] getRemovedAnnotationTypes();

	Map<String, List<String>> getDeclareParentsMap();

	void setDeclareParentsMap(Map<String, List<String>> newmap);

	void addFullyQualifiedName(String fqname);

	String getFullyQualifiedName();

	void setAnnotationRemover(boolean isRemover);

	boolean isAnnotationRemover();

	/**
	 * @return the return type of a method or type of a field in signature form (e.g. Ljava/lang/String;)
	 */
	String getCorrespondingTypeSignature();
}
