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

abstract public class ClassFileStruct implements ClassFileConstants {
	byte[] reference;
	int structOffset;
public ClassFileStruct(byte classFileBytes[], int off) {
	reference = classFileBytes;
	structOffset = off;
}
public ClassFileStruct (byte classFileBytes[], int off, boolean verifyStructure) {
	reference = classFileBytes;
	structOffset = off;
}
public double doubleAt(int relativeOffset) {
	return (Double.longBitsToDouble(this.i8At(relativeOffset)));
}
public float floatAt(int relativeOffset) {
	return (Float.intBitsToFloat(this.i4At(relativeOffset)));
}
public int i1At(int relativeOffset) {
	return reference[relativeOffset + structOffset];
}
public int i2At(int relativeOffset) {
	int position = relativeOffset + structOffset;
	return (reference[position++] << 8) + (reference[position] & 0xFF);
}
public int i4At(int relativeOffset) {
	int position = relativeOffset + structOffset;
	return ((reference[position++] & 0xFF) << 24) + ((reference[position++] & 0xFF) << 16) + ((reference[position++] & 0xFF) << 8) + (reference[position] & 0xFF);
}
public long i8At(int relativeOffset) {
	int position = relativeOffset + structOffset;
	return (((long) (reference[position++] & 0xFF)) << 56) + (((long) (reference[position++] & 0xFF)) << 48) + (((long) (reference[position++] & 0xFF)) << 40) + (((long) (reference[position++] & 0xFF)) << 32) + (((long) (reference[position++] & 0xFF)) << 24) + (((long) (reference[position++] & 0xFF)) << 16) + (((long) (reference[position++] & 0xFF)) << 8) + ((long) (reference[position++] & 0xFF));
}
public static String printTypeModifiers(int modifiers) {

	java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
	java.io.PrintWriter print = new java.io.PrintWriter(out);

	if ((modifiers & AccPublic) != 0) print.print("public "); //$NON-NLS-1$
	if ((modifiers & AccPrivate) != 0) print.print("private "); //$NON-NLS-1$
	if ((modifiers & AccFinal) != 0) print.print("final "); //$NON-NLS-1$
	if ((modifiers & AccSuper) != 0) print.print("super "); //$NON-NLS-1$
	if ((modifiers & AccInterface) != 0) print.print("interface "); //$NON-NLS-1$
	if ((modifiers & AccAbstract) != 0) print.print("abstract "); //$NON-NLS-1$
	print.flush();
	return out.toString();
}
public int u1At(int relativeOffset) {
	return (reference[relativeOffset + structOffset] & 0xFF);
}
public int u2At(int relativeOffset) {
	int position = relativeOffset + structOffset;
	return ((reference[position++] & 0xFF) << 8) + (reference[position] & 0xFF);
}
public long u4At(int relativeOffset) {
	int position = relativeOffset + structOffset;
	return (((reference[position++] & 0xFFL) << 24) + ((reference[position++] & 0xFF) << 16) + ((reference[position++] & 0xFF) << 8) + (reference[position] & 0xFF));
}
public char[] utf8At(int relativeOffset, int bytesAvailable) {
	int x, y, z;
	int length = bytesAvailable;
	char outputBuf[] = new char[bytesAvailable];
	int outputPos = 0;
	int readOffset = structOffset + relativeOffset;
	
	while (length != 0) {
		x = reference[readOffset++] & 0xFF;
		length--;
		if ((0x80 & x) != 0) {
			y = this.reference[readOffset++] & 0xFF;
			length--;
			if ((x & 0x20) != 0) {
				z = this.reference[readOffset++] & 0xFF;
				length--;
				x = ((x & 0x1F) << 12) + ((y & 0x3F) << 6) + (z & 0x3F);
			} else {
				x = ((x & 0x1F) << 6) + (y & 0x3F);
			}
		}
		outputBuf[outputPos++] = (char) x;
	}

	if (outputPos != bytesAvailable) {
		System.arraycopy(outputBuf, 0, (outputBuf = new char[outputPos]), 0, outputPos);
	}
	return outputBuf;
}

protected void reset() {
	this.reference = null;
}

public char[] utf8At(int relativeOffset, int bytesAvailable, boolean testValidity) throws ClassFormatException {
	int x, y, z;
	int length = bytesAvailable;
	char outputBuf[] = new char[bytesAvailable];
	int outputPos = 0;
	int readOffset = structOffset + relativeOffset;
	
	while (length != 0) {
		x = reference[readOffset++] & 0xFF;
		length--;
		if ((0x80 & x) != 0) {
			if (testValidity) {
				if ((0x40 & x) == 0) {
					throw new ClassFormatException(ClassFormatException.ErrMalformedUtf8);
				}
				if (length < 1) {
					throw new ClassFormatException(ClassFormatException.ErrMalformedUtf8);
				}
			}
			y = this.reference[readOffset++] & 0xFF;
			length--;
			if (testValidity) {
				if ((y & 0xC0) != 0x80) {
					throw new ClassFormatException(ClassFormatException.ErrMalformedUtf8);
				}
			}
			if ((x & 0x20) != 0) {
				if (testValidity && (length < 1)) {
					throw new ClassFormatException(ClassFormatException.ErrMalformedUtf8);
				}
				z = this.reference[readOffset++] & 0xFF;
				length--;
				if (testValidity && ((z & 0xC0) != 0x80)) {
					throw new ClassFormatException(ClassFormatException.ErrMalformedUtf8);
				}
				x = ((x & 0x1F) << 12) + ((y & 0x3F) << 6) + (z & 0x3F);
				if (testValidity && (x < 0x0800)) {
					throw new ClassFormatException(ClassFormatException.ErrMalformedUtf8);
				}
			} else {
				x = ((x & 0x1F) << 6) + (y & 0x3F);
				if (testValidity && !((x == 0) || (x >= 0x80))) {
					throw new ClassFormatException(ClassFormatException.ErrMalformedUtf8);
				}
			}
		} else {
			if (testValidity && x == 0) {
					throw new ClassFormatException(ClassFormatException.ErrMalformedUtf8);
			}
		}
		outputBuf[outputPos++] = (char) x;
	}

	if (outputPos != bytesAvailable) {
		System.arraycopy(outputBuf, 0, (outputBuf = new char[outputPos]), 0, outputPos);
	}
	return outputBuf;
}
public static void verifyMethodNameAndSignature(char[] name, char[] signature) throws ClassFormatException {

	// ensure name is not empty 
	if (name.length == 0) {
		throw new ClassFormatException(ClassFormatException.ErrInvalidMethodName);
	}

	// if name begins with the < character it must be clinit or init
	if (name[0] == '<') {
		if (new String(name).equals("<clinit>") || new String(name).equals("<init>")) { //$NON-NLS-2$ //$NON-NLS-1$
			int signatureLength = signature.length;
			if (!((signatureLength > 2)
				&& (signature[0] == '(')
				&& (signature[signatureLength - 2] == ')')
				&& (signature[signatureLength - 1] == 'V'))) {
				throw new ClassFormatException(ClassFormatException.ErrInvalidMethodSignature);
			}
		} else {
			throw new ClassFormatException(ClassFormatException.ErrInvalidMethodName);
		}
	}
}
}
