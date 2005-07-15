/* *******************************************************************
 * Copyright (c) 2002,2005 Contributors
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 *     Andy Clement  start of generics upgrade...
 *     Adrian Colyer - overhaul
 * ******************************************************************/

package org.aspectj.weaver;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.aspectj.apache.bcel.classfile.GenericSignatureParser;
import org.aspectj.apache.bcel.classfile.Signature;
import org.aspectj.apache.bcel.classfile.Signature.ClassSignature;

/**
 * A UnresolvedType represents a type to the weaver. It has a basic signature that knows 
 * nothing about type variables, type parameters, etc.. TypeXs are resolved in some World
 * (a repository of types). When a UnresolvedType is resolved it turns into a 
 * ResolvedType which may be a primitive type, an array type or a ReferenceType. 
 * ReferenceTypes may refer to simple, generic, parameterized or type-variable
 * based reference types. A ReferenceType is backed by a delegate that provides 
 * information about the type based on some repository (currently either BCEL
 * or an EclipseSourceType, but in the future we probably need to support
 * java.lang.reflect based delegates too). 
 * 
 * Every UnresolvedType has a signature, the unique key for the type in the world. 
 *
 * 
 * TypeXs are fully aware of their complete type information (there is no
 * erasure in the UnresolvedType world). To achieve this, the signature of TypeXs
 * combines the basic Java signature and the generic signature information
 * into one complete signature.
 * 
 * The format of a UnresolvedType signature is as follows:
 * 
 * a simple (non-generic, non-parameterized) type has the as its signature
 * the Java signature.
 * e.g. Ljava/lang/String;
 * 
 * a generic type has signature:
 * TypeParamsOpt ClassSig SuperClassSig SuperIntfListOpt
 * 
 * following the Generic signature grammar in the JVM spec., but with the
 * addition of the ClassSignature (which is not in the generic signature). In addition
 * type variable names are replaced by a simple number which represents their
 * declaration order in the type declaration.
 * 
 * e.g. public class Foo<T extends Number> would have signature:
 * <1:Ljava/lang/Number>Lorg.xyz.Foo;Ljava/lang/Object;
 * 
 * A parameterized type is a distinct type in the world with its own signature
 * following the grammar:
 * 
 * TypeParamsOpt ClassSig<ParamSigList>;
 * 
 * but with the L for the class sig replaced by "P". For example List<String> has
 * signature
 * 
 * Pjava/util/List<Ljava/lang/String>;
 * 
 * and List<T> in the following class :
 * class Foo<T> { List<T> lt; }
 * 
 * has signature:
 * <1:>Pjava/util/List<T1;>;
 * 
 * A typex that represents a type variable has its own unique signature,
 * following the grammar for a FormalTypeParameter in the JVM spec.
 * 
 * A generic typex has its true signature and also an erasure signature.
 * Both of these are keys pointing to the same UnresolvedType in the world. For example
 * List has signature:
 * 
 * <1:>Ljava/util/List;Ljava/lang/Object;
 * 
 * and the erasure signature
 * 
 * Ljava/util/List;
 * 
 * Generics wildcards introduce their own special signatures for type parameters.
 * The wildcard ? has signature *
 * The wildcard ? extends Foo has signature +LFoo;
 * The wildcard ? super Foo has signature -LFoo;
 */
public class UnresolvedType  {

