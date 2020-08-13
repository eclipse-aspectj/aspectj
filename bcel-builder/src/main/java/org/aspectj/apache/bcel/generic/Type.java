package org.aspectj.apache.bcel.generic;

/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache BCEL" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache BCEL", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aspectj.apache.bcel.Constants;
import org.aspectj.apache.bcel.ConstantsInitializer;
import org.aspectj.apache.bcel.classfile.ClassFormatException;
import org.aspectj.apache.bcel.classfile.Utility;

/**
 * Abstract super class for all possible java types, namely basic types such as int, object types like String and array types, e.g.
 * int[]
 * 
 * @version $Id: Type.java,v 1.14 2011/09/28 01:14:54 aclement Exp $
 * @author <A HREF="mailto:markus.dahm@berlin.de">M. Dahm</A>
 * 
 *         modified: AndyClement 2-mar-05: Removed unnecessary static and optimized
 */
public abstract class Type {
	protected byte type;
	protected String signature;

	/* Predefined constants */
	public static final BasicType VOID = new BasicType(Constants.T_VOID);
	public static final BasicType BOOLEAN = new BasicType(Constants.T_BOOLEAN);
	public static final BasicType INT = new BasicType(Constants.T_INT);
	public static final BasicType SHORT = new BasicType(Constants.T_SHORT);
	public static final BasicType BYTE = new BasicType(Constants.T_BYTE);
	public static final BasicType LONG = new BasicType(Constants.T_LONG);
	public static final BasicType DOUBLE = new BasicType(Constants.T_DOUBLE);
	public static final BasicType FLOAT = new BasicType(Constants.T_FLOAT);
	public static final BasicType CHAR = new BasicType(Constants.T_CHAR);
	public static final ObjectType OBJECT = new ObjectType("java.lang.Object");
	public static final ObjectType STRING = new ObjectType("java.lang.String");
	public static final ArrayType OBJECT_ARRAY = new ArrayType("java.lang.Object",1);
	public static final ArrayType STRING_ARRAY = new ArrayType("java.lang.String",1);
	public static final ArrayType CLASS_ARRAY = new ArrayType("java.lang.Class",1);
	public static final ObjectType STRINGBUFFER = new ObjectType("java.lang.StringBuffer");
	public static final ObjectType STRINGBUILDER = new ObjectType("java.lang.StringBuilder");
	public static final ObjectType THROWABLE = new ObjectType("java.lang.Throwable");
	public static final ObjectType CLASS = new ObjectType("java.lang.Class");
	public static final ObjectType INTEGER = new ObjectType("java.lang.Integer");
	public static final ObjectType EXCEPTION = new ObjectType("java.lang.Exception");
	public static final ObjectType LIST = new ObjectType("java.util.List");
	public static final ObjectType ITERATOR = new ObjectType("java.util.Iterator");
	public static final Type[] NO_ARGS = new Type[0];
	public static final ReferenceType NULL = new ReferenceType() {
	};
	public static final Type UNKNOWN = new Type(Constants.T_UNKNOWN, "<unknown object>") {
	};
	public static final Type[] STRINGARRAY1 = new Type[] { STRING };
	public static final Type[] STRINGARRAY2 = new Type[] { STRING, STRING };
	public static final Type[] STRINGARRAY3 = new Type[] { STRING, STRING, STRING };
	public static final Type[] STRINGARRAY4 = new Type[] { STRING, STRING, STRING, STRING };
	public static final Type[] STRINGARRAY5 = new Type[] { STRING, STRING, STRING, STRING, STRING };
	public static final Type[] STRINGARRAY6 = new Type[] { STRING, STRING, STRING, STRING, STRING, STRING };
	public static final Type[] STRINGARRAY7 = new Type[] { STRING, STRING, STRING, STRING, STRING, STRING, STRING };

	private static Map<String, Type> commonTypes = new HashMap<>();

