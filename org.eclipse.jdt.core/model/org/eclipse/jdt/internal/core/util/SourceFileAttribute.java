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
import org.eclipse.jdt.core.util.ISourceAttribute;

/**
 * Default implementation of ISourceAttribute
 */
public class SourceFileAttribute
	extends ClassFileAttribute
	implements ISourceAttribute {

	private int sourceFileIndex;
	private char[] sourceFileName;
	
	/**
	 * Constructor for SourceFileAttribute.
	 * @param classFileBytes
	 * @param constantPool
	 * @param offset
	 * @throws ClassFormatException
	 */
	public SourceFileAttribute(
		byte[] classFileBytes,
		IConstantPool constantPool,
		int offset)
		throws ClassFormatException {
		super(classFileBytes, constantPool, offset);
		this.sourceFileIndex = u2At(classFileBytes, 6, offset);
		IConstantPoolEntry constantPoolEntry = constantPool.decodeEntry(this.sourceFileIndex);
		if (constantPoolEntry.getKind() != IConstantPoolConstant.CONSTANT_Utf8) {
			throw new ClassFormatException(ClassFormatException.INVALID_CONSTANT_POOL_ENTRY);
		}
		this.sourceFileName = constantPoolEntry.getUtf8Value();
	}

	/**
	 * @see org.eclipse.jdt.core.util.IClassFileAttribute#getAttributeName()
	 */
	public char[] getAttributeName() {
		return IAttributeNamesConstants.SOURCE;
	}

	/**
	 * @see ISourceAttribute#getSourceFileIndex()
	 */
	public int getSourceFileIndex() {
		return this.sourceFileIndex;
	}

	/**
	 * @see ISourceAttribute#getSourceFileName()
	 */
	public char[] getSourceFileName() {
		return this.sourceFileName;
	}

}
