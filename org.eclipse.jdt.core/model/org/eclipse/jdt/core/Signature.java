/*******************************************************************************
 * Copyright (c) 2000, 2001, 2002 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.core;

import org.eclipse.jdt.core.compiler.ITerminalSymbols;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.internal.compiler.parser.Scanner;
import org.eclipse.jdt.internal.compiler.util.CharOperation;

/**
 * Provides methods for encoding and decoding type and method signature strings.
 * <p>
 * The syntax for a type signature is:
 * <pre>
 * typeSignature ::=
 *     "B"  // byte
 *   | "C"  // char
 *   | "D"  // double
 *   | "F"  // float
 *   | "I"  // int
 *   | "J"  // long
 *   | "S"  // short
 *   | "V"  // void
 *   | "Z"  // boolean
 *   | "L" + binaryTypeName + ";"  // resolved named type (i.e., in compiled code)
 *   | "Q" + sourceTypeName + ";"  // unresolved named type (i.e., in source code)
 *   | "[" + typeSignature  // array of type denoted by typeSignature
 * </pre>
 * </p>
 * <p>
 * Examples:
 * <ul>
 *   <li><code>"[[I"</code> denotes <code>int[][]</code></li>
 *   <li><code>"Ljava.lang.String;"</code> denotes <code>java.lang.String</code> in compiled code</li>
 *   <li><code>"QString"</code> denotes <code>String</code> in source code</li>
 *   <li><code>"Qjava.lang.String"</code> denotes <code>java.lang.String</code> in source code</li>
 *   <li><code>"[QString"</code> denotes <code>String[]</code> in source code</li>
 * </ul>
 * </p>
 * <p>
 * The syntax for a method signature is:
 * <pre>
 * methodSignature ::= "(" + paramTypeSignature* + ")" + returnTypeSignature
 * paramTypeSignature ::= typeSignature
 * returnTypeSignature ::= typeSignature
 * </pre>
 * <p>
 * Examples:
 * <ul>
 *   <li><code>"()I"</code> denotes <code>int foo()</code></li>
 *   <li><code>"([Ljava.lang.String;)V"</code> denotes <code>void foo(java.lang.String[])</code> in compiled code</li>
 *   <li><code>"(QString;)QObject;"</code> denotes <code>Object foo(String)</code> in source code</li>
 * </ul>
 * </p>
 * <p>
 * This class provides static methods and constants only; it is not intended to be
 * instantiated or subclassed by clients.
 * </p>
 */
public final class Signature {

	/**
	 * Character constant indicating the primitive type boolean in a signature.
	 * Value is <code>'Z'</code>.
	 */
	public static final char C_BOOLEAN 		= 'Z';

	/**
	 * Character constant indicating the primitive type byte in a signature.
	 * Value is <code>'B'</code>.
	 */
	public static final char C_BYTE 		= 'B';

	/**
	 * Character constant indicating the primitive type char in a signature.
	 * Value is <code>'C'</code>.
	 */
	public static final char C_CHAR 		= 'C';

	/**
	 * Character constant indicating the primitive type double in a signature.
	 * Value is <code>'D'</code>.
	 */
	public static final char C_DOUBLE 		= 'D';

	/**
	 * Character constant indicating the primitive type float in a signature.
	 * Value is <code>'F'</code>.
	 */
	public static final char C_FLOAT 		= 'F';

	/**
	 * Character constant indicating the primitive type int in a signature.
	 * Value is <code>'I'</code>.
	 */
	public static final char C_INT 			= 'I';
	
	/**
	 * Character constant indicating the semicolon in a signature.
	 * Value is <code>';'</code>.
	 */
	public static final char C_SEMICOLON 			= ';';

	/**
	 * Character constant indicating the primitive type long in a signature.
	 * Value is <code>'J'</code>.
	 */
	public static final char C_LONG			= 'J';
	
	/**
	 * Character constant indicating the primitive type short in a signature.
	 * Value is <code>'S'</code>.
	 */
	public static final char C_SHORT		= 'S';
	
	/**
	 * Character constant indicating result type void in a signature.
	 * Value is <code>'V'</code>.
	 */
	public static final char C_VOID			= 'V';
	
	/** 
	 * Character constant indicating the dot in a signature. 
	 * Value is <code>'.'</code>.
	 */
	public static final char C_DOT			= '.';
	
	/** 
	 * Character constant indicating the dollar in a signature.
	 * Value is <code>'$'</code>.
	 */
	public static final char C_DOLLAR			= '$';

	/** 
	 * Character constant indicating an array type in a signature.
	 * Value is <code>'['</code>.
	 */
	public static final char C_ARRAY		= '[';

	/** 
	 * Character constant indicating the start of a resolved, named type in a 
	 * signature. Value is <code>'L'</code>.
	 */
	public static final char C_RESOLVED		= 'L';

	/** 
	 * Character constant indicating the start of an unresolved, named type in a
	 * signature. Value is <code>'Q'</code>.
	 */
	public static final char C_UNRESOLVED	= 'Q';

	/**
	 * Character constant indicating the end of a named type in a signature. 
	 * Value is <code>';'</code>.
	 */
	public static final char C_NAME_END		= ';';

	/**
	 * Character constant indicating the start of a parameter type list in a
	 * signature. Value is <code>'('</code>.
	 */
	public static final char C_PARAM_START	= '(';

	/**
	 * Character constant indicating the end of a parameter type list in a 
	 * signature. Value is <code>')'</code>.
	 */
	public static final char C_PARAM_END	= ')';

	/**
	 * String constant for the signature of the primitive type boolean.
	 * Value is <code>"Z"</code>.
	 */
	public static final String SIG_BOOLEAN 		= "Z"; //$NON-NLS-1$

