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
package org.eclipse.jdt.internal.compiler.classfmt;

import org.eclipse.jdt.internal.compiler.codegen.AttributeNamesConstants;
import org.eclipse.jdt.internal.compiler.env.IBinaryMethod;
import org.eclipse.jdt.internal.compiler.util.CharOperation;

public class MethodInfo extends ClassFileStruct implements IBinaryMethod, AttributeNamesConstants, Comparable {
	private char[][] exceptionNames;
	private int[] constantPoolOffsets;
	private boolean isDeprecated;
	private boolean isSynthetic;
	private int accessFlags;
	private char[] name;
	private char[] signature;
	private int attributesCount;
	private int attributeBytes;
	static private final char[][] noException = new char[0][0];
	private int decodeIndex;
/**
 * @param classFileBytes byte[]
 * @param offsets int[]
 * @param offset int
 */
public MethodInfo (byte classFileBytes[], int offsets[], int offset) throws ClassFormatException {
	super(classFileBytes, offset);
	constantPoolOffsets = offsets;
	accessFlags = -1;
	int attributesCount = u2At(6);
	int readOffset = 8;
	for (int i = 0; i < attributesCount; i++) {
		readOffset += (6 + u4At(readOffset + 2));
	}
	attributeBytes = readOffset;
}
/**
 * @see IGenericMethod#getArgumentNames()
 */
public char[][] getArgumentNames() {
	return null;
}
/**
 * Answer the resolved names of the exception types in the
 * class file format as specified in section 4.2 of the Java 2 VM spec
 * or null if the array is empty.
 *
 * For example, java.lang.String is java/lang/String.
 * @return char[][]
 */
public char[][] getExceptionTypeNames() {
	if (exceptionNames == null) {
		readExceptionAttributes();
	}
	return exceptionNames;
}
/**
 * Answer the receiver's method descriptor which describes the parameter &
 * return types as specified in section 4.3.3 of the Java 2 VM spec.
 *
 * For example:
 *   - int foo(String) is (Ljava/lang/String;)I
 *   - void foo(Object[]) is (I)[Ljava/lang/Object;
 * @return char[]
 */
public char[] getMethodDescriptor() {
	if (signature == null) {
		// read the name
		int utf8Offset = constantPoolOffsets[u2At(4)] - structOffset;
		signature = utf8At(utf8Offset + 3, u2At(utf8Offset + 1));
	}
	return signature;
}
/**
 * Answer an int whose bits are set according the access constants
 * defined by the VM spec.
 * Set the AccDeprecated and AccSynthetic bits if necessary
 * @return int
 */
public int getModifiers() {
	if (accessFlags == -1) {
		// compute the accessflag. Don't forget the deprecated attribute
		accessFlags = u2At(0);
		readDeprecatedAndSyntheticAttributes();
		if (isDeprecated) {
			accessFlags |= AccDeprecated;
		}
		if (isSynthetic) {
			accessFlags |= AccSynthetic;
		}
	}
	return accessFlags;
}
/**
 * Answer the name of the method.
 *
 * For a constructor, answer <init> & <clinit> for a clinit method.
 * @return char[]
 */
public char[] getSelector() {
	if (name == null) {
		// read the name
		int utf8Offset = constantPoolOffsets[u2At(2)] - structOffset;
		name = utf8At(utf8Offset + 3, u2At(utf8Offset + 1));
	}
	return name;
}
/**
 * Answer true if the method is a class initializer, false otherwise.
 * @return boolean
 */
public boolean isClinit() {
	char[] selector = getSelector();
	return selector[0] == '<' && selector.length == 8; // Can only match <clinit>
}
/**
 * Answer true if the method is a constructor, false otherwise.
 * @return boolean
 */
public boolean isConstructor() {
	char[] selector = getSelector();
	return selector[0] == '<' && selector.length == 6; // Can only match <init>
}
/**
 * Return true if the field is a synthetic method, false otherwise.
 * @return boolean
 */
public boolean isSynthetic() {
	return (getModifiers() & AccSynthetic) != 0;
}
private void readDeprecatedAndSyntheticAttributes() {
	int attributesCount = u2At(6);
	int readOffset = 8;
	for (int i = 0; i < attributesCount; i++) {
		int utf8Offset = constantPoolOffsets[u2At(readOffset)] - structOffset;
		char[] attributeName = utf8At(utf8Offset + 3, u2At(utf8Offset + 1));
		if (CharOperation.equals(attributeName, DeprecatedName)) {
			isDeprecated = true;
		} else if (CharOperation.equals(attributeName, SyntheticName)) {
			isSynthetic = true;
		}
		readOffset += (6 + u4At(readOffset + 2));
	}
}
private void readExceptionAttributes() {
	int attributesCount = u2At(6);
	int readOffset = 8;
	for (int i = 0; i < attributesCount; i++) {
		int utf8Offset = constantPoolOffsets[u2At(readOffset)] - structOffset;
		char[] attributeName = utf8At(utf8Offset + 3, u2At(utf8Offset + 1));
		if (CharOperation.equals(attributeName, ExceptionsName)) {
			// read the number of exception entries
			int entriesNumber = u2At(readOffset + 6);
			// place the readOffset at the beginning of the exceptions table
			readOffset += 8;
			if (entriesNumber == 0) {
				exceptionNames = noException;
			} else {
				exceptionNames = new char[entriesNumber][];
				for (int j = 0; j < entriesNumber; j++) {
					utf8Offset = 
						constantPoolOffsets[u2At(
							constantPoolOffsets[u2At(readOffset)] - structOffset + 1)]
							- structOffset; 
					exceptionNames[j] = utf8At(utf8Offset + 3, u2At(utf8Offset + 1));
					readOffset += 2;
				}
			}
		} else {
			readOffset += (6 + u4At(readOffset + 2));
		}
	}
	if (exceptionNames == null) {
		exceptionNames = noException;
	}
}
/**
 * Answer the size of the receiver in bytes.
 * 
 * @return int
 */
public int sizeInBytes() {
	return attributeBytes;
}
public String toString() {
	int modifiers = getModifiers();
	StringBuffer buffer = new StringBuffer(this.getClass().getName());
	return buffer
		.append("{") //$NON-NLS-1$
		.append(
			((modifiers & AccDeprecated) != 0 ? "deprecated " : "") //$NON-NLS-1$ //$NON-NLS-2$
				+ ((modifiers & 0x0001) == 1 ? "public " : "") //$NON-NLS-1$ //$NON-NLS-2$
				+ ((modifiers & 0x0002) == 0x0002 ? "private " : "") //$NON-NLS-1$ //$NON-NLS-2$
				+ ((modifiers & 0x0004) == 0x0004 ? "protected " : "") //$NON-NLS-1$ //$NON-NLS-2$
				+ ((modifiers & 0x0008) == 0x000008 ? "static " : "") //$NON-NLS-1$ //$NON-NLS-2$
				+ ((modifiers & 0x0010) == 0x0010 ? "final " : "") //$NON-NLS-1$ //$NON-NLS-2$
				+ ((modifiers & 0x0040) == 0x0040 ? "volatile " : "") //$NON-NLS-1$ //$NON-NLS-2$
				+ ((modifiers & 0x0080) == 0x0080 ? "transient " : "")) //$NON-NLS-1$ //$NON-NLS-2$
		.append(getSelector())
		.append(getMethodDescriptor())
		.append("}") //$NON-NLS-1$
		.toString(); 
}
public int compareTo(Object o) {
	if (!(o instanceof MethodInfo)) {
		throw new ClassCastException();
	}

	MethodInfo otherMethod = (MethodInfo) o;
	int result = new String(this.getSelector()).compareTo(new String(otherMethod.getSelector()));
	if (result != 0) return result;
	return new String(this.getMethodDescriptor()).compareTo(new String(otherMethod.getMethodDescriptor()));
}

/**
 * This method is used to fully initialize the contents of the receiver. All methodinfos, fields infos
 * will be therefore fully initialized and we can get rid of the bytes.
 */
void initialize() {
	getModifiers();
	getSelector();
	getMethodDescriptor();
	getExceptionTypeNames();
	reset();
}
protected void reset() {
	this.constantPoolOffsets = null;
	super.reset();
}
}