	// common types referred to by the weaver
    public static final UnresolvedType[] NONE         = new UnresolvedType[0];
    public static final UnresolvedType   OBJECT       = forSignature("Ljava/lang/Object;");
    public static final UnresolvedType   OBJECTARRAY  = forSignature("[Ljava/lang/Object;");
    public static final UnresolvedType   CLONEABLE    = forSignature("Ljava/lang/Cloneable;");
    public static final UnresolvedType   SERIALIZABLE = forSignature("Ljava/io/Serializable;");
    public static final UnresolvedType   THROWABLE    = forSignature("Ljava/lang/Throwable;");
    public static final UnresolvedType   RUNTIME_EXCEPTION    = forSignature("Ljava/lang/RuntimeException;");
    public static final UnresolvedType   ERROR    = forSignature("Ljava/lang/Error;");    
    public static final UnresolvedType   AT_INHERITED = forSignature("Ljava/lang/annotation/Inherited;");
    public static final UnresolvedType   AT_RETENTION = forSignature("Ljava/lang/annotation/Retention;");
    public static final UnresolvedType   ENUM         = forSignature("Ljava/lang/Enum;");
    public static final UnresolvedType   ANNOTATION   = forSignature("Ljava/lang/annotation/Annotation;");
    public static final UnresolvedType   JAVA_LANG_CLASS = forSignature("Ljava/lang/Class;");
    public static final UnresolvedType   JAVA_LANG_EXCEPTION = forSignature("Ljava/lang/Exception;");
    public static final UnresolvedType   JAVA_LANG_REFLECT_METHOD = forSignature("Ljava/lang/reflect/Method;");
    public static final UnresolvedType   SUPPRESS_AJ_WARNINGS = forSignature("Lorg/aspectj/lang/annotation/SuppressAjWarnings;");
    public static final UnresolvedType   AT_TARGET = forSignature("Ljava/lang/annotation/Target;");

    // this doesn't belong here and will get moved to ResolvedType later in the refactoring
	public static final String MISSING_NAME = "<missing>";

    
    // constants indicating the base kind of the type
	// Note: It is not sufficient to say that a parameterized type with no type parameters in fact
	// represents a raw type - a parameterized type with no type parameters can represent
	// an inner type of a parameterized type that specifies no type parameters of its own.
	public final static int PRIMITIVE = 0;
	public final static int SIMPLE       =1;            	// a type with NO type parameters/vars
	public final static int RAW          =2;          	// the erasure of a generic type
	public final static int GENERIC      =3;        	// a generic type
	public final static int PARAMETERIZED=4;	 	// a parameterized type
	public final static int TYPE_VARIABLE = 5;    	// a type variable
	public final static int WILDCARD = 6; 			// a generic wildcard type

    protected int typeKind = SIMPLE; // what kind of type am I?

	/**
	 * THE SIGNATURE - see the comments above for how this is defined
	 */
    protected String signature;

    /**
     * The erasure of the signature. Contains only the Java signature of the type
     * with all supertype, superinterface, type variable, and parameter information
     * removed.
     */
    protected String signatureErasure;

    /**
     * Iff isParameterized(), then these are the type parameters 
     */
	protected UnresolvedType[]  typeParameters;

	/**
	 * Iff isGeneric(), then these are the type variables declared on the type
	 * Iff isParameterized(), then these are the type variables bound as parameters
	 * in the type 
	 */
	private TypeVariable[] typeVariables;

	   /**
     * Determines if this represents a primitive type.  A primitive type
     * is one of nine predefined resolved types.
     *
     * @return true iff this type represents a primitive type
     *
     * @see     ResolvedType#Boolean
     * @see     ResolvedType#Character
     * @see     ResolvedType#Byte
     * @see     ResolvedType#Short
     * @see     ResolvedType#Integer
     * @see     ResolvedType#Long
     * @see     ResolvedType#Float
     * @see     ResolvedType#Double
     * @see     ResolvedType#Void
     */   
    public boolean isPrimitiveType() { return typeKind == PRIMITIVE; }
    public boolean isSimpleType() { return typeKind == SIMPLE; }
    public boolean isRawType() { return typeKind == RAW; }
    public boolean isGenericType() { return typeKind == GENERIC; }
    public boolean isParameterizedType() { return typeKind == PARAMETERIZED; }
    public boolean isTypeVariable() { return typeKind == TYPE_VARIABLE; }
    public boolean isGenericWildcard() { return typeKind == WILDCARD; }

    // for any reference type, we can get some extra information...
    public final boolean isArray() {  return signature.startsWith("["); } 

    /** 
     * Equality is checked based on the underlying signature.
     * {@link ResolvedType} objects' equals is by reference.
     */
    public boolean equals(Object other) {
        if (! (other instanceof UnresolvedType)) return false;
        return signature.equals(((UnresolvedType) other).signature);
    }
    
    /** 
     * Equality is checked based on the underlying signature, so the hash code
     * of a particular type is the hash code of its signature string.
     */
    public final int hashCode() {
        return signature.hashCode();
    }
    
