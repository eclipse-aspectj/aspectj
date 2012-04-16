/* *******************************************************************
 * Copyright (c) 2002,2005 Contributors
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 *     Andy Clement  start of generics upgrade...
 *     Adrian Colyer - overhaul
 * ******************************************************************/

package org.aspectj.weaver;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Map;

import org.aspectj.util.GenericSignature;
import org.aspectj.util.GenericSignature.ClassSignature;
import org.aspectj.util.GenericSignatureParser;
import org.aspectj.weaver.tools.Traceable;

/**
 * A UnresolvedType represents a type to the weaver. UnresolvedTypes are resolved in some World (a type repository). When a
 * UnresolvedType is resolved it turns into a ResolvedType which may be a primitive type, or a ReferenceType. ReferenceTypes may
 * refer to simple, generic, parameterized or type-variable based reference types. A ReferenceType is backed by a delegate that
 * provides information about the type based on some repository (for example an Eclipse based delegate, a bytecode based delegate or
 * a reflection based delegate).
 * <p>
 * Every UnresolvedType has a signature, the unique key for the type in the world.
 */
public class UnresolvedType implements Traceable, TypeVariableDeclaringElement {

	// common type structures
	public static final UnresolvedType[] NONE = new UnresolvedType[0];
	public static final UnresolvedType OBJECT = forSignature("Ljava/lang/Object;");
	public static final UnresolvedType OBJECTARRAY = forSignature("[Ljava/lang/Object;");
	public static final UnresolvedType CLONEABLE = forSignature("Ljava/lang/Cloneable;");
	public static final UnresolvedType SERIALIZABLE = forSignature("Ljava/io/Serializable;");
	public static final UnresolvedType THROWABLE = forSignature("Ljava/lang/Throwable;");
	public static final UnresolvedType RUNTIME_EXCEPTION = forSignature("Ljava/lang/RuntimeException;");
	public static final UnresolvedType ERROR = forSignature("Ljava/lang/Error;");
	public static final UnresolvedType AT_INHERITED = forSignature("Ljava/lang/annotation/Inherited;");
	public static final UnresolvedType AT_RETENTION = forSignature("Ljava/lang/annotation/Retention;");
	public static final UnresolvedType ENUM = forSignature("Ljava/lang/Enum;");
	public static final UnresolvedType ANNOTATION = forSignature("Ljava/lang/annotation/Annotation;");
	public static final UnresolvedType JL_CLASS = forSignature("Ljava/lang/Class;");
	public static final UnresolvedType JAVA_LANG_CLASS_ARRAY = forSignature("[Ljava/lang/Class;");
	public static final UnresolvedType JL_STRING = forSignature("Ljava/lang/String;");
	public static final UnresolvedType JL_EXCEPTION = forSignature("Ljava/lang/Exception;");
	public static final UnresolvedType JAVA_LANG_REFLECT_METHOD = forSignature("Ljava/lang/reflect/Method;");
	public static final UnresolvedType JAVA_LANG_REFLECT_FIELD = forSignature("Ljava/lang/reflect/Field;");
	public static final UnresolvedType JAVA_LANG_REFLECT_CONSTRUCTOR = forSignature("Ljava/lang/reflect/Constructor;");
	public static final UnresolvedType JAVA_LANG_ANNOTATION = forSignature("Ljava/lang/annotation/Annotation;");
	public static final UnresolvedType SUPPRESS_AJ_WARNINGS = forSignature("Lorg/aspectj/lang/annotation/SuppressAjWarnings;");
	public static final UnresolvedType AT_TARGET = forSignature("Ljava/lang/annotation/Target;");
	public static final UnresolvedType SOMETHING = new UnresolvedType("?");
	public static final UnresolvedType[] ARRAY_WITH_JUST_OBJECT = new UnresolvedType[] { OBJECT };
	public static final UnresolvedType JOINPOINT_STATICPART = forSignature("Lorg/aspectj/lang/JoinPoint$StaticPart;");
	public static final UnresolvedType JOINPOINT_ENCLOSINGSTATICPART = forSignature("Lorg/aspectj/lang/JoinPoint$EnclosingStaticPart;");

