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
import org.eclipse.jdt.core.util.ILocalVariableAttribute;
import org.eclipse.jdt.core.util.ILocalVariableTableEntry;

/**
 * Default implementation of ILocalVariableAttribute.
 */
public class LocalVariableAttribute
	extends ClassFileAttribute
	implements ILocalVariableAttribute {

	private static final ILocalVariableTableEntry[] NO_ENTRIES = new ILocalVariableTableEntry[0];
	private int localVariableTableLength;
	private ILocalVariableTableEntry[] localVariableTable;
	
	/**
	 * Constructor for LocalVariableAttribute.
	 * @param classFileBytes
	 * @param constantPool
	 * @param offset
	 * @throws ClassFormatException
	 */
	public LocalVariableAttribute(
		byte[] classFileBytes,
		IConstantPool constantPool,
		int offset)
		throws ClassFormatException {
		super(classFileBytes, constantPool, offset);
		this.localVariableTableLength = u2At(classFileBytes, 6, offset);
		int readOffset = 8;
		int length = this.localVariableTableLength;
		this.localVariableTable = NO_ENTRIES;
		if (length != 0) {
			this.localVariableTable = new LocalVariableTableEntry[length];
		}
		for (int i = 0; i < length; i++) {
			this.localVariableTable[i] = new LocalVariableTableEntry(classFileBytes, constantPool, offset + readOffset);
			readOffset += 10;
		}
	}

	/**
	 * @see org.eclipse.jdt.core.util.IClassFileAttribute#getAttributeName()
	 */
	public char[] getAttributeName() {
		return IAttributeNamesConstants.LOCAL_VARIABLE;
	}

	/**
	 * @see ILocalVariableAttribute#getLocalVariableTable()
	 */
	public ILocalVariableTableEntry[] getLocalVariableTable() {
		return this.localVariableTable;
	}

	/**
	 * @see ILocalVariableAttribute#getLocalVariableTableLength()
	 */
	public int getLocalVariableTableLength() {
		return this.localVariableTableLength;
	}

}