	static {
		commonTypes.put(STRING.getSignature(), STRING);
		commonTypes.put(THROWABLE.getSignature(), THROWABLE);
		commonTypes.put(VOID.getSignature(), VOID);
		commonTypes.put(BOOLEAN.getSignature(), BOOLEAN);
		commonTypes.put(BYTE.getSignature(), BYTE);
		commonTypes.put(SHORT.getSignature(), SHORT);
		commonTypes.put(CHAR.getSignature(), CHAR);
		commonTypes.put(INT.getSignature(), INT);
		commonTypes.put(LONG.getSignature(), LONG);
		commonTypes.put(DOUBLE.getSignature(), DOUBLE);
		commonTypes.put(FLOAT.getSignature(), FLOAT);
		commonTypes.put(CLASS.getSignature(), CLASS);
		commonTypes.put(OBJECT.getSignature(), OBJECT);
		commonTypes.put(STRING_ARRAY.getSignature(), STRING_ARRAY);
		commonTypes.put(CLASS_ARRAY.getSignature(), CLASS_ARRAY);
		commonTypes.put(OBJECT_ARRAY.getSignature(), OBJECT_ARRAY);
		commonTypes.put(INTEGER.getSignature(), INTEGER);
		commonTypes.put(EXCEPTION.getSignature(), EXCEPTION);
		commonTypes.put(STRINGBUFFER.getSignature(), STRINGBUFFER);
		commonTypes.put(STRINGBUILDER.getSignature(), STRINGBUILDER);
		commonTypes.put(LIST.getSignature(), LIST);
		commonTypes.put(ITERATOR.getSignature(), ITERATOR);
		ConstantsInitializer.initialize(); // needs calling because it will not have run properly the first time
	}

	protected Type(byte t, String s) {
		type = t;
		signature = s;
	}

	public String getSignature() {
		return signature;
	}

	public byte getType() {
		return type;
	}

	/**
	 * @return stack size of this type (2 for long and double, 0 for void, 1 otherwise)
	 */
	public int getSize() {
		switch (type) {
		case Constants.T_DOUBLE:
		case Constants.T_LONG:
			return 2;
		case Constants.T_VOID:
			return 0;
		default:
			return 1;
		}
	}

	/**
	 * @return Type string, e.g. 'int[]'
	 */
	@Override
	public String toString() {
		return ((this.equals(Type.NULL) || (type >= Constants.T_UNKNOWN))) ? signature : Utility
				.signatureToString(signature, false);
	}

	public static final Type getType(String signature) {
		Type t = commonTypes.get(signature);
		if (t != null) {
			return t;
		}
		byte type = Utility.typeOfSignature(signature);
		if (type <= Constants.T_VOID) {
			return BasicType.getType(type);
		} else if (type == Constants.T_ARRAY) {
			int dim = 0;
			do {
				dim++;
			} while (signature.charAt(dim) == '[');
			// Recurse, but just once, if the signature is ok
			Type componentType = getType(signature.substring(dim));
			return new ArrayType(componentType, dim);
		} else { // type == T_REFERENCE
			// generics awareness
			int nextAngly = signature.indexOf('<');
			// Format is 'Lblahblah;'
			int index = signature.indexOf(';'); // Look for closing ';'

			String typeString = null;
			if (nextAngly == -1 || nextAngly > index) {
				typeString = signature.substring(1, index).replace('/', '.');
			} else {
				boolean endOfSigReached = false;
				int posn = nextAngly;
				int genericDepth = 0;
				while (!endOfSigReached) {
					switch (signature.charAt(posn++)) {
					case '<':
						genericDepth++;
						break;
					case '>':
						genericDepth--;
						break;
					case ';':
						if (genericDepth == 0) {
							endOfSigReached = true;
						}
						break;
					default:
					}
				}
				index = posn - 1;
				typeString = signature.substring(1, nextAngly).replace('/', '.');
			}
			// ObjectType doesn't currently store parameterized info
			return new ObjectType(typeString);
		}
	}