    /**
     * protected constructor for use only within UnresolvedType hierarchy. Use
     * one of the UnresolvedType.forXXX static methods for normal creation of
     * TypeXs.
     * Picks apart the signature string to set the type kind and calculates the
     * corresponding signatureErasure. A SIMPLE type created from a plain
     * Java signature may turn into a GENERIC type when it is resolved.
     * 
     * This method should never be called for a primitive type. (UnresolvedType. forSignature
     * deals with those).
     * 
     * @param signature in the form described in the class comment at the
     * top of this file. 
     */
//    protected UnresolvedType(String aSignature) {
//    	this.signature = aSignature;
//
//
//    }
    
    // -----------------------------
    // old stuff...
    
    
	/**
	 * For parameterized types, this is the signature of the raw type (e.g. Ljava/util/List; )
	 * For non-parameterized types, it is null.
	 */
	protected String rawTypeSignature;
	
	// For a generic type, this is the 'declared' signature
	// e.g. for Enum: <E:Ljava/lang/Enum<TE;>;>Ljava/lang/Object;Ljava/lang/Comparable<TE;>;Ljava/io/Serializable;
	// note: it doesnt include the name of the type!
	protected String genericSignature;
	
	/**
	 * @param      signature   the bytecode string representation of this Type
	 */
    protected UnresolvedType(String signature) {
        super();
        this.signature = signature;
		// avoid treating '<missing>' as a parameterized type ...
		if (signature.charAt(0)!='<' && signature.indexOf("<")!=-1 && !signature.startsWith("<missing>")) {
			// anglies alert - parameterized type
			processSignature(signature);
		}
    }
	
	/**
	 * Called when processing a parameterized type, sets the raw type for this typeX and calls a subroutine
	 * to handle sorting out the type parameters for the type.
	 */
	private void processSignature(String sig) {
		// determine the raw type
		//TODO asc generics tidy this bit up?
		boolean skip=false;
		if (sig.charAt(0)=='+') {/*isExtends=true;*/skip=true;}
		if (sig.charAt(0)=='-') {/*isSuper=true;*/skip=true;}
		int parameterTypesStart = signature.indexOf("<");
		int parameterTypesEnd   = signature.lastIndexOf(">");
		StringBuffer rawTypeSb = new StringBuffer();
		String p = signature.substring(0,parameterTypesStart);
		if (skip) p = p.substring(1);
		rawTypeSb.append(p).append(";");
		rawTypeSignature = rawTypeSb.toString();
		typeParameters = processParameterization(signature,parameterTypesStart+1,parameterTypesEnd-1);
		typeKind = PARAMETERIZED;
	}
	
	/**
	 * For a parameterized signature, e.g. <Ljava/lang/String;Ljava/util/List<Ljava/lang/String;>;>"
	 * this routine will return an appropriate array of TypeXs representing the top level type parameters.
	 * Where type parameters are themselves parameterized, we recurse.
	 */
	public UnresolvedType[] processParameterization(String paramSig,int startpos,int endpos) {
		boolean debug = false;
		if (debug) {
			StringBuffer sb = new StringBuffer();
			sb.append(paramSig).append("\n");
			for(int i=0;i<paramSig.length();i++) {
				if (i==startpos || i==endpos) sb.append("^");
				else if (i<startpos || i>endpos) sb.append(" ");
				else sb.append("-");
			}
			sb.append("\n");
			System.err.println(sb.toString());
		}
		int posn = startpos;
		List parameterTypes = new ArrayList();
		while (posn<endpos && paramSig.charAt(posn)!='>') {
			int nextAngly = paramSig.indexOf("<",posn);
			int nextSemi  = paramSig.indexOf(";",posn);
			if (nextAngly==-1 || nextSemi<nextAngly) { // the next type is not parameterized
				// simple type
				parameterTypes.add(UnresolvedType.forSignature(paramSig.substring(posn,nextSemi+1)));
				posn=nextSemi+1; // jump to the next type
			} else if (nextAngly!=-1 && nextSemi>nextAngly) {  // parameterized type, e.g. Ljava/util/Set<Ljava/util/String;>
				int count=1;
				int pos=nextAngly+1;
				for (;count>0;pos++){
					switch (paramSig.charAt(pos)) {
						case '<':count++;break;
						case '>':count--;break;
						default:
					}
				}
				String sub = paramSig.substring(posn,pos+1);
				parameterTypes.add(UnresolvedType.forSignature(sub));
				posn=pos+1;
			} else {
				throw new BCException("What the hell do i do with this? ["+paramSig.substring(posn)+"]");
			} 
		}
		return (UnresolvedType[])parameterTypes.toArray(new UnresolvedType[]{});
	}