	/**
	 * String constant for the signature of the primitive type byte. 
	 * Value is <code>"B"</code>.
	 */
	public static final String SIG_BYTE 		= "B"; //$NON-NLS-1$

	/**
	 * String constant for the signature of the primitive type char.
	 * Value is <code>"C"</code>.
	 */
	public static final String SIG_CHAR 		= "C"; //$NON-NLS-1$

	/**
	 * String constant for the signature of the primitive type double.
	 * Value is <code>"D"</code>.
	 */
	public static final String SIG_DOUBLE 		= "D"; //$NON-NLS-1$

	/**
	 * String constant for the signature of the primitive type float.
	 * Value is <code>"F"</code>.
	 */
	public static final String SIG_FLOAT 		= "F"; //$NON-NLS-1$

	/**
	 * String constant for the signature of the primitive type int.
	 * Value is <code>"I"</code>.
	 */
	public static final String SIG_INT 			= "I"; //$NON-NLS-1$

	/**
	 * String constant for the signature of the primitive type long.
	 * Value is <code>"J"</code>.
	 */
	public static final String SIG_LONG			= "J"; //$NON-NLS-1$

	/**
	 * String constant for the signature of the primitive type short.
	 * Value is <code>"S"</code>.
	 */
	public static final String SIG_SHORT		= "S"; //$NON-NLS-1$

	/** String constant for the signature of result type void.
	 * Value is <code>"V"</code>.
	 */
	public static final String SIG_VOID			= "V"; //$NON-NLS-1$
	
