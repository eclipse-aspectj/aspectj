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
import org.eclipse.jdt.core.util.IClassFileAttribute;
import org.eclipse.jdt.core.util.IClassFileReader;
import org.eclipse.jdt.core.util.ICodeAttribute;
import org.eclipse.jdt.core.util.IConstantPool;
import org.eclipse.jdt.core.util.IConstantPoolConstant;
import org.eclipse.jdt.core.util.IConstantPoolEntry;
import org.eclipse.jdt.core.util.IExceptionAttribute;
import org.eclipse.jdt.core.util.IMethodInfo;

/**
 * Default implementation of IMethodInfo.
 */
public class MethodInfo extends ClassFileStruct implements IMethodInfo {
	private boolean isDeprecated;
	private boolean isSynthetic;
	private int accessFlags;
	private char[] name;
	private char[] descriptor;
	private int nameIndex;
	private int descriptorIndex;
	private int attributesCount;
	private int attributeBytes;
	private ICodeAttribute codeAttribute;
	private IExceptionAttribute exceptionAttribute;
	private IClassFileAttribute[] attributes;
	
	/**
	 * @param classFileBytes byte[]
	 * @param offsets int[]
	 * @param offset int
	 * @param decodingFlags int
	 */
	public MethodInfo(byte classFileBytes[], IConstantPool constantPool, int offset, int decodingFlags)
		throws ClassFormatException {
			
		boolean no_code_attribute = (decodingFlags & IClassFileReader.METHOD_BODIES) == 0;
		accessFlags = u2At(classFileBytes, 0, offset);
		
		this.nameIndex = u2At(classFileBytes, 2, offset);
		IConstantPoolEntry constantPoolEntry = constantPool.decodeEntry(this.nameIndex);
		if (constantPoolEntry.getKind() != IConstantPoolConstant.CONSTANT_Utf8) {
			throw new ClassFormatException(ClassFormatException.INVALID_CONSTANT_POOL_ENTRY);
		}
		this.name = constantPoolEntry.getUtf8Value();
		
		this.descriptorIndex = u2At(classFileBytes, 4, offset);
		constantPoolEntry = constantPool.decodeEntry(this.descriptorIndex);
		if (constantPoolEntry.getKind() != IConstantPoolConstant.CONSTANT_Utf8) {
			throw new ClassFormatException(ClassFormatException.INVALID_CONSTANT_POOL_ENTRY);
		}
		this.descriptor = constantPoolEntry.getUtf8Value();
		
		this.attributesCount = u2At(classFileBytes, 6, offset);
		this.attributes = ClassFileAttribute.NO_ATTRIBUTES;
		if (this.attributesCount != 0) {
			if (no_code_attribute) {
				if (this.attributesCount != 1) {
					this.attributes = new IClassFileAttribute[this.attributesCount - 1];
				}
			} else {
				this.attributes = new IClassFileAttribute[this.attributesCount];
			}
		}
		int attributesIndex = 0;
		int readOffset = 8;
		for (int i = 0; i < this.attributesCount; i++) {
			constantPoolEntry = constantPool.decodeEntry(u2At(classFileBytes, readOffset, offset));
			if (constantPoolEntry.getKind() != IConstantPoolConstant.CONSTANT_Utf8) {
				throw new ClassFormatException(ClassFormatException.INVALID_CONSTANT_POOL_ENTRY);
			}
			char[] attributeName = constantPoolEntry.getUtf8Value();
			if (equals(attributeName, IAttributeNamesConstants.DEPRECATED)) {
				this.isDeprecated = true;
				this.attributes[attributesIndex++] = new ClassFileAttribute(classFileBytes, constantPool, offset + readOffset);
			} else if (equals(attributeName, IAttributeNamesConstants.SYNTHETIC)) {
				this.isSynthetic = true;
				this.attributes[attributesIndex++] = new ClassFileAttribute(classFileBytes, constantPool, offset + readOffset);
			} else if (equals(attributeName, IAttributeNamesConstants.CODE)) {
				if (!no_code_attribute) {
					this.codeAttribute = new CodeAttribute(classFileBytes, constantPool, offset + readOffset);
					this.attributes[attributesIndex++] = this.codeAttribute;
				}
			} else if (equals(attributeName, IAttributeNamesConstants.EXCEPTIONS)) {
				this.exceptionAttribute = new ExceptionAttribute(classFileBytes, constantPool, offset + readOffset);
				this.attributes[attributesIndex++] = this.exceptionAttribute;
			} else {
				this.attributes[attributesIndex++] = new ClassFileAttribute(classFileBytes, constantPool, offset + readOffset);
			}
			readOffset += (6 + u4At(classFileBytes, readOffset + 2, offset));
		}
		attributeBytes = readOffset;
	}
	/**
	 * @see IMethodInfo#getAccessFlags()
	 */
	public int getAccessFlags() {
		return this.accessFlags;
	}

	/**
	 * @see IMethodInfo#getCodeAttribute()
	 */
	public ICodeAttribute getCodeAttribute() {
		return this.codeAttribute;
	}

	/**
	 * @see IMethodInfo#getDescriptor()
	 */
	public char[] getDescriptor() {
		return this.descriptor;
	}

	/**
	 * @see IMethodInfo#getName()
	 */
	public char[] getName() {
		return this.name;
	}

	/**
	 * @see IMethodInfo#isClinit()
	 */
	public boolean isClinit() {
		return name[0] == '<' && name.length == 8; // Can only match <clinit>
	}

	/**
	 * @see IMethodInfo#isConstructor()
	 */
	public boolean isConstructor() {
		return name[0] == '<' && name.length == 6; // Can only match <init>
	}

	/**
	 * @see IMethodInfo#isDeprecated()
	 */
	public boolean isDeprecated() {
		return this.isDeprecated;
	}

	/**
	 * @see IMethodInfo#isSynthetic()
	 */
	public boolean isSynthetic() {
		return this.isSynthetic;
	}

	/**
	 * @see IMethodInfo#getExceptionAttribute()
	 */
	public IExceptionAttribute getExceptionAttribute() {
		return this.exceptionAttribute;
	}

	/**
	 * @see IMethodInfo#getAttributeCount()
	 */
	public int getAttributeCount() {
		return this.attributesCount;
	}

	/**
	 * @see IMethodInfo#getDescriptorIndex()
	 */
	public int getDescriptorIndex() {
		return this.descriptorIndex;
	}

	/**
	 * @see IMethodInfo#getNameIndex()
	 */
	public int getNameIndex() {
		return this.nameIndex;
	}

	int sizeInBytes() {
		return attributeBytes;
	}
	/**
	 * @see IMethodInfo#getAttributes()
	 */
	public IClassFileAttribute[] getAttributes() {
		return this.attributes;
	}

}