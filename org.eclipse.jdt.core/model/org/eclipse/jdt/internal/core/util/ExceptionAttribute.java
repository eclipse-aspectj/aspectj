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
package org.eclipse.jdt.internal.core.util;

import org.eclipse.jdt.core.util.ClassFormatException;
import org.eclipse.jdt.core.util.IAttributeNamesConstants;
import org.eclipse.jdt.core.util.IConstantPool;
import org.eclipse.jdt.core.util.IConstantPoolConstant;
import org.eclipse.jdt.core.util.IConstantPoolEntry;
import org.eclipse.jdt.core.util.IExceptionAttribute;

/**
 * Default implementation of IExceptionAttribute.
 */
public class ExceptionAttribute extends ClassFileAttribute implements IExceptionAttribute {
	private static final int[] NO_EXCEPTION_INDEXES = new int[0];
	private static final char[][] NO_EXCEPTION_NAMES = new char[0][0];
	
	private int exceptionsNumber;
	private char[][] exceptionNames;
	private int[] exceptionIndexes;
	
	ExceptionAttribute(byte[] classFileBytes, IConstantPool constantPool, int offset) throws ClassFormatException {
		super(classFileBytes, constantPool, offset);
		this.exceptionsNumber = u2At(classFileBytes, 6, offset);
		int exceptionLength = this.exceptionsNumber;
		this.exceptionNames = NO_EXCEPTION_NAMES;
		this.exceptionIndexes = NO_EXCEPTION_INDEXES;
		if (exceptionLength != 0) {
			this.exceptionNames = new char[exceptionLength][];
			this.exceptionIndexes = new int[exceptionLength];
		}
		int readOffset = 8;
		IConstantPoolEntry constantPoolEntry;
		for (int i = 0; i < exceptionLength; i++) {
			exceptionIndexes[i] = u2At(classFileBytes, readOffset, offset);
			constantPoolEntry = constantPool.decodeEntry(exceptionIndexes[i]);
			if (constantPoolEntry.getKind() != IConstantPoolConstant.CONSTANT_Class) {
				throw new ClassFormatException(ClassFormatException.INVALID_CONSTANT_POOL_ENTRY);
			}
			exceptionNames[i] = constantPoolEntry.getClassInfoName();
			readOffset += 2;
		}
	}
	
	/**
	 * @see IExceptionAttribute#getExceptionIndexes()
	 */
	public int[] getExceptionIndexes() {
		return this.exceptionIndexes;
	}

	/**
	 * @see IExceptionAttribute#getExceptionNames()
	 */
	public char[][] getExceptionNames() {
		return this.exceptionNames;
	}

	/**
	 * @see IExceptionAttribute#getExceptionsNumber()
	 */
	public int getExceptionsNumber() {
		return this.exceptionsNumber;
	}

	/**
	 * @see org.eclipse.jdt.core.util.IClassFileAttribute#getAttributeName()
	 */
	public char[] getAttributeName() {
		return IAttributeNamesConstants.EXCEPTIONS;
	}

}