    // ---- Things we can do without a world
    
    /**
     * This is the size of this type as used in JVM.
     */
    public int getSize() {
        return 1;
    }

    
    public static UnresolvedType makeArray(UnresolvedType base, int dims) {
    	StringBuffer sig = new StringBuffer();
    	for (int i=0; i < dims; i++) sig.append("[");
    	sig.append(base.getSignature());
    	return UnresolvedType.forSignature(sig.toString());
    }

    /**
     * NOTE: Use forSignature() if you can, it'll be cheaper !
     * Constructs a UnresolvedType for a java language type name.  For example:
     *
     * <blockquote><pre>
     *   UnresolvedType.forName("java.lang.Thread[]")
     *   UnresolvedType.forName("int")
     * </pre></blockquote>
     *
     * Types may equivalently be produced by this or by {@link #forSignature(String)}. 
     *
     * <blockquote><pre>
     *   UnresolvedType.forName("java.lang.Thread[]").equals(Type.forSignature("[Ljava/lang/Thread;")
     *   UnresolvedType.forName("int").equals(Type.forSignature("I"))
     * </pre></blockquote>
     * 
     * @param      name   the java language type name in question.
     * @return     a type object representing that java language type.
     */
    public static UnresolvedType forName(String name) {
        return forSignature(nameToSignature(name));
    }
	

    /** Constructs a UnresolvedType for each java language type name in an incoming array.
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
	
	
    public static UnresolvedType forGenericTypeSignature(String nameSig,String declaredGenericSig) {
    	UnresolvedType ret = UnresolvedType.forSignature(nameSig);
    	ret.typeKind=GENERIC;
    	
    	ClassSignature csig = new GenericSignatureParser().parseAsClassSignature(declaredGenericSig);
    	
    	Signature.FormalTypeParameter[] ftps = csig.formalTypeParameters;
    	ret.typeVariables = new TypeVariable[ftps.length];
    	for (int i = 0; i < ftps.length; i++) {
			Signature.FormalTypeParameter parameter = ftps[i];
			Signature.ClassTypeSignature cts = (Signature.ClassTypeSignature)parameter.classBound;
			ret.typeVariables[i]=new TypeVariable(ftps[i].identifier,UnresolvedType.forSignature(cts.outerType.identifier+";"));
		}
    	ret.rawTypeSignature = ret.signature;
    	ret.signature = ret.rawTypeSignature;
    	ret.genericSignature=declaredGenericSig;
    	return ret;
    }
    
	public static UnresolvedType forGenericType(String name,TypeVariable[] tvbs,String genericSig) { // TODO asc generics needs a declared sig
		UnresolvedType ret = UnresolvedType.forName(name);
		ret.typeKind=GENERIC;
		ret.typeVariables = tvbs;
		ret.rawTypeSignature = ret.signature;
		ret.genericSignature = genericSig;
		return ret;
	}
	
	/**
	 * Makes a parameterized type with the given name
	 * and parameterized type names.
	 */
    public static UnresolvedType forParameterizedTypeNames(String name, String[] paramTypeNames) {
		UnresolvedType[] paramTypes = null;
    	if (paramTypeNames!=null) {
			paramTypes = new UnresolvedType[paramTypeNames.length];
			for (int i = 0; i < paramTypeNames.length; i++) {
				paramTypes[i] = UnresolvedType.forName(paramTypeNames[i]);
			}
		}
    	return UnresolvedType.forParameterizedTypes(UnresolvedType.forName(name), paramTypes);
    }
    
