/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.weaver;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Iterator;

public class TypeX {
	/**
	 * This is the bytecode string representation of this Type
	 */
    protected String signature;

	/**
	 * @param      signature   the bytecode string representation of this Type
	 */
    protected TypeX(String signature) {
        super();
        this.signature = signature;
    }

    // ---- Things we can do without a world
    
    /**
     * This is the size of this type as used in JVM.
     */
    public int getSize() {
        return 1;
    }

    /** 
     * Equality is checked based on the underlying signature.
     * {@link ResolvedType} objects' equals is by reference.
     */
    public boolean equals(Object other) {
        if (! (other instanceof TypeX)) return false;
        return signature.equals(((TypeX) other).signature);
    }
    
    /** 
     * Equality is checked based on the underlying signature, so the hash code
     * of a particular type is the hash code of its signature string.
     */
    public final int hashCode() {
        return signature.hashCode();
    }
    
    public static TypeX makeArray(TypeX base, int dims) {
    	StringBuffer sig = new StringBuffer();
    	for (int i=0; i < dims; i++) sig.append("[");
    	sig.append(base.getSignature());
    	return TypeX.forSignature(sig.toString());
    }

    /**
     * Constructs a TypeX for a java language type name.  For example:
     *
     * <blockquote><pre>
     *   TypeX.forName("java.lang.Thread[]")
     *   TypeX.forName("int")
     * </pre></blockquote>
     *
     * Types may equivalently be produced by this or by {@link #forSignature(String)}. 
     *
     * <blockquote><pre>
     *   TypeX.forName("java.lang.Thread[]").equals(Type.forSignature("[Ljava/lang/Thread;")
     *   TypeX.forName("int").equals(Type.forSignature("I"))
     * </pre></blockquote>
     * 
     * @param      name   the java language type name in question.
     * @return     a type object representing that java language type.
     */
    public static TypeX forName(String name) {
        return forSignature(nameToSignature(name));
    }

    /** Constructs a TypeX for each java language type name in an incoming array.
     * 
     * @param names an array of java language type names.
     * @return an array of TypeX objects.
     * @see #forName(String)
     */
    public static TypeX[] forNames(String[] names) {
        TypeX[] ret = new TypeX[names.length];
        for (int i = 0, len = names.length; i < len; i++) {
            ret[i] = TypeX.forName(names[i]);
        }
        return ret;
    }  
    
