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
import org.eclipse.jdt.core.util.ILineNumberAttribute;

/**
 * Default implementation of ILineNumberAttribute.
 */
public class LineNumberAttribute
	extends ClassFileAttribute
	implements ILineNumberAttribute {

	private static final int[][] NO_ENTRIES = new int[0][0];
	private int lineNumberTableLength;
	private int[][] lineNumberTable;
	
	/**
	 * Constructor for LineNumberAttribute.
	 * @param classFileBytes
	 * @param constantPool
	 * @param offset
	 * @throws ClassFormatException
	 */
	public LineNumberAttribute(
		byte[] classFileBytes,
		IConstantPool constantPool,
		int offset)
		throws ClassFormatException {
		super(classFileBytes, constantPool, offset);
		
		this.lineNumberTableLength = u2At(classFileBytes, 6, offset);
		this.lineNumberTable = NO_ENTRIES;
		if (this.lineNumberTableLength != 0) {
			this.lineNumberTable = new int[this.lineNumberTableLength][2];
		}
		int readOffset = 8;
		for (int i = 0, max = this.lineNumberTableLength; i < max; i++) {
			this.lineNumberTable[i][0] = u2At(classFileBytes, readOffset, offset);
			this.lineNumberTable[i][1] = u2At(classFileBytes, readOffset + 2, offset);
			readOffset += 4;
		}
	}

	/**
	 * @see org.eclipse.jdt.core.util.IClassFileAttribute#getAttributeName()
	 */
	public char[] getAttributeName() {
		return IAttributeNamesConstants.LINE_NUMBER;
	}

	/**
	 * @see ILineNumberAttribute#getLineNumberTable()
	 */
	public int[][] getLineNumberTable() {
		return this.lineNumberTable;
	}

	/**
	 * @see ILineNumberAttribute#getLineNumberTableLength()
	 */
	public int getLineNumberTableLength() {
		return this.lineNumberTableLength;
	}

}
