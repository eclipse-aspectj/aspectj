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
import org.eclipse.jdt.core.util.IClassFileAttribute;
import org.eclipse.jdt.core.util.IConstantPool;
import org.eclipse.jdt.core.util.IConstantPoolConstant;
import org.eclipse.jdt.core.util.IConstantPoolEntry;

/**
 * Default implementation of IClassFileAttribute
 */
public class ClassFileAttribute extends ClassFileStruct implements IClassFileAttribute {
	public static final IClassFileAttribute[] NO_ATTRIBUTES = new IClassFileAttribute[0];
	private long attributeLength;
	private int attributeNameIndex;
	private char[] attributeName;
	
	public ClassFileAttribute(byte[] classFileBytes, IConstantPool constantPool, int offset) throws ClassFormatException {
		this.attributeNameIndex = u2At(classFileBytes, 0, offset);
		this.attributeLength = u4At(classFileBytes, 2, offset);
		IConstantPoolEntry constantPoolEntry = constantPool.decodeEntry(this.attributeNameIndex);
		if (constantPoolEntry.getKind() != IConstantPoolConstant.CONSTANT_Utf8) {
			throw new ClassFormatException(ClassFormatException.INVALID_CONSTANT_POOL_ENTRY);
		}
		this.attributeName = constantPoolEntry.getUtf8Value();
	}

	public int getAttributeNameIndex() {
		return this.attributeNameIndex;
	}

	/**
	 * @see IClassFileAttribute#getAttributeName()
	 */
	public char[] getAttributeName() {
		return this.attributeName;
	}

	/**
	 * @see IClassFileAttribute#getAttributeLength()
	 */
	public long getAttributeLength() {
		return this.attributeLength;
	}

}