	/**
	 * Convert signature to a Type object.
	 * 
	 * @param signature signature string such as Ljava/lang/String;
	 * @return type object
	 */
	public static final TypeHolder getTypeInternal(String signature) throws StringIndexOutOfBoundsException {
		byte type = Utility.typeOfSignature(signature);

		if (type <= Constants.T_VOID) {
			return new TypeHolder(BasicType.getType(type), 1);
		} else if (type == Constants.T_ARRAY) {
			int dim = 0;
			do {
				dim++;
			} while (signature.charAt(dim) == '[');
			// Recurse, but just once, if the signature is ok
			TypeHolder th = getTypeInternal(signature.substring(dim));
			return new TypeHolder(new ArrayType(th.getType(), dim), dim + th.getConsumed());
		} else { // type == T_REFERENCE
			// Format is 'Lblahblah;'
			int index = signature.indexOf(';'); // Look for closing ';'
			if (index < 0) {
				throw new ClassFormatException("Invalid signature: " + signature);
			}

			// generics awareness
			int nextAngly = signature.indexOf('<');
			String typeString = null;
			if (nextAngly == -1 || nextAngly > index) {
				typeString = signature.substring(1, index).replace('/', '.');
			} else {
				boolean endOfSigReached = false;
				int posn = nextAngly;
				int genericDepth = 0;
				while (!endOfSigReached) {
					switch (signature.charAt(posn++)) {
					case '<':
						genericDepth++;
						break;
					case '>':
						genericDepth--;
						break;
					case ';':
						if (genericDepth == 0) {
							endOfSigReached = true;
						}
						break;
					default:
					}
				}
				index = posn - 1;
				typeString = signature.substring(1, nextAngly).replace('/', '.');
			}
			// ObjectType doesn't currently store parameterized info
			return new TypeHolder(new ObjectType(typeString), index + 1);
		}
	}

	/**
	 * Convert return value of a method (signature) to a Type object.
	 * 
	 * @param signature signature string such as (Ljava/lang/String;)V
	 * @return return type
	 */
	public static Type getReturnType(String signature) {
		try {
			// Read return type after ')'
			int index = signature.lastIndexOf(')') + 1;
			return getType(signature.substring(index));
		} catch (StringIndexOutOfBoundsException e) { // Should never occur
			throw new ClassFormatException("Invalid method signature: " + signature);
		}
	}

	/**
	 * Convert arguments of a method (signature) to an array of Type objects.
	 * 
	 * @param signature signature string such as (Ljava/lang/String;)V
	 * @return array of argument types
	 */
	// OPTIMIZE crap impl
	public static Type[] getArgumentTypes(String signature) {
		List<Type> argumentTypes = new ArrayList<>();
		int index;
		Type[] types;

		try { // Read all declarations between for `(' and `)'
			if (signature.charAt(0) != '(') {
				throw new ClassFormatException("Invalid method signature: " + signature);
			}

			index = 1; // current string position

			while (signature.charAt(index) != ')') {
				TypeHolder th = getTypeInternal(signature.substring(index));
				argumentTypes.add(th.getType());
				index += th.getConsumed(); // update position
			}
		} catch (StringIndexOutOfBoundsException e) { // Should never occur
			throw new ClassFormatException("Invalid method signature: " + signature);
		}

		types = new Type[argumentTypes.size()];
		argumentTypes.toArray(types);
		return types;
	}