	public static final UnresolvedType BOOLEAN = forPrimitiveType("Z");
	public static final UnresolvedType BYTE = forPrimitiveType("B");
	public static final UnresolvedType CHAR = forPrimitiveType("C");
	public static final UnresolvedType DOUBLE = forPrimitiveType("D");
	public static final UnresolvedType FLOAT = forPrimitiveType("F");
	public static final UnresolvedType INT = forPrimitiveType("I");
	public static final UnresolvedType LONG = forPrimitiveType("J");
	public static final UnresolvedType SHORT = forPrimitiveType("S");
	public static final UnresolvedType VOID = forPrimitiveType("V");

	// A type is considered missing if we have a signature for it but cannot find the delegate
	public static final String MISSING_NAME = "@missing@";

	// OPTIMIZE I dont think you can ask something unresolved what kind of type it is, how can it always know? Push down into
	// resolvedtype that will force references to resolvedtypes to be correct rather than relying on unresolvedtypes to answer
	// questions
	protected TypeKind typeKind = TypeKind.SIMPLE; // what kind of type am I?

	protected String signature;

	/**
	 * The erasure of the signature. Contains only the Java signature of the type with all supertype, superinterface, type variable,
	 * and parameter information removed.
	 */
	protected String signatureErasure;

	/**
	 * Calculated on first request - the package name (java.lang for type java.lang.String)
	 */
	private String packageName;

	/**
	 * Calculated on first request - the class name (String for type java.lang.String)
	 */
	private String className;

	/**
	 * Iff isParameterized(), then these are the type parameters
	 */
	protected UnresolvedType[] typeParameters;

	/**
	 * Iff isGeneric(), then these are the type variables declared on the type Iff isParameterized(), then these are the type
	 * variables bound as parameters in the type
	 */
	// OPTIMIZE should be no state in here that will damage whether equals() is correct...
	protected TypeVariable[] typeVariables;

	public boolean isPrimitiveType() {
		return typeKind == TypeKind.PRIMITIVE;
	}

	public boolean isVoid() {
		// OPTIMIZE promote to bitflag?
		return signature.equals("V");
	}

	public boolean isSimpleType() {
		return typeKind == TypeKind.SIMPLE;
	}

	public boolean isRawType() {
		return typeKind == TypeKind.RAW;
	}

	public boolean isGenericType() {
		return typeKind == TypeKind.GENERIC;
	}

	public boolean isParameterizedType() {
		return typeKind == TypeKind.PARAMETERIZED;
	}

	public boolean isParameterizedOrGenericType() {
		return typeKind == TypeKind.GENERIC || typeKind == TypeKind.PARAMETERIZED;
	}

	public boolean isParameterizedOrRawType() {
		return typeKind == TypeKind.PARAMETERIZED || typeKind == TypeKind.RAW;
	}

	public boolean isTypeVariableReference() {
		return typeKind == TypeKind.TYPE_VARIABLE;
	}

	public boolean isGenericWildcard() {
		return typeKind == TypeKind.WILDCARD;
	}

	public TypeKind getTypekind() {
		return typeKind;
	}

	// for any reference type, we can get some extra information...
	public final boolean isArray() {
		return signature.length() > 0 && signature.charAt(0) == '[';
	}