	/**
	 * Creates a new type array with a fresh type appended to the end.
	 * 
	 * @param types the left hand side of the new array
	 * @param end the right hand side of the new array
	 */
    public static TypeX[] add(TypeX[] types, TypeX end) {
		int len = types.length;
		TypeX[] ret = new TypeX[len + 1];
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
    public static TypeX[] insert(TypeX start, TypeX[] types) {
		int len = types.length;
		TypeX[] ret = new TypeX[len + 1];
		ret[0] = start;
		System.arraycopy(types, 0, ret, 1, len);
		return ret;
    }    

    /**
     * Constructs a Type for a JVM bytecode signature string.  For example:
     *
     * <blockquote><pre>
     *   TypeX.forSignature("[Ljava/lang/Thread;")
     *   TypeX.forSignature("I");
     * </pre></blockquote>
     *
     * Types may equivalently be produced by this or by {@link #forName(String)}. 
     *
     * <blockquote><pre>
     *   TypeX.forName("java.lang.Thread[]").equals(Type.forSignature("[Ljava/lang/Thread;")
     *   TypeX.forName("int").equals(Type.forSignature("I"))
     * </pre></blockquote>
     * 
     * @param      signature the JVM bytecode signature string for the desired type.
     * @return     a type object represnting that JVM bytecode signature. 
     */
    public static TypeX forSignature(String signature) {
        switch (signature.charAt(0)) {
            case 'B': return ResolvedTypeX.BYTE;
            case 'C': return ResolvedTypeX.CHAR;
            case 'D': return ResolvedTypeX.DOUBLE;
            case 'F': return ResolvedTypeX.FLOAT;
            case 'I': return ResolvedTypeX.INT;
            case 'J': return ResolvedTypeX.LONG;
            case 'L':
                return new TypeX(signature);
            case 'S': return ResolvedTypeX.SHORT;
            case 'V': return ResolvedTypeX.VOID;
            case 'Z': return ResolvedTypeX.BOOLEAN;
            case '[':
                return new TypeX(signature);
            default: 
                throw new BCException("Bad type signature " + signature);
        }      
    }
    
    /** Constructs a TypeX for each JVM bytecode type signature in an incoming array.
     * 
     * @param names an array of JVM bytecode type signatures
     * @return an array of TypeX objects.
     * @see #forSignature(String)
     */
    public static TypeX[] forSignatures(String[] sigs) {
        TypeX[] ret = new TypeX[sigs.length];
        for (int i = 0, len = sigs.length; i < len; i++) {
            ret[i] = TypeX.forSignature(sigs[i]);
        }
        return ret;
    }  

    /**
     * Returns the name of this type in java language form.  For all 
     * TypeX t:
     *
     * <blockquote><pre>
     *   TypeX.forName(t.getName()).equals(t)
     * </pre></blockquote>
     *
     * and for all String s where s is a lexically valid java language typename:
     * 
     * <blockquote><pre>
     *   TypeX.forName(s).getName().equals(s)
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

    /**
     * Returns an array of strings representing the java langauge names of 
     * an array of types.
     *
     * @param types an array of TypeX objects
     * @return an array of Strings fo the java language names of types.
     * @see #getName()
     */
    public static String[] getNames(TypeX[] types) {
        String[] ret = new String[types.length];
        for (int i = 0, len = types.length; i < len; i++) {
            ret[i] = types[i].getName();
        }
        return ret;
    } 
    
    /**
     * Returns the name of this type in JVM signature form.  For all 
     * TypeX t:
     *
     * <blockquote><pre>
     *   TypeX.forSignature(t.getSignature()).equals(t)
     * </pre></blockquote>
     *
     * and for all String s where s is a lexically valid JVM type signature string:
     * 
     * <blockquote><pre>
     *   TypeX.forSignature(s).getSignature().equals(s)
     * </pre></blockquote>
     * 
     * @return  the java JVM signature string for this type.
     */
    public String getSignature() {
        return signature;
    }

    /**
     * Determins if this represents an array type.
     *
     * @return  true iff this represents an array type.
     */
    public final boolean isArray() {
        return signature.startsWith("[");
    }
    
    /**
     * Returns a TypeX object representing the effective outermost enclosing type
     * for a name type.  For all other types, this will return the type itself.
     * 
     * The only guarantee is given in JLS 13.1 where code generated according to
     * those rules will have type names that can be split apart in this way.
     * @return the outermost enclosing TypeX object or this.
     */
    public TypeX getOutermostType() {
    	if (isArray() || isPrimitive()) return this;
		String sig = getSignature();
		int dollar = sig.indexOf('$');
		if (dollar != -1) {
			return TypeX.forSignature(sig.substring(0, dollar) + ';');
		} else {
			return this;
		}
    }

    /**
     * Returns a TypeX object representing the component type of this array, or
     * null if this type does not represent an array type.
     *
     * @return the component TypeX object, or null.
     */
    public TypeX getComponentType() {
        if (isArray()) {
            return forSignature(signature.substring(1));
        } else {
            return null;
        }
    }

    /**
     * Determines if this represents a primitive type.  A primitive type
     * is one of nine predefined resolved types.
     *
     * @return true iff this type represents a primitive type
     *
     * @see     ResolvedTypeX#Boolean
     * @see     ResolvedTypeX#Character
     * @see     ResolvedTypeX#Byte
     * @see     ResolvedTypeX#Short
     * @see     ResolvedTypeX#Integer
     * @see     ResolvedTypeX#Long
     * @see     ResolvedTypeX#Float
     * @see     ResolvedTypeX#Double
     * @see     ResolvedTypeX#Void
     */
    public boolean isPrimitive() {
        return false;
    }

    
    /** 
     * Returns a java language string representation of this type.
     */
    public String toString() {
        return getName();
    }

    // ---- requires worlds

    /**
     * Types may have pointcuts just as they have methods and fields.
     */
    public ResolvedPointcutDefinition findPointcut(String name, World world) {
        return world.findPointcut(this, name);
    }

   /**
     * Determines if variables of this type could be assigned values of another
     * with lots of help.  This is the same as isCoercableFrom, but java.lang.Object
     * is convertable from and to all types. 
     * 
     * @param other the other type
     * @param world the {@link World} in which the possible assignment should be checked.
     * @return true iff variables of this type could be assigned values of other with possible conversion
     */    
    public final boolean isConvertableFrom(TypeX other, World world) {
        if (this.equals(OBJECT) || other.equals(OBJECT)) return true;
        return this.isCoerceableFrom(other, world);
    }

    /**
     * Determines if variables of this type could be assigned values of another
     * without any conversion computation of any kind.  For primitive types
     * this means equality, and for reference types it means assignability.
     * 
     * @param other the other type
     * @param world the {@link World} in which the possible assignment should be checked.
     * @return true iff variables of this type could be assigned values of other without any conversion computation
     */
    public boolean needsNoConversionFrom(TypeX other, World world) {
		// primitives override this method, so we know we're not primitive.
		// So if the other is primitive, don't bother asking the world anything.
		if (other.isPrimitive()) return false;
        return world.needsNoConversionFrom(this, other);
    }

    /**
     * Determines if the variables of this type could be assigned values
     * of another type without casting.  This still allows for assignment conversion
     * as per JLS 2ed 5.2.
     * 
     * @param other the other type
     * @param world the {@link World} in which the possible assignment should be checked.
     * @return true iff variables of this type could be assigned values of other without casting
     * @exception NullPointerException if other is null
     */
    public boolean isAssignableFrom(TypeX other, World world) {
		// primitives override this method, so we know we're not primitive.
		// So if the other is primitive, don't bother asking the world anything.
		if (other.isPrimitive()) return false;
        return world.isAssignableFrom(this, other);
    }

    /**
     * Determines if values of another type could possibly be cast to
     * this type.  The rules followed are from JLS 2ed 5.5, "Casting Conversion".
     *   
     * <p> This method should be commutative, i.e., for all TypeX a, b and all World w:
     * 
     * <blockquote><pre>
     *    a.isCoerceableFrom(b, w) == b.isCoerceableFrom(a, w)
     * </pre></blockquote>
     *
     * @param other the other type
     * @param world the {@link World} in which the possible coersion should be checked.
     * @return true iff values of other could possibly be cast to this type. 
     * @exception NullPointerException if other is null.
     */
    public boolean isCoerceableFrom(TypeX other, World world) {
		// primitives override this method, so we know we're not primitive.
		// So if the other is primitive, don't bother asking the world anything.
		if (other.isPrimitive()) return false;
        return world.isCoerceableFrom(this, other);
    }

    /**
     * Determines if this represents an interface type.
     * 
     * @param world the {@link World} in which we should check.
     * @return  true iff this represents an interface type.
     */
    public final boolean isInterface(World world) {
    	return world.resolve(this).isInterface();
    }

    /**
     * Determines if this represents a class type.
     * 
     * @param world the {@link World} in which we should check.
     * @return  true iff this represents a class type.
     */
    public final boolean isClass(World world) {
        return world.resolve(this).isClass();
    }


    /**
     * Determines if this represents an aspect type.
     * 
     * @param world the {@link World} in which we should check.
     * @return  true iff this represents an aspect type.
     */
    public final boolean isAspect(World world) {
        return world.resolve(this).isAspect();
    }


    /**
     * Returns a TypeX object representing the superclass of this type, or null.
     * If this represents a java.lang.Object, a primitive type, or void, this
     * method returns null.  
     *
     * <p>
     * This differs from {@link java.lang.class#getSuperclass()} in that 
     * this returns a TypeX object representing java.lang.Object for interfaces instead
     * of returning null.
     * 
     * @param world the {@link World} in which the lookup should be made.
     * @return this type's superclass, or null if none exists.
     */
    public TypeX getSuperclass(World world) {
        return world.getSuperclass(this);
    }

    /**
     * Returns an array of TypeX objects representing the declared interfaces
     * of this type. 
     * 
     * <p>
     * If this object represents a class, the declared interfaces are those it
     * implements.  If this object represents an interface, the declared interfaces
     * are those it extends.  If this object represents a primitive, an empty
     * array is returned.  If this object represents an array, an array
     * containing types for java.lang.Cloneable and java.io.Serializable is returned.
     *
     * @param world the {@link World} in which the lookup should be made.
     * @return an iterator through the declared interfaces of this type.
     */
    public TypeX[] getDeclaredInterfaces(World world) {
        return world.getDeclaredInterfaces(this);
    }

    /**
     * Returns an iterator through TypeX objects representing all the direct
     * supertypes of this type.  That is, through the superclass, if any, and
     * all declared interfaces.
     *
     * @param world the {@link World} in which the lookup should be made.
     * @return an iterator through the direct supertypes of this type.
     */
    public Iterator getDirectSupertypes(World world) {
        return world.resolve(this).getDirectSupertypes();
    }

    /**
     * Returns the modifiers for this type.  
     * 
     * See {@link java.lang.Class#getModifiers()} for a description
     * of the weirdness of this methods on primitives and arrays.
     *
     * @param world the {@link World} in which the lookup is made.
     * @return an int representing the modifiers for this type
     * @see     java.lang.reflect.Modifier
     */
    public int getModifiers(World world) {
        return world.getModifiers(this);
    }

    /**
     * Returns an array representing the declared fields of this object.  This may include
     * non-user-visible fields.
     * This method returns an
     * empty array if it represents an array type or a primitive type, so
     * the implicit length field of arrays is just that, implicit.
     *
     * @param world the {@link World} in which the lookup is done.
     * @return the array representing the declared fields of this type
     */
    public ResolvedMember[] getDeclaredFields(World world) {
        return world.getDeclaredFields(this);
    }

    /**
     * Returns an array representing the declared methods of this object.  This includes
     * constructors and the static initialzation method.  This also includes all
     * shadowMungers in an aspect.  So it may include more than the user-visible methods.
     * This method returns an
     * empty array if it represents an array type or a primitive type.  
     *
     * @param world the {@link World} in which the lookup is done.
     * @return the array representing the declared methods of this type
     */
    public ResolvedMember[] getDeclaredMethods(World world) {
        return world.getDeclaredMethods(this);
    }
    

    /**
     * Returns an array representing the declared pointcuts of this object.
     * This method returns an
     * empty array if it represents an array type or a primitive type.
     *
     * @param world the {@link World} in which the lookup is done.
     * @return the array representing the declared pointcuts of this type
     */
    public ResolvedMember[] getDeclaredPointcuts(World world) {
    	return world.getDeclaredPointcuts(this);
    }
    
	/**
	 * Returns a resolved version of this type according to a particular world.
     * 
     * @param world thie {@link World} within which to resolve.
     * @return a resolved type representing this type in the appropriate world. 
	 */
	public ResolvedTypeX resolve(World world) {
		return world.resolve(this);
	}

    // ---- fields

    public static final TypeX[] NONE         = new TypeX[0];
    public static final TypeX   OBJECT       = forSignature("Ljava/lang/Object;");
    public static final TypeX   OBJECTARRAY  = forSignature("[Ljava/lang/Object;");
    public static final TypeX   CLONEABLE    = forSignature("Ljava/lang/Cloneable;");
    public static final TypeX   SERIALIZABLE = forSignature("Ljava/io/Serializable;");
    public static final TypeX   THROWABLE    = forSignature("Ljava/lang/Throwable;");
    public static final TypeX   RUNTIME_EXCEPTION    = forSignature("Ljava/lang/RuntimeException;");
    public static final TypeX   ERROR    = forSignature("Ljava/lang/Error;");
    
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
                return signature.substring(1, signature.length() - 1).replace('/', '.');
            case 'S': return "short";
            case 'V': return "void";
            case 'Z': return "boolean";
            case '[':
                return signatureToName(signature.substring(1, signature.length())) + "[]";
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
        if (name.length() != 0)  // lots more tests could be made here...
            return "L" + name.replace('.', '/') + ";";
        else 
            throw new BCException("Bad type name: " + name);
    }
    
