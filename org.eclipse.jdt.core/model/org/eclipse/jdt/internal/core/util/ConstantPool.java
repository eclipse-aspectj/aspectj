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

import org.eclipse.jdt.core.util.IConstantPool;
import org.eclipse.jdt.core.util.IConstantPoolConstant;
import org.eclipse.jdt.core.util.IConstantPoolEntry;

/**
 * Default implementation of IConstantPool.
 */
public class ConstantPool extends ClassFileStruct implements IConstantPool {
	
	private int constantPoolCount;
	private int[] constantPoolOffset;
	private byte[] classFileBytes;
	private ConstantPoolEntry constantPoolEntry;
	
	ConstantPool(byte[] reference, int[] constantPoolOffset) {
		this.constantPoolCount = constantPoolOffset.length;
		this.constantPoolOffset = constantPoolOffset;
		this.classFileBytes = reference;
		this.constantPoolEntry = new ConstantPoolEntry();
	}

	/**
	 * @see IConstantPool#decodeEntry(int)
	 */
	public IConstantPoolEntry decodeEntry(int index) {
		this.constantPoolEntry.reset();
		int kind = getEntryKind(index);
		this.constantPoolEntry.setKind(kind);
		switch(kind) {
			case IConstantPoolConstant.CONSTANT_Class :
				this.constantPoolEntry.setClassInfoNameIndex(u2At(this.classFileBytes,  1, this.constantPoolOffset[index]));
				this.constantPoolEntry.setClassInfoName(getUtf8ValueAt(this.constantPoolEntry.getClassInfoNameIndex()));
				break;
			case IConstantPoolConstant.CONSTANT_Double :
				this.constantPoolEntry.setDoubleValue(doubleAt(classFileBytes, 1, this.constantPoolOffset[index]));
				break;
			case IConstantPoolConstant.CONSTANT_Fieldref :
				this.constantPoolEntry.setClassIndex(u2At(this.classFileBytes,  1, this.constantPoolOffset[index]));
				int declaringClassIndex = u2At(this.classFileBytes,  1, this.constantPoolOffset[this.constantPoolEntry.getClassIndex()]);
				this.constantPoolEntry.setClassName(getUtf8ValueAt(declaringClassIndex));
				this.constantPoolEntry.setNameAndTypeIndex(u2At(this.classFileBytes,  3, this.constantPoolOffset[index]));
				int fieldNameIndex = u2At(this.classFileBytes,  1, this.constantPoolOffset[this.constantPoolEntry.getNameAndTypeIndex()]);
				int fieldDescriptorIndex = u2At(this.classFileBytes,  3, this.constantPoolOffset[this.constantPoolEntry.getNameAndTypeIndex()]);
				this.constantPoolEntry.setFieldName(getUtf8ValueAt(fieldNameIndex));
				this.constantPoolEntry.setFieldDescriptor(getUtf8ValueAt(fieldDescriptorIndex));
				break;
			case IConstantPoolConstant.CONSTANT_Methodref :
			case IConstantPoolConstant.CONSTANT_InterfaceMethodref :
				this.constantPoolEntry.setClassIndex(u2At(this.classFileBytes,  1, this.constantPoolOffset[index]));
				declaringClassIndex = u2At(this.classFileBytes,  1, this.constantPoolOffset[this.constantPoolEntry.getClassIndex()]);
				this.constantPoolEntry.setClassName(getUtf8ValueAt(declaringClassIndex));
				this.constantPoolEntry.setNameAndTypeIndex(u2At(this.classFileBytes,  3, this.constantPoolOffset[index]));
				int methodNameIndex = u2At(this.classFileBytes,  1, this.constantPoolOffset[this.constantPoolEntry.getNameAndTypeIndex()]);
				int methodDescriptorIndex = u2At(this.classFileBytes,  3, this.constantPoolOffset[this.constantPoolEntry.getNameAndTypeIndex()]);
				this.constantPoolEntry.setMethodName(getUtf8ValueAt(methodNameIndex));
				this.constantPoolEntry.setMethodDescriptor(getUtf8ValueAt(methodDescriptorIndex));
				break;
			case IConstantPoolConstant.CONSTANT_Float :
				this.constantPoolEntry.setFloatValue(floatAt(classFileBytes, 1, this.constantPoolOffset[index]));
				break;
			case IConstantPoolConstant.CONSTANT_Integer :
				this.constantPoolEntry.setIntegerValue(i4At(classFileBytes, 1, this.constantPoolOffset[index]));
				break;
			case IConstantPoolConstant.CONSTANT_Long :
				this.constantPoolEntry.setLongValue(i8At(classFileBytes, 1, this.constantPoolOffset[index]));
				break;
			case IConstantPoolConstant.CONSTANT_NameAndType :
				this.constantPoolEntry.setNameAndTypeNameIndex(u2At(this.classFileBytes,  1, this.constantPoolOffset[index]));
				this.constantPoolEntry.setNameAndTypeDescriptorIndex(u2At(this.classFileBytes,  3, this.constantPoolOffset[index]));
				break;
			case IConstantPoolConstant.CONSTANT_String :
				this.constantPoolEntry.setStringIndex(u2At(this.classFileBytes,  1, this.constantPoolOffset[index]));
				this.constantPoolEntry.setStringValue(getUtf8ValueAt(this.constantPoolEntry.getStringIndex()));
				break;
			case IConstantPoolConstant.CONSTANT_Utf8 :
				this.constantPoolEntry.setUtf8Length(u2At(this.classFileBytes,  1, this.constantPoolOffset[index]));
				this.constantPoolEntry.setUtf8Value(getUtf8ValueAt(index));
		}
		return this.constantPoolEntry;
	}

	/**
	 * @see IConstantPool#getConstantPoolCount()
	 */
	public int getConstantPoolCount() {
		return this.constantPoolCount;
	}

	/**
	 * @see IConstantPool#getEntryKind(int)
	 */
	public int getEntryKind(int index) {
		return this.u1At(this.classFileBytes, 0, this.constantPoolOffset[index]);
	}

	private char[] getUtf8ValueAt(int utf8Index) {
		int utf8Offset = this.constantPoolOffset[utf8Index];
		return utf8At(classFileBytes, 0, utf8Offset + 3, u2At(classFileBytes, 0, utf8Offset + 1));
	}
}
