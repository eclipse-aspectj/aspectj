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
import org.eclipse.jdt.core.util.IConstantPool;
import org.eclipse.jdt.core.util.IInnerClassesAttribute;
import org.eclipse.jdt.core.util.IInnerClassesAttributeEntry;

/**
 * Default implementation of IInnerClassesAttribute.
 */
public class InnerClassesAttribute extends ClassFileAttribute implements IInnerClassesAttribute {		
	private static final IInnerClassesAttributeEntry[] NO_ENTRIES = new IInnerClassesAttributeEntry[0];

	private int numberOfClasses;
	private IInnerClassesAttributeEntry[] entries;
	/**
	 * Constructor for InnerClassesAttribute.
	 * @param classFileBytes
	 * @param constantPool
	 * @param offset
	 * @throws ClassFormatException
	 */
	public InnerClassesAttribute(
		byte[] classFileBytes,
		IConstantPool constantPool,
		int offset)
		throws ClassFormatException {
		super(classFileBytes, constantPool, offset);
		this.numberOfClasses = u2At(classFileBytes, 6, offset);
		int readOffset = 8;
		int length = this.numberOfClasses;
		this.entries = NO_ENTRIES;
		if (length != 0) {
			this.entries = new IInnerClassesAttributeEntry[length];
		}
		for (int i = 0; i < length; i++) {
			this.entries[i] = new InnerClassesAttributeEntry(classFileBytes, constantPool, offset + readOffset);
			readOffset += 8;
		}		
	}

	/**
	 * @see IInnerClassesAttribute#getInnerClassAttributesEntries()
	 */
	public IInnerClassesAttributeEntry[] getInnerClassAttributesEntries() {
		return this.entries;
	}

	/**
	 * @see IInnerClassesAttribute#getNumberOfClasses()
	 */
	public int getNumberOfClasses() {
		return this.numberOfClasses;
	}

}