	public void write(DataOutputStream s) throws IOException {
		s.writeUTF(signature);
	}
	
	public static TypeX read(DataInputStream s) throws IOException {
		String sig = s.readUTF();
		if (sig.equals(MISSING_NAME)) {
			return ResolvedTypeX.MISSING;
		} else {
			return TypeX.forSignature(sig);
		}
	}
	
	public static void writeArray(TypeX[] types, DataOutputStream s) throws IOException {
		int len = types.length;
		s.writeShort(len);
		for (int i=0; i < len; i++) {
			types[i].write(s);
		}
	}
	
	public static TypeX[] readArray(DataInputStream s) throws IOException {
		int len = s.readShort();
		TypeX[] types = new TypeX[len];
		for (int i=0; i < len; i++) {
			types[i] = TypeX.read(s);
		}
		return types;
	}


	/**
	 * For debugging purposes
	 */
	public void dump(World world) {
		if (isAspect(world)) System.out.print("aspect ");
		else if (isInterface(world)) System.out.print("interface ");
		else if (isClass(world)) System.out.print("class ");
		
		System.out.println(toString());
		dumpResolvedMembers("fields", getDeclaredFields(world));
		dumpResolvedMembers("methods", getDeclaredMethods(world));
		dumpResolvedMembers("pointcuts", getDeclaredPointcuts(world));
	}

	private void dumpResolvedMembers(String label, ResolvedMember[] l) {
		final String indent = "    ";
		System.out.println(label);
		if (l == null) {
			System.out.println(indent + "null");
			return;
		}
		
		for (int i=0, len=l.length; i < len; i++) {
			System.out.println(indent + l[i]);
		}
	}
	
	// ----

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
	
	public String getClassName() {
		String name = getName();
		int index = name.lastIndexOf('.');
		if (index == -1) { 
			return name; 
		} else {
			return name.substring(index+1);
		}
	}
	

	
	
	
	public static final String MISSING_NAME = "<missing>";

}