	private static final char[] NO_CHAR = new char[0];
	private static final char[][] NO_CHAR_CHAR = new char[0][];
	private static final char[] BOOLEAN = {'b', 'o', 'o', 'l', 'e', 'a', 'n'};
	private static final char[] BYTE = {'b', 'y', 't', 'e'};
	private static final char[] CHAR = {'c', 'h', 'a', 'r'};
	private static final char[] DOUBLE = {'d', 'o', 'u', 'b', 'l', 'e'};
	private static final char[] FLOAT = {'f', 'l', 'o', 'a', 't'};
	private static final char[] INT = {'i', 'n', 't'};
	private static final char[] LONG = {'l', 'o', 'n', 'g'};
	private static final char[] SHORT = {'s', 'h', 'o', 'r', 't'};
	private static final char[] VOID = {'v', 'o', 'i', 'd'};
	
	
/**
 * Not instantiable.
 */
private Signature() {}

private static long copyType(char[] signature, int sigPos, char[] dest, int index, boolean fullyQualifyTypeNames) {
	int arrayCount = 0;
	loop: while (true) {
		switch (signature[sigPos++]) {
			case C_ARRAY :
				arrayCount++;
				break;
			case C_BOOLEAN :
				int length = BOOLEAN.length;
				System.arraycopy(BOOLEAN, 0, dest, index, length);
				index += length;
				break loop;
			case C_BYTE :
				length = BYTE.length;
				System.arraycopy(BYTE, 0, dest, index, length);
				index += length;
				break loop;
			case C_CHAR :
				length = CHAR.length;
				System.arraycopy(CHAR, 0, dest, index, length);
				index += length;
				break loop;
			case C_DOUBLE :
				length = DOUBLE.length;
				System.arraycopy(DOUBLE, 0, dest, index, length);
				index += length;
				break loop;
			case C_FLOAT :
				length = FLOAT.length;
				System.arraycopy(FLOAT, 0, dest, index, length);
				index += length;
				break loop;
			case C_INT :
				length = INT.length;
				System.arraycopy(INT, 0, dest, index, length);
				index += length;
				break loop;
			case C_LONG :
				length = LONG.length;
				System.arraycopy(LONG, 0, dest, index, length);
				index += length;
				break loop;
			case C_SHORT :
				length = SHORT.length;
				System.arraycopy(SHORT, 0, dest, index, length);
				index += length;
				break loop;
			case C_VOID :
				length = VOID.length;
				System.arraycopy(VOID, 0, dest, index, length);
				index += length;
				break loop;
			case C_RESOLVED :
			case C_UNRESOLVED :
				int end = CharOperation.indexOf(C_SEMICOLON, signature, sigPos);
				if (end == -1) throw new IllegalArgumentException();
				int start;
				if (fullyQualifyTypeNames) {
					start = sigPos;
				} else {
					start = CharOperation.lastIndexOf(C_DOT, signature, sigPos, end)+1;
					if (start == 0) start = sigPos;
				} 
				length = end-start;
				System.arraycopy(signature, start, dest, index, length);
				sigPos = end+1;
				index += length;
				break loop;
		}
	}
	while (arrayCount-- > 0) {
		dest[index++] = '[';
		dest[index++] = ']';
	}
	return (((long) index) << 32) + sigPos;
}
/**
 * Creates a new type signature with the given amount of array nesting added 
 * to the given type signature.
 *
 * @param typeSignature the type signature
 * @param arrayCount the desired number of levels of array nesting
 * @return the encoded array type signature
 * 
 * @since 2.0
 */
public static char[] createArraySignature(char[] typeSignature, int arrayCount) {
	if (arrayCount == 0) return typeSignature;
	int sigLength = typeSignature.length;
	char[] result = new char[arrayCount + sigLength];
	for (int i = 0; i < arrayCount; i++) {
		result[i] = C_ARRAY;
	}
	System.arraycopy(typeSignature, 0, result, arrayCount, sigLength);
	return result;
}
/**
 * Creates a new type signature with the given amount of array nesting added 
 * to the given type signature.
 *
 * @param typeSignature the type signature
 * @param arrayCount the desired number of levels of array nesting
 * @return the encoded array type signature
 */
public static String createArraySignature(String typeSignature, int arrayCount) {
	return new String(createArraySignature(typeSignature.toCharArray(), arrayCount));
}
/**
 * Creates a method signature from the given parameter and return type 
 * signatures. The encoded method signature is dot-based.
 *
 * @param parameterTypes the list of parameter type signatures
 * @param returnType the return type signature
 * @return the encoded method signature
 * 
 * @since 2.0
 */
public static char[] createMethodSignature(char[][] parameterTypes, char[] returnType) {
	int parameterTypesLength = parameterTypes.length;
	int parameterLength = 0;
	for (int i = 0; i < parameterTypesLength; i++) {
		parameterLength += parameterTypes[i].length;
		
	}
	int returnTypeLength = returnType.length;
	char[] result = new char[1 + parameterLength + 1 + returnTypeLength];
	result[0] = C_PARAM_START;
	int index = 1;
	for (int i = 0; i < parameterTypesLength; i++) {
		char[] parameterType = parameterTypes[i];
		int length = parameterType.length;
		System.arraycopy(parameterType, 0, result, index, length);
		index += length;
	}
	result[index] = C_PARAM_END;
	System.arraycopy(returnType, 0, result, index+1, returnTypeLength);
	return result;
}
/**
 * Creates a method signature from the given parameter and return type 
 * signatures. The encoded method signature is dot-based.
 *
 * @param parameterTypes the list of parameter type signatures
 * @param returnType the return type signature
 * @return the encoded method signature
 */
public static String createMethodSignature(String[] parameterTypes, String returnType) {
	int parameterTypesLenth = parameterTypes.length;
	char[][] parameters = new char[parameterTypesLenth][];
	for (int i = 0; i < parameterTypesLenth; i++) {
		parameters[i] = parameterTypes[i].toCharArray();
	}
	return new String(createMethodSignature(parameters, returnType.toCharArray()));
}
/**
 * Creates a new type signature from the given type name encoded as a character
 * array. This method is equivalent to
 * <code>createTypeSignature(new String(typeName),isResolved)</code>, although
 * more efficient for callers with character arrays rather than strings. If the 
 * type name is qualified, then it is expected to be dot-based.
 *
 * @param typeName the possibly qualified type name
 * @param isResolved <code>true</code> if the type name is to be considered
 *   resolved (for example, a type name from a binary class file), and 
 *   <code>false</code> if the type name is to be considered unresolved
 *   (for example, a type name found in source code)
 * @return the encoded type signature
 * @see #createTypeSignature(java.lang.String,boolean)
 */
public static String createTypeSignature(char[] typeName, boolean isResolved) {
	return new String(createCharArrayTypeSignature(typeName, isResolved));
}
/**
 * Creates a new type signature from the given type name encoded as a character
 * array. This method is equivalent to
 * <code>createTypeSignature(new String(typeName),isResolved).toCharArray()</code>, although
 * more efficient for callers with character arrays rather than strings. If the 
 * type name is qualified, then it is expected to be dot-based.
 *
 * @param typeName the possibly qualified type name
 * @param isResolved <code>true</code> if the type name is to be considered
 *   resolved (for example, a type name from a binary class file), and 
 *   <code>false</code> if the type name is to be considered unresolved
 *   (for example, a type name found in source code)
 * @return the encoded type signature
 * @see #createTypeSignature(java.lang.String,boolean)
 * 
 * @since 2.0
 */
public static char[] createCharArrayTypeSignature(char[] typeName, boolean isResolved) {
	try {
		Scanner scanner = new Scanner();
		scanner.setSource(typeName);
		int token = scanner.getNextToken();
		boolean primitive = true;
		char primitiveSig = ' ';
		StringBuffer sig = null;
		int arrayCount = 0;
		switch (token) {
			case ITerminalSymbols.TokenNameIdentifier :
				char[] idSource = scanner.getCurrentIdentifierSource();
				sig = new StringBuffer(idSource.length);
				sig.append(idSource);
				primitive = false;
				break;
			case ITerminalSymbols.TokenNameboolean :
				primitiveSig = Signature.C_BOOLEAN;
				break;
			case ITerminalSymbols.TokenNamebyte :
				primitiveSig = Signature.C_BYTE;
				break;
			case ITerminalSymbols.TokenNamechar :
				primitiveSig = Signature.C_CHAR;
				break;
			case ITerminalSymbols.TokenNamedouble :
				primitiveSig = Signature.C_DOUBLE;
				break;
			case ITerminalSymbols.TokenNamefloat :
				primitiveSig = Signature.C_FLOAT;
				break;
			case ITerminalSymbols.TokenNameint :
				primitiveSig = Signature.C_INT;
				break;
			case ITerminalSymbols.TokenNamelong :
				primitiveSig = Signature.C_LONG;
				break;
			case ITerminalSymbols.TokenNameshort :
				primitiveSig = Signature.C_SHORT;
				break;
			case ITerminalSymbols.TokenNamevoid :
				primitiveSig = Signature.C_VOID;
				break;
			default :
				throw new IllegalArgumentException();
		}
		token = scanner.getNextToken();
		while (!primitive && token == ITerminalSymbols.TokenNameDOT) {
			sig.append(scanner.getCurrentIdentifierSource());
			token = scanner.getNextToken();
			if (token == ITerminalSymbols.TokenNameIdentifier) {
				sig.append(scanner.getCurrentIdentifierSource());
				token = scanner.getNextToken();
			} else {
				throw new IllegalArgumentException();
			}
		}
		while (token == ITerminalSymbols.TokenNameLBRACKET) {
			token = scanner.getNextToken();
			if (token != ITerminalSymbols.TokenNameRBRACKET)
				throw new IllegalArgumentException();
			arrayCount++;
			token = scanner.getNextToken();
		}
		if (token != ITerminalSymbols.TokenNameEOF)
			throw new IllegalArgumentException();
		char[] result;
		if (primitive) {
			result = new char[arrayCount+1];
			result[arrayCount] = primitiveSig;
		} else {
			int sigLength = sig.length(); 
			int resultLength = arrayCount + 1 + sigLength + 1; // e.g. '[[[Ljava.lang.String;'
			result = new char[resultLength];
			sig.getChars(0, sigLength, result, arrayCount + 1);
			result[arrayCount] = isResolved ? C_RESOLVED : C_UNRESOLVED;
			result[resultLength-1] = C_NAME_END;
		}
		for (int i = 0; i < arrayCount; i++) {
			result[i] = C_ARRAY;
		}
		return result;
	} catch (InvalidInputException e) {
		throw new IllegalArgumentException();
	}
}
/**
 * Creates a new type signature from the given type name. If the type name is qualified,
 * then it is expected to be dot-based.
 * <p>
 * For example:
 * <pre>
 * <code>
 * createTypeSignature("int", hucairz) -> "I"
 * createTypeSignature("java.lang.String", true) -> "Ljava.lang.String;"
 * createTypeSignature("String", false) -> "QString;"
 * createTypeSignature("java.lang.String", false) -> "Qjava.lang.String;"
 * createTypeSignature("int []", false) -> "[I"
 * </code>
 * </pre>
 * </p>
 *
 * @param typeName the possibly qualified type name
 * @param isResolved <code>true</code> if the type name is to be considered
 *   resolved (for example, a type name from a binary class file), and 
 *   <code>false</code> if the type name is to be considered unresolved
 *   (for example, a type name found in source code)
 * @return the encoded type signature
 */
public static String createTypeSignature(String typeName, boolean isResolved) {
	return createTypeSignature(typeName.toCharArray(), isResolved);
}
/**
 * Returns the array count (array nesting depth) of the given type signature.
 *
 * @param typeSignature the type signature
 * @return the array nesting depth, or 0 if not an array
 * @exception IllegalArgumentException if the signature is not syntactically
 *   correct
 * 
 * @since 2.0
 */
public static int getArrayCount(char[] typeSignature) throws IllegalArgumentException {	
	try {
		int count = 0;
		while (typeSignature[count] == C_ARRAY) {
			++count;
		}
		return count;
	} catch (ArrayIndexOutOfBoundsException e) { // signature is syntactically incorrect if last character is C_ARRAY
		throw new IllegalArgumentException();
	}
}
/**
 * Returns the array count (array nesting depth) of the given type signature.
 *
 * @param typeSignature the type signature
 * @return the array nesting depth, or 0 if not an array
 * @exception IllegalArgumentException if the signature is not syntactically
 *   correct
 */
public static int getArrayCount(String typeSignature) throws IllegalArgumentException {
	return getArrayCount(typeSignature.toCharArray());
}
/**
 * Returns the type signature without any array nesting.
 * <p>
 * For example:
 * <pre>
 * <code>
 * getElementType({'[', '[', 'I'}) --> {'I'}.
 * </code>
 * </pre>
 * </p>
 * 
 * @param typeSignature the type signature
 * @return the type signature without arrays
 * @exception IllegalArgumentException if the signature is not syntactically
 *   correct
 * 
 * @since 2.0
 */
public static char[] getElementType(char[] typeSignature) throws IllegalArgumentException {
	int count = getArrayCount(typeSignature);
	if (count == 0) return typeSignature;
	int length = typeSignature.length;
	char[] result = new char[length-count];
	System.arraycopy(typeSignature, count, result, 0, length-count);
	return result;
}
/**
 * Returns the type signature without any array nesting.
 * <p>
 * For example:
 * <pre>
 * <code>
 * getElementType("[[I") --> "I".
 * </code>
 * </pre>
 * </p>
 * 
 * @param typeSignature the type signature
 * @return the type signature without arrays
 * @exception IllegalArgumentException if the signature is not syntactically
 *   correct
 */
public static String getElementType(String typeSignature) throws IllegalArgumentException {
	return new String(getElementType(typeSignature.toCharArray()));
}
/**
 * Returns the number of parameter types in the given method signature.
 *
 * @param methodSignature the method signature
 * @return the number of parameters
 * @exception IllegalArgumentException if the signature is not syntactically
 *   correct
 * @since 2.0
 */
public static int getParameterCount(char[] methodSignature) throws IllegalArgumentException {
	try {
		int count = 0;
		int i = CharOperation.indexOf(C_PARAM_START, methodSignature) + 1;
		if (i == 0)
			throw new IllegalArgumentException();
		for (;;) {
			char c = methodSignature[i++];
			switch (c) {
				case C_ARRAY :
					break;
				case C_BOOLEAN :
				case C_BYTE :
				case C_CHAR :
				case C_DOUBLE :
				case C_FLOAT :
				case C_INT :
				case C_LONG :
				case C_SHORT :
				case C_VOID :
					++count;
					break;
				case C_RESOLVED :
				case C_UNRESOLVED :
					i = CharOperation.indexOf(C_SEMICOLON, methodSignature, i) + 1;
					if (i == 0)
						throw new IllegalArgumentException();
					++count;
					break;
				case C_PARAM_END :
					return count;
				default :
					throw new IllegalArgumentException();
			}
		}
	} catch (ArrayIndexOutOfBoundsException e) {
		throw new IllegalArgumentException();
	}
}
/**
 * Returns the number of parameter types in the given method signature.
 *
 * @param methodSignature the method signature
 * @return the number of parameters
 * @exception IllegalArgumentException if the signature is not syntactically
 *   correct
 */
public static int getParameterCount(String methodSignature) throws IllegalArgumentException {
	return getParameterCount(methodSignature.toCharArray());
}
/**
 * Extracts the parameter type signatures from the given method signature. 
 * The method signature is expected to be dot-based.
 *
 * @param methodSignature the method signature
 * @return the list of parameter type signatures
 * @exception IllegalArgumentException if the signature is syntactically
 *   incorrect
 * 
 * @since 2.0
 */
public static char[][] getParameterTypes(char[] methodSignature) throws IllegalArgumentException {
	try {
		int count = getParameterCount(methodSignature);
		char[][] result = new char[count][];
		if (count == 0)
			return result;
		int i = CharOperation.indexOf(C_PARAM_START, methodSignature) + 1;
		count = 0;
		int start = i;
		for (;;) {
			char c = methodSignature[i++];
			switch (c) {
				case C_ARRAY :
					// array depth is i - start;
					break;
				case C_BOOLEAN :
				case C_BYTE :
				case C_CHAR :
				case C_DOUBLE :
				case C_FLOAT :
				case C_INT :
				case C_LONG :
				case C_SHORT :
				case C_VOID :
					// common case of base types
					if (i - start == 1) {
						switch (c) {
							case C_BOOLEAN :
								result[count++] = new char[] {C_BOOLEAN};
								break;
							case C_BYTE :
								result[count++] = new char[] {C_BYTE};
								break;
							case C_CHAR :
								result[count++] = new char[] {C_CHAR};
								break;
							case C_DOUBLE :
								result[count++] = new char[] {C_DOUBLE};
								break;
							case C_FLOAT :
								result[count++] = new char[] {C_FLOAT};
								break;
							case C_INT :
								result[count++] = new char[] {C_INT};
								break;
							case C_LONG :
								result[count++] = new char[] {C_LONG};
								break;
							case C_SHORT :
								result[count++] = new char[] {C_SHORT};
								break;
							case C_VOID :
								result[count++] = new char[] {C_VOID};
								break;
						}
					} else {
						result[count++] = CharOperation.subarray(methodSignature, start, i);
					}
					start = i;
					break;
				case C_RESOLVED :
				case C_UNRESOLVED :
					i = CharOperation.indexOf(C_SEMICOLON, methodSignature, i) + 1;
					if (i == 0)
						throw new IllegalArgumentException();
					result[count++] = CharOperation.subarray(methodSignature, start, i);
					start = i;
					break;
				case C_PARAM_END:
					return result;
				default :
					throw new IllegalArgumentException();
			}
		}
	} catch (ArrayIndexOutOfBoundsException e) {
		throw new IllegalArgumentException();
	}
}
/**
 * Extracts the parameter type signatures from the given method signature. 
 * The method signature is expected to be dot-based.
 *
 * @param methodSignature the method signature
 * @return the list of parameter type signatures
 * @exception IllegalArgumentException if the signature is syntactically
 *   incorrect
 */
public static String[] getParameterTypes(String methodSignature) throws IllegalArgumentException {
	char[][] parameterTypes = getParameterTypes(methodSignature.toCharArray());
	int length = parameterTypes.length;
	String[] result = new String[length];
	for (int i = 0; i < length; i++) {
		result[i] = new String(parameterTypes[i]);
	}
	return result;
}
/**
 * Returns a char array containing all but the last segment of the given 
 * dot-separated qualified name. Returns the empty char array if it is not qualified.
 * <p>
 * For example:
 * <pre>
 * <code>
 * getQualifier({'j', 'a', 'v', 'a', '.', 'l', 'a', 'n', 'g', '.', 'O', 'b', 'j', 'e', 'c', 't'}) -> {'j', 'a', 'v', 'a', '.', 'l', 'a', 'n', 'g'}
 * getQualifier({'O', 'u', 't', 'e', 'r', '.', 'I', 'n', 'n', 'e', 'r'}) -> {'O', 'u', 't', 'e', 'r'}
 * </code>
 * </pre>
 * </p>
 *
 * @param name the name
 * @return the qualifier prefix, or the empty char array if the name contains no
 *   dots
 * 
 * @since 2.0
 */
public static char[] getQualifier(char[] name) {
	int lastDot = CharOperation.lastIndexOf(C_DOT, name);
	if (lastDot == -1) {
		return NO_CHAR; //$NON-NLS-1$
	}
	return CharOperation.subarray(name, 0, lastDot);
}
/**
 * Returns a string containing all but the last segment of the given 
 * dot-separated qualified name. Returns the empty string if it is not qualified.
 * <p>
 * For example:
 * <pre>
 * <code>
 * getQualifier("java.lang.Object") -> "java.lang"
 * getQualifier("Outer.Inner") -> "Outer"
 * </code>
 * </pre>
 * </p>
 *
 * @param name the name
 * @return the qualifier prefix, or the empty string if the name contains no
 *   dots
 */
public static String getQualifier(String name) {
	return new String(getQualifier(name.toCharArray()));
}
/**
 * Extracts the return type from the given method signature. The method signature is 
 * expected to be dot-based.
 *
 * @param methodSignature the method signature
 * @return the type signature of the return type
 * @exception IllegalArgumentException if the signature is syntactically
 *   incorrect
 * 
 * @since 2.0
 */
public static char[] getReturnType(char[] methodSignature) throws IllegalArgumentException {
	int i = CharOperation.lastIndexOf(C_PARAM_END, methodSignature);
	if (i == -1) {
		throw new IllegalArgumentException();
	}
	return CharOperation.subarray(methodSignature, i + 1, methodSignature.length);
}
/**
 * Extracts the return type from the given method signature. The method signature is 
 * expected to be dot-based.
 *
 * @param methodSignature the method signature
 * @return the type signature of the return type
 * @exception IllegalArgumentException if the signature is syntactically
 *   incorrect
 */
public static String getReturnType(String methodSignature) throws IllegalArgumentException {
	return new String(getReturnType(methodSignature.toCharArray()));
}
/**
 * Returns the last segment of the given dot-separated qualified name.
 * Returns the given name if it is not qualified.
 * <p>
 * For example:
 * <pre>
 * <code>
 * getSimpleName({'j', 'a', 'v', 'a', '.', 'l', 'a', 'n', 'g', '.', 'O', 'b', 'j', 'e', 'c', 't'}) -> {'O', 'b', 'j', 'e', 'c', 't'}
 * </code>
 * </pre>
 * </p>
 *
 * @param name the name
 * @return the last segment of the qualified name
 * 
 * @since 2.0
 */
public static char[] getSimpleName(char[] name) {
	int lastDot = CharOperation.lastIndexOf(C_DOT, name);
	if (lastDot == -1) {
		return name;
	}
	return CharOperation.subarray(name, lastDot + 1, name.length);
}
/**
 * Returns the last segment of the given dot-separated qualified name.
 * Returns the given name if it is not qualified.
 * <p>
 * For example:
 * <pre>
 * <code>
 * getSimpleName("java.lang.Object") -> "Object"
 * </code>
 * </pre>
 * </p>
 *
 * @param name the name
 * @return the last segment of the qualified name
 */
public static String getSimpleName(String name) {
	return new String(getSimpleName(name.toCharArray()));
}
/**
 * Returns all segments of the given dot-separated qualified name.
 * Returns an array with only the given name if it is not qualified.
 * Returns an empty array if the name is empty.
 * <p>
 * For example:
 * <pre>
 * <code>
 * getSimpleNames({'j', 'a', 'v', 'a', '.', 'l', 'a', 'n', 'g', '.', 'O', 'b', 'j', 'e', 'c', 't'}) -> {{'j', 'a', 'v', 'a'}, {'l', 'a', 'n', 'g'}, {'O', 'b', 'j', 'e', 'c', 't'}}
 * getSimpleNames({'O', 'b', 'j', 'e', 'c', 't'}) -> {{'O', 'b', 'j', 'e', 'c', 't'}}
 * getSimpleNames("") -> {}
 * </code>
 * </pre>
 *
 * @param name the name
 * @return the list of simple names, possibly empty
 * 
 * @since 2.0
 */
public static char[][] getSimpleNames(char[] name) {
	if (name.length == 0) {
		return NO_CHAR_CHAR;
	}
	int dot = CharOperation.indexOf(C_DOT, name);
	if (dot == -1) {
		return new char[][] {name};
	}
	int n = 1;
	while ((dot = CharOperation.indexOf(C_DOT, name, dot + 1)) != -1) {
		++n;
	}
	char[][] result = new char[n + 1][];
	int segStart = 0;
	for (int i = 0; i < n; ++i) {
		dot = CharOperation.indexOf(C_DOT, name, segStart);
		result[i] = CharOperation.subarray(name, segStart, dot);
		segStart = dot + 1;
	}
	result[n] = CharOperation.subarray(name, segStart, name.length);
	return result;
}
/**
 * Returns all segments of the given dot-separated qualified name.
 * Returns an array with only the given name if it is not qualified.
 * Returns an empty array if the name is empty.
 * <p>
 * For example:
 * <pre>
 * <code>
 * getSimpleNames("java.lang.Object") -> {"java", "lang", "Object"}
 * getSimpleNames("Object") -> {"Object"}
 * getSimpleNames("") -> {}
 * </code>
 * </pre>
 *
 * @param name the name
 * @return the list of simple names, possibly empty
 */
public static String[] getSimpleNames(String name) {
	char[][] simpleNames = getSimpleNames(name.toCharArray());
	int length = simpleNames.length;
	String[] result = new String[length];
	for (int i = 0; i < length; i++) {
		result[i] = new String(simpleNames[i]);
	}
	return result;
}
/**
 * Converts the given method signature to a readable form. The method signature is expected to
 * be dot-based.
 * <p>
 * For example:
 * <pre>
 * <code>
 * toString("([Ljava.lang.String;)V", "main", new String[] {"args"}, false, true) -> "void main(String[] args)"
 * </code>
 * </pre>
 * </p>
 * 
 * @param methodSignature the method signature to convert
 * @param methodName the name of the method to insert in the result, or 
 *   <code>null</code> if no method name is to be included
 * @param parameterNames the parameter names to insert in the result, or 
 *   <code>null</code> if no parameter names are to be included; if supplied,
 *   the number of parameter names must match that of the method signature
 * @param fullyQualifyTypeNames <code>true</code> if type names should be fully
 *   qualified, and <code>false</code> to use only simple names
 * @param includeReturnType <code>true</code> if the return type is to be
 *   included
 * @return the char array representation of the method signature
 * 
 * @since 2.0
 */
public static char[] toCharArray(char[] methodSignature, char[] methodName, char[][] parameterNames, boolean fullyQualifyTypeNames, boolean includeReturnType) {
	try {
		int firstParen = CharOperation.indexOf(C_PARAM_START, methodSignature);
		if (firstParen == -1) throw new IllegalArgumentException();
		
		int sigLength = methodSignature.length;
		
		// compute result length
		
		// method signature
		int paramCount = 0;
		int lastParen = -1;
		int resultLength = 0;
		signature: for (int i = firstParen; i < sigLength; i++) {
			switch (methodSignature[i]) {
				case C_ARRAY :
					resultLength += 2; // []
					continue signature;
				case C_BOOLEAN :
					resultLength += BOOLEAN.length;
					break;
				case C_BYTE :
					resultLength += BYTE.length;
					break;
				case C_CHAR :
					resultLength += CHAR.length;
					break;
				case C_DOUBLE :
					resultLength += DOUBLE.length;
					break;
				case C_FLOAT :
					resultLength += FLOAT.length;
					break;
				case C_INT :
					resultLength += INT.length;
					break;
				case C_LONG :
					resultLength += LONG.length;
					break;
				case C_SHORT :
					resultLength += SHORT.length;
					break;
				case C_VOID :
					resultLength += VOID.length;
					break;
				case C_RESOLVED :
				case C_UNRESOLVED :
					int end = CharOperation.indexOf(C_SEMICOLON, methodSignature, i);
					if (end == -1) throw new IllegalArgumentException();
					int start;
					if (fullyQualifyTypeNames) {
						start = i+1;
					} else {
						start = CharOperation.lastIndexOf(C_DOT, methodSignature, i, end) + 1;
						if (start == 0) start = i+1;
					} 
					resultLength += end-start;
					i = end;
					break;
				case C_PARAM_START :
					// add space for "("
					resultLength++;
					continue signature;
				case C_PARAM_END :
					lastParen = i;
					if (includeReturnType) {
						if (paramCount > 0) {
							// remove space for ", " that was added with last parameter and remove space that is going to be added for ", " after return type 
							// and add space for ") "
							resultLength -= 2;
						} //else
							// remove space that is going to be added for ", " after return type 
							// and add space for ") "
							// -> noop
						
						// decrement param count because it is going to be added for return type
						paramCount--;
						continue signature;
					} else {
						if (paramCount > 0) {
							// remove space for ", " that was added with last parameter and add space for ")"
							resultLength--;
						} else {
							// add space for ")"
							resultLength++;
						}
						break signature;
					}
				default :
					throw new IllegalArgumentException();
			}
			resultLength += 2; // add space for ", "
			paramCount++;
		}
		
		// parameter names
		int parameterNamesLength = parameterNames == null ? 0 : parameterNames.length;
		for (int i = 0; i <parameterNamesLength; i++) {
			resultLength += parameterNames[i].length + 1; // parameter name + space
		}
		
		// selector
		int selectorLength = methodName == null ? 0 : methodName.length;
		resultLength += selectorLength;
		
		// create resulting char array
		char[] result = new char[resultLength];
		
		// returned type
		int index = 0;
		if (includeReturnType) {
			long pos = copyType(methodSignature, lastParen+1, result, index, fullyQualifyTypeNames);
			index = (int) (pos >>> 32);
			result[index++] = ' ';
		}
		
		// selector
		if (methodName != null) {
			System.arraycopy(methodName, 0, result, index, selectorLength);
			index += selectorLength;
		}
		
		// parameters
		result[index++] = C_PARAM_START;
		int sigPos = firstParen+1;
		for (int i = 0; i < paramCount; i++) {
			long pos = copyType(methodSignature, sigPos, result, index, fullyQualifyTypeNames);
			index = (int) (pos >>> 32);
			sigPos = (int)pos;
			if (parameterNames != null) {
				result[index++] = ' ';
				char[] parameterName = parameterNames[i];
				int paramLength = parameterName.length;
				System.arraycopy(parameterName, 0, result, index, paramLength);
				index += paramLength;
			}
			if (i != paramCount-1) {
				result[index++] = ',';
				result[index++] = ' ';
			}
		}
		if (sigPos >= sigLength) {
			throw new IllegalArgumentException(); // should be on last paren
		}
		result[index++] = C_PARAM_END;
		
		return result;
	} catch (ArrayIndexOutOfBoundsException e) {
		throw new IllegalArgumentException();
	}		
}
/**
 * Converts the given type signature to a readable string. The signature is expected to
 * be dot-based.
 * 
 * <p>
 * For example:
 * <pre>
 * <code>
 * toString({'[', 'L', 'j', 'a', 'v', 'a', '.', 'l', 'a', 'n', 'g', '.', 'S', 't', 'r', 'i', 'n', 'g', ';'}) -> {'j', 'a', 'v', 'a', '.', 'l', 'a', 'n', 'g', '.', 'S', 't', 'r', 'i', 'n', 'g', '[', ']'}
 * toString({'I'}) -> {'i', 'n', 't'}
 * </code>
 * </pre>
 * </p>
 * <p>
 * Note: This method assumes that a type signature containing a <code>'$'</code>
 * is an inner type signature. While this is correct in most cases, someone could 
 * define a non-inner type name containing a <code>'$'</code>. Handling this 
 * correctly in all cases would have required resolving the signature, which 
 * generally not feasible.
 * </p>
 *
 * @param signature the type signature
 * @return the string representation of the type
 * @exception IllegalArgumentException if the signature is not syntactically
 *   correct
 * 
 * @since 2.0
 */
public static char[] toCharArray(char[] signature) throws IllegalArgumentException {
	try {
		int sigLength = signature.length;

		if (sigLength == 0 || signature[0] == C_PARAM_START) {
			return toCharArray(signature, NO_CHAR, null, true, true);
		}
		
		// compute result length
		int resultLength = 0;
		int index = -1;
		while (signature[++index] == C_ARRAY) {
			resultLength += 2; // []
		}
		switch (signature[index]) {
			case C_BOOLEAN :
				resultLength += BOOLEAN.length;
				break;
			case C_BYTE :
				resultLength += BYTE.length;
				break;
			case C_CHAR :
				resultLength += CHAR.length;
				break;
			case C_DOUBLE :
				resultLength += DOUBLE.length;
				break;
			case C_FLOAT :
				resultLength += FLOAT.length;
				break;
			case C_INT :
				resultLength += INT.length;
				break;
			case C_LONG :
				resultLength += LONG.length;
				break;
			case C_SHORT :
				resultLength += SHORT.length;
				break;
			case C_VOID :
				resultLength += VOID.length;
				break;
			case C_RESOLVED :
			case C_UNRESOLVED :
				int end = CharOperation.indexOf(C_SEMICOLON, signature, index);
				if (end == -1) throw new IllegalArgumentException();
				int start = index + 1;
				resultLength += end-start;
				break;
			default :
				throw new IllegalArgumentException();
		}
		
		char[] result = new char[resultLength];
		copyType(signature, 0, result, 0, true);

		/**
		 * Converts '$' separated type signatures into '.' separated type signature.
		 * NOTE: This assumes that the type signature is an inner type signature.
		 *       This is true in most cases, but someone can define a non-inner type 
		 *       name containing a '$'. However to tell the difference, we would have
		 *       to resolve the signature, which cannot be done at this point.
		 */
		CharOperation.replace(result, C_DOLLAR, C_DOT);

		return result;
	} catch (ArrayIndexOutOfBoundsException e) {
		throw new IllegalArgumentException();
	}	
}
/**
 * Converts the given array of qualified name segments to a qualified name.
 * <p>
 * For example:
 * <pre>
 * <code>
 * toQualifiedName({{'j', 'a', 'v', 'a'}, {'l', 'a', 'n', 'g'}, {'O', 'b', 'j', 'e', 'c', 't'}}) -> {'j', 'a', 'v', 'a', '.', 'l', 'a', 'n', 'g', '.', 'O', 'b', 'j', 'e', 'c', 't'}
 * toQualifiedName({{'O', 'b', 'j', 'e', 'c', 't'}}) -> {'O', 'b', 'j', 'e', 'c', 't'}
 * toQualifiedName({{}}) -> {}
 * </code>
 * </pre>
 * </p>
 *
 * @param segments the list of name segments, possibly empty
 * @return the dot-separated qualified name, or the empty string
 * 
 * @since 2.0
 */
public static char[] toQualifiedName(char[][] segments) {
	int length = segments.length;
	if (length == 0) return NO_CHAR;
	if (length == 1) return segments[0];
	
	int resultLength = 0;
	for (int i = 0; i < length; i++) {
		resultLength += segments[i].length+1;
	}
	resultLength--;
	char[] result = new char[resultLength];
	int index = 0;
	for (int i = 0; i < length; i++) {
		char[] segment = segments[i];
		int segmentLength = segment.length;
		System.arraycopy(segment, 0, result, index, segmentLength);
		index += segmentLength;
		if (i != length-1) {
			result[index++] = C_DOT;
		}
	}
	return result;
}
/**
 * Converts the given array of qualified name segments to a qualified name.
 * <p>
 * For example:
 * <pre>
 * <code>
 * toQualifiedName(new String[] {"java", "lang", "Object"}) -> "java.lang.Object"
 * toQualifiedName(new String[] {"Object"}) -> "Object"
 * toQualifiedName(new String[0]) -> ""
 * </code>
 * </pre>
 * </p>
 *
 * @param segments the list of name segments, possibly empty
 * @return the dot-separated qualified name, or the empty string
 */
public static String toQualifiedName(String[] segments) {
	int length = segments.length;
	char[][] charArrays = new char[length][];
	for (int i = 0; i < length; i++) {
		charArrays[i] = segments[i].toCharArray();
	}
	return new String(toQualifiedName(charArrays));
}
/**
 * Converts the given type signature to a readable string. The signature is expected to
 * be dot-based.
 * 
 * <p>
 * For example:
 * <pre>
 * <code>
 * toString("[Ljava.lang.String;") -> "java.lang.String[]"
 * toString("I") -> "int"
 * </code>
 * </pre>
 * </p>
 * <p>
 * Note: This method assumes that a type signature containing a <code>'$'</code>
 * is an inner type signature. While this is correct in most cases, someone could 
 * define a non-inner type name containing a <code>'$'</code>. Handling this 
 * correctly in all cases would have required resolving the signature, which 
 * generally not feasible.
 * </p>
 *
 * @param signature the type signature
 * @return the string representation of the type
 * @exception IllegalArgumentException if the signature is not syntactically
 *   correct
 */
public static String toString(String signature) throws IllegalArgumentException {
	return new String(toCharArray(signature.toCharArray()));
}
/**
 * Converts the given method signature to a readable string. The method signature is expected to
 * be dot-based.
 * <p>
 * For example:
 * <pre>
 * <code>
 * toString("([Ljava.lang.String;)V", "main", new String[] {"args"}, false, true) -> "void main(String[] args)"
 * </code>
 * </pre>
 * </p>
 * 
 * @param methodSignature the method signature to convert
 * @param methodName the name of the method to insert in the result, or 
 *   <code>null</code> if no method name is to be included
 * @param parameterNames the parameter names to insert in the result, or 
 *   <code>null</code> if no parameter names are to be included; if supplied,
 *   the number of parameter names must match that of the method signature
 * @param fullyQualifyTypeNames <code>true</code> if type names should be fully
 *   qualified, and <code>false</code> to use only simple names
 * @param includeReturnType <code>true</code> if the return type is to be
 *   included
 * @return the string representation of the method signature
 */
public static String toString(String methodSignature, String methodName, String[] parameterNames, boolean fullyQualifyTypeNames, boolean includeReturnType) {
	char[][] params;
	if (parameterNames == null) {
		params = null;
	} else {
		int paramLength = parameterNames.length;
		params = new char[paramLength][];
		for (int i = 0; i < paramLength; i++) {
			params[i] = parameterNames[i].toCharArray();
		}
	}
	return new String(toCharArray(methodSignature.toCharArray(), methodName == null ? null : methodName.toCharArray(), params, fullyQualifyTypeNames, includeReturnType));
}
}