	/**
	 * Equality is checked based on the underlying signature.
	 */
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof UnresolvedType)) {
			return false;
		}
		return signature.equals(((UnresolvedType) other).signature);
	}

	/**
	 * Equality is checked based on the underlying signature, so the hash code of a particular type is the hash code of its
	 * signature string.
	 */
	@Override
	public int hashCode() {
		return signature.hashCode();
	}

	protected UnresolvedType(String signature) {
		this.signature = signature;
		this.signatureErasure = signature;
	}

	protected UnresolvedType(String signature, String signatureErasure) {
		this.signature = signature;
		this.signatureErasure = signatureErasure;
	}

	// called from TypeFactory
	public UnresolvedType(String signature, String signatureErasure, UnresolvedType[] typeParams) {
		this.signature = signature;
		this.signatureErasure = signatureErasure;
		this.typeParameters = typeParams;
		if (typeParams != null) {
			this.typeKind = TypeKind.PARAMETERIZED;
		}
	}

	// The operations supported by an UnresolvedType are those that do not require a world

	/**
	 * This is the size of this type as used in JVM.
	 */
	public int getSize() {
		return size;
	}

	private int size = 1;

	/**
	 * NOTE: Use forSignature() if you can, it'll be cheaper ! Constructs a UnresolvedType for a java language type name. For
	 * example:
	 * 
	 * <blockquote>
	 * 
	 * <pre>
	 *   UnresolvedType.forName(&quot;java.lang.Thread[]&quot;)
	 *   UnresolvedType.forName(&quot;int&quot;)
	 * </pre>
	 * 
	 * </blockquote>
	 * 
	 * Types may equivalently be produced by this or by {@link #forSignature(String)}.
	 * 
	 * <blockquote>
	 * 
	 * <pre>
	 *   UnresolvedType.forName(&quot;java.lang.Thread[]&quot;).equals(Type.forSignature(&quot;[Ljava/lang/Thread;&quot;)
	 *   UnresolvedType.forName(&quot;int&quot;).equals(Type.forSignature(&quot;I&quot;))
	 * </pre>
	 * 
	 * </blockquote>
	 * 
	 * @param name the java language type name in question.
	 * @return a type object representing that java language type.
	 */
	// OPTIMIZE change users of this to use forSignature, especially for simple cases
	public static UnresolvedType forName(String name) {
		return forSignature(nameToSignature(name));
	}

	/**
	 * Constructs a UnresolvedType for each java language type name in an incoming array.
	 * 
	 * @param names an array of java language type names.
	 * @return an array of UnresolvedType objects.
	 * @see #forName(String)
	 */
	public static UnresolvedType[] forNames(String[] names) {
		UnresolvedType[] ret = new UnresolvedType[names.length];
		for (int i = 0, len = names.length; i < len; i++) {
			ret[i] = UnresolvedType.forName(names[i]);
		}
		return ret;
	}

	public static UnresolvedType forGenericType(String name, TypeVariable[] tvbs, String genericSig) {
		// TODO asc generics needs a declared sig
		String sig = nameToSignature(name);
		UnresolvedType ret = UnresolvedType.forSignature(sig);
		ret.typeKind = TypeKind.GENERIC;
		ret.typeVariables = tvbs;
		ret.signatureErasure = sig;
		return ret;
	}

	public static UnresolvedType forGenericTypeSignature(String sig, String declaredGenericSig) {
		UnresolvedType ret = UnresolvedType.forSignature(sig);
		ret.typeKind = TypeKind.GENERIC;

		ClassSignature csig = new GenericSignatureParser().parseAsClassSignature(declaredGenericSig);

		GenericSignature.FormalTypeParameter[] ftps = csig.formalTypeParameters;
		ret.typeVariables = new TypeVariable[ftps.length];
		for (int i = 0; i < ftps.length; i++) {
			GenericSignature.FormalTypeParameter parameter = ftps[i];
			if (parameter.classBound instanceof GenericSignature.ClassTypeSignature) {
				GenericSignature.ClassTypeSignature cts = (GenericSignature.ClassTypeSignature) parameter.classBound;
				ret.typeVariables[i] = new TypeVariable(ftps[i].identifier, UnresolvedType.forSignature(cts.outerType.identifier
						+ ";"));
			} else if (parameter.classBound instanceof GenericSignature.TypeVariableSignature) {
				GenericSignature.TypeVariableSignature tvs = (GenericSignature.TypeVariableSignature) parameter.classBound;
				UnresolvedTypeVariableReferenceType utvrt = new UnresolvedTypeVariableReferenceType(new TypeVariable(
						tvs.typeVariableName));
				ret.typeVariables[i] = new TypeVariable(ftps[i].identifier, utvrt);
			} else {
				throw new BCException(
						"UnresolvedType.forGenericTypeSignature(): Do not know how to process type variable bound of type '"
								+ parameter.classBound.getClass() + "'.  Full signature is '" + sig + "'");
			}
		}
		ret.signatureErasure = sig;
		ret.signature = ret.signatureErasure;
		return ret;
	}

	public static UnresolvedType forGenericTypeVariables(String sig, TypeVariable[] tVars) {
		UnresolvedType ret = UnresolvedType.forSignature(sig);
		ret.typeKind = TypeKind.GENERIC;
		ret.typeVariables = tVars;
		ret.signatureErasure = sig;
		ret.signature = ret.signatureErasure;
		return ret;
	}

	public static UnresolvedType forRawTypeName(String name) {
		UnresolvedType ret = UnresolvedType.forName(name);
		ret.typeKind = TypeKind.RAW;
		return ret;
	}

	public static UnresolvedType forPrimitiveType(String signature) {
		UnresolvedType ret = new UnresolvedType(signature);
		ret.typeKind = TypeKind.PRIMITIVE;
		if (signature.equals("J") || signature.equals("D")) {
			ret.size = 2;
		} else if (signature.equals("V")) {
			ret.size = 0;
		}
		return ret;
	}

	/**
	 * Creates a new type array with a fresh type appended to the end.
	 * 
	 * @param types the left hand side of the new array
	 * @param end the right hand side of the new array
	 */
	public static UnresolvedType[] add(UnresolvedType[] types, UnresolvedType end) {
		int len = types.length;
		UnresolvedType[] ret = new UnresolvedType[len + 1];
		System.arraycopy(types, 0, ret, 0, len);
		ret[len] = end;
		return ret;
	}

	/**
	 * Creates a new type array with a fresh type inserted at the beginning.
	 * 
	 * 
	 * @param start the left hand side of the new array
	 * @param types the right hand side of the new array
	 */
	public static UnresolvedType[] insert(UnresolvedType start, UnresolvedType[] types) {
		int len = types.length;
		UnresolvedType[] ret = new UnresolvedType[len + 1];
		ret[0] = start;
		System.arraycopy(types, 0, ret, 1, len);
		return ret;
	}

	/**
	 * Constructs a Type for a JVM bytecode signature string. For example:
	 * 
	 * <blockquote>
	 * 
	 * <pre>
	 *   UnresolvedType.forSignature(&quot;[Ljava/lang/Thread;&quot;)
	 *   UnresolvedType.forSignature(&quot;I&quot;);
	 * </pre>
	 * 
	 * </blockquote>
	 * 
	 * Types may equivalently be produced by this or by {@link #forName(String)}. This method should not be passed P signatures.
	 * 
	 * <blockquote>
	 * 
	 * <pre>
	 *   UnresolvedType.forName(&quot;java.lang.Thread[]&quot;).equals(Type.forSignature(&quot;[Ljava/lang/Thread;&quot;)
	 *   UnresolvedType.forName(&quot;int&quot;).equals(Type.forSignature(&quot;I&quot;))
	 * </pre>
	 * 
	 * </blockquote>
	 * 
	 * @param signature the JVM bytecode signature string for the desired type.
	 * @return a type object represnting that JVM bytecode signature.
	 */
	public static UnresolvedType forSignature(String signature) {
		assert !(signature.startsWith("L") && signature.indexOf("<") != -1);
		switch (signature.charAt(0)) {
		case 'B':
			return UnresolvedType.BYTE;
		case 'C':
			return UnresolvedType.CHAR;
		case 'D':
			return UnresolvedType.DOUBLE;
		case 'F':
			return UnresolvedType.FLOAT;
		case 'I':
			return UnresolvedType.INT;
		case 'J':
			return UnresolvedType.LONG;
		case 'L':
			return TypeFactory.createTypeFromSignature(signature);
		case 'P':
			return TypeFactory.createTypeFromSignature(signature);
		case 'S':
			return UnresolvedType.SHORT;
		case 'V':
			return UnresolvedType.VOID;
		case 'Z':
			return UnresolvedType.BOOLEAN;
		case '[':
			return TypeFactory.createTypeFromSignature(signature);
		case '+':
			return TypeFactory.createTypeFromSignature(signature);
		case '-':
			return TypeFactory.createTypeFromSignature(signature);
		case '?':
			return TypeFactory.createTypeFromSignature(signature);
		case 'T':
			return TypeFactory.createTypeFromSignature(signature);
		default:
			throw new BCException("Bad type signature " + signature);
		}
	}

	/**
	 * Constructs a UnresolvedType for each JVM bytecode type signature in an incoming array.
	 * 
	 * @param names an array of JVM bytecode type signatures
	 * @return an array of UnresolvedType objects.
	 * @see #forSignature(String)
	 */
	public static UnresolvedType[] forSignatures(String[] sigs) {
		UnresolvedType[] ret = new UnresolvedType[sigs.length];
		for (int i = 0, len = sigs.length; i < len; i++) {
			ret[i] = UnresolvedType.forSignature(sigs[i]);
		}
		return ret;
	}

	/**
	 * Returns the name of this type in java language form (e.g. java.lang.Thread or boolean[]). This produces a more aesthetically
	 * pleasing string than {@link java.lang.Class#getName()}.
	 * 
	 * @return the java language name of this type.
	 */
	public String getName() {
		return signatureToName(signature);
	}

	public String getSimpleName() {
		String name = getRawName();
		int lastDot = name.lastIndexOf('.');
		if (lastDot != -1) {
			name = name.substring(lastDot + 1);
		}
		if (isParameterizedType()) {
			StringBuffer sb = new StringBuffer(name);
			sb.append("<");
			for (int i = 0; i < (typeParameters.length - 1); i++) {
				sb.append(typeParameters[i].getSimpleName());
				sb.append(",");
			}
			sb.append(typeParameters[typeParameters.length - 1].getSimpleName());
			sb.append(">");
			name = sb.toString();
		}
		return name;
	}

	public String getRawName() {
		return signatureToName((signatureErasure == null ? signature : signatureErasure));
	}

	public String getBaseName() {
		String name = getName();
		if (isParameterizedType() || isGenericType()) {
			if (typeParameters == null) {
				return name;
			} else {
				return name.substring(0, name.indexOf("<"));
			}
		} else {
			return name;
		}
	}

	public String getSimpleBaseName() {
		String name = getBaseName();
		int lastDot = name.lastIndexOf('.');
		if (lastDot != -1) {
			name = name.substring(lastDot + 1);
		}
		return name;
	}

	/**
	 * Returns an array of strings representing the java langauge names of an array of types.
	 * 
	 * @param types an array of UnresolvedType objects
	 * @return an array of Strings fo the java language names of types.
	 * @see #getName()
	 */
	public static String[] getNames(UnresolvedType[] types) {
		String[] ret = new String[types.length];
		for (int i = 0, len = types.length; i < len; i++) {
			ret[i] = types[i].getName();
		}
		return ret;
	}

	/**
	 * Returns the name of this type in JVM signature form. For all UnresolvedType t:
	 * 
	 * <blockquote>
	 * 
	 * <pre>
	 * UnresolvedType.forSignature(t.getSignature()).equals(t)
	 * </pre>
	 * 
	 * </blockquote>
	 * 
	 * and for all String s where s is a lexically valid JVM type signature string:
	 * 
	 * <blockquote>
	 * 
	 * <pre>
	 * UnresolvedType.forSignature(s).getSignature().equals(s)
	 * </pre>
	 * 
	 * </blockquote>
	 * 
	 * @return the java JVM signature string for this type.
	 */
	public String getSignature() {
		return signature;
	}

	/**
	 * For parameterized types, return the signature for the raw type
	 */
	public String getErasureSignature() {
		if (signatureErasure == null) {
			return signature;
		}
		return signatureErasure;
	}

	private boolean needsModifiableDelegate = false;

	public boolean needsModifiableDelegate() {
		return needsModifiableDelegate;
	}

	public void setNeedsModifiableDelegate(boolean b) {
		this.needsModifiableDelegate = b;
	}

	public UnresolvedType getRawType() {
		return UnresolvedType.forSignature(getErasureSignature());
	}

	/**
	 * Returns a UnresolvedType object representing the effective outermost enclosing type for a name type. For all other types,
	 * this will return the type itself.
	 * 
	 * The only guarantee is given in JLS 13.1 where code generated according to those rules will have type names that can be split
	 * apart in this way.
	 * 
	 * @return the outermost enclosing UnresolvedType object or this.
	 */
	public UnresolvedType getOutermostType() {
		if (isArray() || isPrimitiveType()) {
			return this;
		}
		String sig = getErasureSignature();
		int dollar = sig.indexOf('$');
		if (dollar != -1) {
			return UnresolvedType.forSignature(sig.substring(0, dollar) + ';');
		} else {
			return this;
		}
	}

	/**
	 * Returns a UnresolvedType object representing the component type of this array, or null if this type does not represent an
	 * array type.
	 * 
	 * @return the component UnresolvedType object, or null.
	 */
	public UnresolvedType getComponentType() {
		if (isArray()) {
			return forSignature(signature.substring(1));
		} else {
			return null;
		}
	}

	/**
	 * Returns a java language string representation of this type.
	 */
	@Override
	public String toString() {
		return getName(); // + " - " + getKind();
	}

	public String toDebugString() {
		return getName();
	}

	// ---- requires worlds

	/**
	 * Returns a resolved version of this type according to a particular world.
	 * 
	 * @param world the {@link World} within which to resolve.
	 * @return a resolved type representing this type in the appropriate world.
	 */
	public ResolvedType resolve(World world) {
		return world.resolve(this);
	}

	// ---- helpers

	private static String signatureToName(String signature) {
		switch (signature.charAt(0)) {
		case 'B':
			return "byte";
		case 'C':
			return "char";
		case 'D':
			return "double";
		case 'F':
			return "float";
		case 'I':
			return "int";
		case 'J':
			return "long";
		case 'L':
			String name = signature.substring(1, signature.length() - 1).replace('/', '.');
			return name;
		case 'T':
			StringBuffer nameBuff2 = new StringBuffer();
			int colon = signature.indexOf(";");
			String tvarName = signature.substring(1, colon);
			nameBuff2.append(tvarName);
			return nameBuff2.toString();
		case 'P': // it's one of our parameterized type sigs
			StringBuffer nameBuff = new StringBuffer();
			// signature for parameterized types is e.g.
			// List<String> -> Ljava/util/List<Ljava/lang/String;>;
			// Map<String,List<Integer>> -> Ljava/util/Map<java/lang/String;Ljava/util/List<Ljava/lang/Integer;>;>;
			int paramNestLevel = 0;
			for (int i = 1; i < signature.length(); i++) {
				char c = signature.charAt(i);
				switch (c) {
				case '/':
					nameBuff.append('.');
					break;
				case '<':
					nameBuff.append("<");
					paramNestLevel++;
					StringBuffer innerBuff = new StringBuffer();
					while (paramNestLevel > 0) {
						c = signature.charAt(++i);
						if (c == '<') {
							paramNestLevel++;
						}
						if (c == '>') {
							paramNestLevel--;
						}
						if (paramNestLevel > 0) {
							innerBuff.append(c);
						}
						if (c == ';' && paramNestLevel == 1) {
							nameBuff.append(signatureToName(innerBuff.toString()));
							if (signature.charAt(i + 1) != '>') {
								nameBuff.append(',');
							}
							innerBuff = new StringBuffer();
						}
					}
					nameBuff.append(">");
					break;
				case ';':
					break;
				default:
					nameBuff.append(c);
				}
			}
			return nameBuff.toString();
		case 'S':
			return "short";
		case 'V':
			return "void";
		case 'Z':
			return "boolean";
		case '[':
			return signatureToName(signature.substring(1, signature.length())) + "[]";
			// case '<':
			// // its a generic!
			// if (signature.charAt(1)=='>') return signatureToName(signature.substring(2));
		case '+':
			return "? extends " + signatureToName(signature.substring(1, signature.length()));
		case '-':
			return "? super " + signatureToName(signature.substring(1, signature.length()));
		case '*':
			return "?";
		default:
			throw new BCException("Bad type signature: " + signature);
		}
	}

	private static String nameToSignature(String name) {
		int len = name.length();
		if (len < 8) {
			if (name.equals("int")) {
				return "I";
			}
			if (name.equals("void")) {
				return "V";
			}
			if (name.equals("long")) {
				return "J";
			}
			if (name.equals("boolean")) {
				return "Z";
			}
			if (name.equals("double")) {
				return "D";
			}
			if (name.equals("float")) {
				return "F";
			}
			if (name.equals("byte")) {
				return "B";
			}
			if (name.equals("short")) {
				return "S";
			}
			if (name.equals("char")) {
				return "C";
			}
			if (name.equals("?")) {
				return name;
			}
		}
		if (len == 0) {
			throw new BCException("Bad type name: " + name);
		}
		if (name.endsWith("[]")) {
			return "[" + nameToSignature(name.substring(0, name.length() - 2));
		}
		
		// Sometimes the 'name' for an array is of the form: [Ljava.lang.String;
		if (name.charAt(0)=='[') {
			return name.replace('.','/');
		}

		if (name.indexOf("<") == -1) {
			// not parameterized
			return new StringBuilder("L").append(name.replace('.', '/')).append(';').toString();
		} else {
			StringBuffer nameBuff = new StringBuffer();
			int nestLevel = 0;
			nameBuff.append("P");
			for (int i = 0; i < len; i++) {
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
						} else if (c == '>') {
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
//				case '>':
//					throw new IllegalStateException("Should by matched by <");
//				case ',':
//					throw new IllegalStateException("Should only happen inside <...>");
				default:
					nameBuff.append(c);
				}
			}
			nameBuff.append(";");
			return nameBuff.toString();
		}
	}

	/**
	 * Write out an UnresolvedType - the signature should be enough.
	 */
	public final void write(CompressingDataOutputStream s) throws IOException {
		s.writeUTF(getSignature());
	}

	/**
	 * Read in an UnresolvedType - just read the signature and rebuild the UnresolvedType.
	 */
	public static UnresolvedType read(DataInputStream s) throws IOException {
		String sig = s.readUTF();
		if (sig.equals(MISSING_NAME)) {
			return ResolvedType.MISSING;
		} else {
			// TODO isn't it a shame to build these (this method is expensive) and then chuck them away on resolution?
			// TODO review callers and see if they are immediately resolving it, maybe we can do something more optimal if they are
			return UnresolvedType.forSignature(sig);
		}
	}

	public String getNameAsIdentifier() {
		return getName().replace('.', '_');
	}

	public String getPackageNameAsIdentifier() {
		String name = getName();
		int index = name.lastIndexOf('.');
		if (index == -1) {
			return "";
		} else {
			return name.substring(0, index).replace('.', '_');
		}
	}

	public UnresolvedType[] getTypeParameters() {
		return typeParameters == null ? UnresolvedType.NONE : typeParameters;
	}

	public TypeVariable[] getTypeVariables() {
		return typeVariables;
	}

	public static class TypeKind {
		// Note: It is not sufficient to say that a parameterized type with no type parameters in fact
		// represents a raw type - a parameterized type with no type parameters can represent
		// an inner type of a parameterized type that specifies no type parameters of its own.
		public final static TypeKind PRIMITIVE = new TypeKind("primitive");
		public final static TypeKind SIMPLE = new TypeKind("simple"); // a type with NO type parameters/vars
		public final static TypeKind RAW = new TypeKind("raw"); // the erasure of a generic type
		public final static TypeKind GENERIC = new TypeKind("generic"); // a generic type
		public final static TypeKind PARAMETERIZED = new TypeKind("parameterized"); // a parameterized type
		public final static TypeKind TYPE_VARIABLE = new TypeKind("type_variable"); // a type variable
		public final static TypeKind WILDCARD = new TypeKind("wildcard"); // a generic wildcard type

		@Override
		public String toString() {
			return type;
		}

		private TypeKind(String type) {
			this.type = type;
		}

		private final String type;
	}

	public TypeVariable getTypeVariableNamed(String name) {
		TypeVariable[] vars = getTypeVariables();
		if (vars == null || vars.length == 0) {
			return null;
		}
		for (int i = 0; i < vars.length; i++) {
			TypeVariable aVar = vars[i];
			if (aVar.getName().equals(name)) {
				return aVar;
			}
		}
		return null;
	}

	public String toTraceString() {
		return getClass().getName() + "[" + getName() + "]";
	}

	/**
	 * Return a version of this parameterized type in which any type parameters that are type variable references are replaced by
	 * their matching type variable binding.
	 */
	// OPTIMIZE methods like this just allow callers to be lazy and not ensure they are working with the right (resolved) subtype
	public UnresolvedType parameterize(Map<String, UnresolvedType> typeBindings) {
		throw new UnsupportedOperationException("unable to parameterize unresolved type: " + signature);
	}

	/**
	 * @return the class name (does not include the package name)
	 */
	public String getClassName() {
		if (className == null) {
			String name = getName();
			if (name.indexOf("<") != -1) {
				name = name.substring(0, name.indexOf("<"));
			}
			int index = name.lastIndexOf('.');
			if (index == -1) {
				className = name;
			} else {
				className = name.substring(index + 1);
			}
		}
		return className;
	}

	/**
	 * @return the package name (no class name included)
	 */
	public String getPackageName() {
		if (packageName == null) {
			String name = getName();
			if (name.indexOf("<") != -1) {
				name = name.substring(0, name.indexOf("<"));
			}
			int index = name.lastIndexOf('.');
			if (index == -1) {
				packageName = "";
			} else {
				packageName = name.substring(0, index);
			}
		}
		return packageName;
	}

	// TODO these move to a TypeUtils class

	public static void writeArray(UnresolvedType[] types, CompressingDataOutputStream stream) throws IOException {
		int len = types.length;
		stream.writeShort(len);
		for (UnresolvedType type : types) {
			type.write(stream);
		}
	}

	public static UnresolvedType[] readArray(DataInputStream s) throws IOException {
		int len = s.readShort();
		if (len == 0) {
			return UnresolvedType.NONE;
		}
		UnresolvedType[] types = new UnresolvedType[len];
		for (int i = 0; i < len; i++) {
			types[i] = UnresolvedType.read(s);
		}
		return types;
	}

	public static UnresolvedType makeArray(UnresolvedType base, int dims) {
		StringBuffer sig = new StringBuffer();
		for (int i = 0; i < dims; i++) {
			sig.append("[");
		}
		sig.append(base.getSignature());
		return UnresolvedType.forSignature(sig.toString());
	}
}