	/**
	 * Work out the type of each argument in the signature and return the cumulative sizes of all the types (size means number of
	 * stack slots it consumes, eg double=2, int=1). Unlike the call above, this does minimal unpacking
	 */
	public static int getArgumentSizes(String signature) {
		int size = 0;
		if (signature.charAt(0) != '(') {
			throw new ClassFormatException("Invalid method signature: " + signature);
		}

		int index = 1; // current string position
		try {
			while (signature.charAt(index) != ')') {
				byte type = Utility.typeOfSignature(signature.charAt(index));
				if (type <= Constants.T_VOID) {
					size += BasicType.getType(type).getSize();
					index++;
				} else if (type == Constants.T_ARRAY) {
					int dim = 0;
					do {
						dim++;
					} while (signature.charAt(dim + index) == '[');
					TypeHolder th = getTypeInternal(signature.substring(dim + index));
					size += 1;
					index += dim + th.getConsumed();
				} else { // type == T_REFERENCE
					// Format is 'Lblahblah;'
					int index2 = signature.indexOf(';', index); // Look for closing ';'

					// generics awareness
					int nextAngly = signature.indexOf('<', index);
					if (nextAngly == -1 || nextAngly > index2) {
					} else {
						boolean endOfSigReached = false;
						int posn = nextAngly;
						int genericDepth = 0;
						while (!endOfSigReached) {
							switch (signature.charAt(posn++)) {
							case '<':
								genericDepth++;
								break;
							case '>':
								genericDepth--;
								break;
							case ';':
								if (genericDepth == 0) {
									endOfSigReached = true;
								}
								break;
							default:
							}
						}
						index2 = posn - 1;
					}
					size++;
					index = index2 + 1;
				}
			}
		} catch (StringIndexOutOfBoundsException e) { // Should never occur
			throw new ClassFormatException("Invalid method signature: " + signature);
		}
		return size;
	}

	/**
	 * Return the size of the type expressed in the signature. The signature should contain only one type.
	 */
	public static int getTypeSize(String signature) {
		byte type = Utility.typeOfSignature(signature.charAt(0));
		if (type <= Constants.T_VOID) {
			return BasicType.getType(type).getSize();
		} else if (type == Constants.T_ARRAY) {
			return 1;
		} else { // type == T_REFERENCE
			return 1;
		}
	}

	/**
	 * Convert runtime java.lang.Class to BCEL Type object.
	 * 
	 * @param cl Java class
	 * @return corresponding Type object
	 */
	public static Type getType(java.lang.Class cl) {
		if (cl == null) {
			throw new IllegalArgumentException("Class must not be null");
		}

		/*
		 * That's an amazingly easy case, because getName() returns the signature. That's what we would have liked anyway.
		 */
		if (cl.isArray()) {
			return getType(cl.getName());
		} else if (cl.isPrimitive()) {
			if (cl == Integer.TYPE) {
				return INT;
			} else if (cl == Void.TYPE) {
				return VOID;
			} else if (cl == Double.TYPE) {
				return DOUBLE;
			} else if (cl == Float.TYPE) {
				return FLOAT;
			} else if (cl == Boolean.TYPE) {
				return BOOLEAN;
			} else if (cl == Byte.TYPE) {
				return BYTE;
			} else if (cl == Short.TYPE) {
				return SHORT;
			} else if (cl == Byte.TYPE) {
				return BYTE;
			} else if (cl == Long.TYPE) {
				return LONG;
			} else if (cl == Character.TYPE) {
				return CHAR;
			} else {
				throw new IllegalStateException("Ooops, what primitive type is " + cl);
			}
		} else { // "Real" class
			return new ObjectType(cl.getName());
		}
	}

	public static String getSignature(java.lang.reflect.Method meth) {
		StringBuffer sb = new StringBuffer("(");
		Class[] params = meth.getParameterTypes(); // avoid clone

		for (Class param : params) {
			sb.append(getType(param).getSignature());
		}

		sb.append(")");
		sb.append(getType(meth.getReturnType()).getSignature());
		return sb.toString();
	}

	public static String getSignature(java.lang.reflect.Constructor<?> cons) {
		StringBuffer sb = new StringBuffer("(");
		Class<?>[] params = cons.getParameterTypes(); // avoid clone

		for (Class<?> param : params) {
			sb.append(getType(param).getSignature());
		}

		sb.append(")V");
		return sb.toString();
	}

	public static class TypeHolder {
		private Type t;
		private int consumed;

		public Type getType() {
			return t;
		}

		public int getConsumed() {
			return consumed;
		}

		public TypeHolder(Type t, int i) {
			this.t = t;
			this.consumed = i;
		}
	}

}