    public static UnresolvedType forParameterizedTypes(UnresolvedType rawType, UnresolvedType[] paramTypes) {
		UnresolvedType ret = rawType;
		ret.typeKind=PARAMETERIZED;
		ret.typeParameters = paramTypes;
		ret.rawTypeSignature = ret.signature;
		// sig for e.g. List<String> is Ljava/util/List<Ljava/lang/String;>;
		if (ret.typeParameters!=null) {
			StringBuffer sigAddition = new StringBuffer();
			sigAddition.append("<");
			for (int i = 0; i < ret.typeParameters.length; i++) {
				sigAddition.append(ret.typeParameters[i].signature);			
			}
			sigAddition.append(">");
			sigAddition.append(";");
			ret.signature = ret.signature.substring(0,ret.signature.length()-1) + sigAddition.toString();
		}
		return ret;    	
    }
	
	public static UnresolvedType forRawTypeNames(String name) {
		UnresolvedType ret = UnresolvedType.forName(name);
		ret.typeKind = RAW;
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
     * Constructs a Type for a JVM bytecode signature string.  For example:
     *
     * <blockquote><pre>
     *   UnresolvedType.forSignature("[Ljava/lang/Thread;")
     *   UnresolvedType.forSignature("I");
     * </pre></blockquote>
     *
     * Types may equivalently be produced by this or by {@link #forName(String)}. 
     *
     * <blockquote><pre>
     *   UnresolvedType.forName("java.lang.Thread[]").equals(Type.forSignature("[Ljava/lang/Thread;")
     *   UnresolvedType.forName("int").equals(Type.forSignature("I"))
     * </pre></blockquote>
     * 
     * @param      signature the JVM bytecode signature string for the desired type.
     * @return     a type object represnting that JVM bytecode signature. 
     */
    public static UnresolvedType forSignature(String signature) {
        switch (signature.charAt(0)) {
            case 'B': return ResolvedType.BYTE;
            case 'C': return ResolvedType.CHAR;
            case 'D': return ResolvedType.DOUBLE;
            case 'F': return ResolvedType.FLOAT;
            case 'I': return ResolvedType.INT;
            case 'J': return ResolvedType.LONG;
            case 'L': return new UnresolvedType(signature);
            case 'S': return ResolvedType.SHORT;
            case 'V': return ResolvedType.VOID;
            case 'Z': return ResolvedType.BOOLEAN;
            case '[': return new UnresolvedType(signature);
            case '+': return new UnresolvedType(signature);
            case '-' : return new UnresolvedType(signature);
            case '?' : return GenericsWildcardTypeX.GENERIC_WILDCARD;
            default:  throw new BCException("Bad type signature " + signature);
        }      
    }
    
    /** Constructs a UnresolvedType for each JVM bytecode type signature in an incoming array.
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
     * Returns the name of this type in java language form.  For all 
     * UnresolvedType t:
     *
     * <blockquote><pre>
     *   UnresolvedType.forName(t.getName()).equals(t)
     * </pre></blockquote>
     *
     * and for all String s where s is a lexically valid java language typename:
     * 
     * <blockquote><pre>
     *   UnresolvedType.forName(s).getName().equals(s)
     * </pre></blockquote>
     * 
     * This produces a more esthetically pleasing string than 
     * {@link java.lang.Class#getName()}.
     *
     * @return  the java language name of this type.
     */
    public String getName() {
        return signatureToName(signature);
    }
    
    public String getRawName() {
    	return signatureToName((rawTypeSignature==null?signature:rawTypeSignature));
    }
	
	public String getBaseName() {
		String name = getName();
		if (isParameterizedType() || isGenericType()) {
			if (typeParameters==null) return name;
			else                      return name.substring(0,name.indexOf("<"));
		} else {
			return name;
		}
	}

    /**
     * Returns an array of strings representing the java langauge names of 
     * an array of types.
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
     * Returns the name of this type in JVM signature form.  For all 
     * UnresolvedType t:
     *
     * <blockquote><pre>
     *   UnresolvedType.forSignature(t.getSignature()).equals(t)
     * </pre></blockquote>
     *
     * and for all String s where s is a lexically valid JVM type signature string:
     * 
     * <blockquote><pre>
     *   UnresolvedType.forSignature(s).getSignature().equals(s)
     * </pre></blockquote>
     * 
     * @return  the java JVM signature string for this type.
     */
    public String getSignature() {
		return signature;
    }
	
	public String getParameterizedSignature() {
		return signature;
	}
	
	/**
	 * For parameterized types, return the signature for the raw type
	 */
	public String getRawTypeSignature() {
		if (rawTypeSignature==null) return signature;
		return rawTypeSignature;
	}
	
	public UnresolvedType getRawType() {
		return UnresolvedType.forSignature(getRawTypeSignature());
	}
	

 	
    /**
     * Returns a UnresolvedType object representing the effective outermost enclosing type
     * for a name type.  For all other types, this will return the type itself.
     * 
     * The only guarantee is given in JLS 13.1 where code generated according to
     * those rules will have type names that can be split apart in this way.
     * @return the outermost enclosing UnresolvedType object or this.
     */
    public UnresolvedType getOutermostType() {
    	if (isArray() || isPrimitiveType()) return this;
		String sig = getSignature();
		int dollar = sig.indexOf('$');
		if (dollar != -1) {
			return UnresolvedType.forSignature(sig.substring(0, dollar) + ';');
		} else {
			return this;
		}
    }

    /**
     * Returns a UnresolvedType object representing the component type of this array, or
     * null if this type does not represent an array type.
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
    public String toString() {
        return getName();
    }

    // ---- requires worlds

    /**
	 * Returns a resolved version of this type according to a particular world.
     * 
     * @param world thie {@link World} within which to resolve.
     * @return a resolved type representing this type in the appropriate world. 
	 */
	public ResolvedType resolve(World world) {
		return world.resolve(this);
	}

    // ---- helpers
    
    private static String signatureToName(String signature) {
        switch (signature.charAt(0)) {
            case 'B': return "byte";
            case 'C': return "char";
            case 'D': return "double";
            case 'F': return "float";
            case 'I': return "int";
            case 'J': return "long";
            case 'L':
                String name =  signature.substring(1, signature.length() - 1).replace('/', '.');
				if (name.indexOf("<") == -1) return name;
				// signature for parameterized types is e.g.
				// List<String> -> Ljava/util/List<Ljava/lang/String;>;
				// Map<String,List<Integer>> -> Ljava/util/Map<java/lang/String;Ljava/util/List<Ljava/lang/Integer;>;>;
				StringBuffer nameBuff = new StringBuffer();
				boolean justSeenLeftArrowChar = false;
				boolean justSeenTypeParameter = false;
				boolean justSeenSemiColon= false;
				int paramNestLevel = 0;
				for (int i = 0 ; i < name.length(); i++) {
					char c = name.charAt(i);
					switch (c) {
						case '<' : 
							justSeenLeftArrowChar = true;
							paramNestLevel++;
							nameBuff.append(c); 
							break;
						case ';' :
							justSeenSemiColon = true;
							break;
						case '>' :
							paramNestLevel--;
							nameBuff.append(c);
							break;
						case 'L' :
							if (justSeenLeftArrowChar) {
								justSeenLeftArrowChar = false;
								break;
							}
							if (justSeenSemiColon) {
								nameBuff.append(",");
							} else {
								nameBuff.append("L");
							}
							break;
						case 'T':
							if (justSeenLeftArrowChar) {
								justSeenLeftArrowChar = false;
								justSeenTypeParameter = true;
								break;
							}
							if (justSeenSemiColon) {
								nameBuff.append(",");
							} else {
								nameBuff.append("T");
							}
							justSeenTypeParameter = true;
							// type parameter
							break;
						default: 
							justSeenSemiColon = false;
							justSeenTypeParameter = false;
							justSeenLeftArrowChar = false;
							nameBuff.append(c);
					}
				}
				return nameBuff.toString();
            case 'S': return "short";
            case 'V': return "void";
            case 'Z': return "boolean";
            case '[':
                return signatureToName(signature.substring(1, signature.length())) + "[]";
//            case '<': 
//            	// its a generic!
 //           	if (signature.charAt(1)=='>') return signatureToName(signature.substring(2));
            case '+' : return signatureToName(signature.substring(1, signature.length()));
            case '-' : return signatureToName(signature.substring(1, signature.length()));
            default: 
                throw new BCException("Bad type signature: " + signature);
        }      
    }
    
    private static String nameToSignature(String name) {
        if (name.equals("byte")) return "B";
        if (name.equals("char")) return "C";
        if (name.equals("double")) return "D";
        if (name.equals("float")) return "F";
        if (name.equals("int")) return "I";
        if (name.equals("long")) return "J";
        if (name.equals("short")) return "S";
        if (name.equals("boolean")) return "Z";
        if (name.equals("void")) return "V";
        if (name.endsWith("[]")) 
            return "[" + nameToSignature(name.substring(0, name.length() - 2));
        if (name.length() != 0) {
        	// lots more tests could be made here...
        	
        	// 1) If it is already an array type, do not mess with it.
        	if (name.charAt(0)=='[' && name.charAt(name.length()-1)==';') return name;
        	else {
				if (name.indexOf("<") == -1) {
					// not parameterised
					return "L" + name.replace('.', '/') + ";";
				} else {
					StringBuffer nameBuff = new StringBuffer();
					nameBuff.append("L");
					for (int i = 0; i < name.length(); i++) {
						char c = name.charAt(i);
						switch (c) {
						case '.' : nameBuff.append('/'); break;
						case '<' : nameBuff.append("<L"); break;
						case '>' : nameBuff.append(";>"); break;
						case ',' : nameBuff.append(";L"); break;
						default: nameBuff.append(c);
						}
					}
					nameBuff.append(";");
					return nameBuff.toString();
				}
        	}
        }
        else 
            throw new BCException("Bad type name: " + name);
    }
    
	public void write(DataOutputStream s) throws IOException {
		s.writeUTF(signature);
	}
	
	public static UnresolvedType read(DataInputStream s) throws IOException {
		String sig = s.readUTF();
		if (sig.equals(MISSING_NAME)) {
			return ResolvedType.MISSING;
		} else {
			return UnresolvedType.forSignature(sig);
		}
	}
	
	public static void writeArray(UnresolvedType[] types, DataOutputStream s) throws IOException {
		int len = types.length;
		s.writeShort(len);
		for (int i=0; i < len; i++) {
			types[i].write(s);
		}
	}
	
	public static UnresolvedType[] readArray(DataInputStream s) throws IOException {
		int len = s.readShort();
		UnresolvedType[] types = new UnresolvedType[len];
		for (int i=0; i < len; i++) {
			types[i] = UnresolvedType.read(s);
		}
		return types;
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
	
	public String getPackageName() {
		String name = getName();
		int index = name.lastIndexOf('.');
		if (index == -1) { 
			return null; 
		} else {
			return name.substring(0, index);
		}
	}
	
	public UnresolvedType[] getTypeParameters() {
		return typeParameters == null ? new UnresolvedType[0] : typeParameters;
	}
	
	/**
	 * Doesn't include the package
	 */
	public String getClassName() {
		String name = getName();
		int index = name.lastIndexOf('.');
		if (index == -1) { 
			return name; 
		} else {
			return name.substring(index+1);
		}
	}
	
	public TypeVariable[] getTypeVariables() {
		return typeVariables;
	}




	public static UnresolvedType[] getInterfacesFromSignature(String sig) {
 		// there is a declared signature - use it to work out the interfaces, rather than the stuff in the class file...
		ClassSignature cSig = new GenericSignatureParser().parseAsClassSignature(sig);
		Signature.ClassTypeSignature[] declaredInterfaces = cSig.superInterfaceSignatures;
		UnresolvedType[] retVal = new UnresolvedType[declaredInterfaces.length];
		for (int i = 0; i < declaredInterfaces.length; i++) {
			Signature.ClassTypeSignature signature = declaredInterfaces[i];
			retVal[i] = convertFromClassSignatureToTypeX(signature);
		}
		return retVal;
	}
	
	private static UnresolvedType convertFromClassSignatureToTypeX(Signature.ClassTypeSignature signature) {
		return new UnresolvedType(signature.classSignature);
	}
	
		public String getKind() {
		switch (typeKind) {
			case 0: return "SIMPLE";
			case 1: return "RAW";
			case 2: return "GENERIC";
			case 3: return "PARAMETERIZED";
			default: return null;
		}		
	}
}

