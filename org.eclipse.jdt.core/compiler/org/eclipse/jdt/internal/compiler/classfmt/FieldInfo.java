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
import org.eclipse.jdt.internal.compiler.env.IBinaryField;
import org.eclipse.jdt.internal.compiler.impl.BooleanConstant;
import org.eclipse.jdt.internal.compiler.impl.ByteConstant;
import org.eclipse.jdt.internal.compiler.impl.CharConstant;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.impl.DoubleConstant;
import org.eclipse.jdt.internal.compiler.impl.FloatConstant;
import org.eclipse.jdt.internal.compiler.impl.IntConstant;
import org.eclipse.jdt.internal.compiler.impl.LongConstant;
import org.eclipse.jdt.internal.compiler.impl.ShortConstant;
import org.eclipse.jdt.internal.compiler.impl.StringConstant;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;
import org.eclipse.jdt.internal.compiler.util.CharOperation;

public class FieldInfo extends ClassFileStruct implements AttributeNamesConstants, IBinaryField, Comparable, TypeIds {
	private Constant constant;
	private boolean isDeprecated;
	private boolean isSynthetic;
	private int[] constantPoolOffsets;
	private int accessFlags;
	private char[] name;
	private char[] signature;
	private int attributesCount;
	private int attributeBytes;
	private Object wrappedConstantValue;
/**
 * @param classFileBytes byte[]
 * @param offsets int[]
 * @param offset int
 */
public FieldInfo (byte classFileBytes[], int offsets[], int offset) throws ClassFormatException {
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
 * Return the constant of the field.
 * Return org.eclipse.jdt.internal.compiler.impl.Constant.NotAConstant if there is none.
 * @return org.eclipse.jdt.internal.compiler.impl.Constant
 */
public Constant getConstant() {
	if (constant == null) {
		// read constant
		readConstantAttribute();
	}
	return constant;
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
 * Answer the name of the field.
 * @return char[]
 */
public char[] getName() {
	if (name == null) {
		// read the name
		int utf8Offset = constantPoolOffsets[u2At(2)] - structOffset;
		name = utf8At(utf8Offset + 3, u2At(utf8Offset + 1));
	}
	return name;
}
/**
 * Answer the resolved name of the receiver's type in the
 * class file format as specified in section 4.3.2 of the Java 2 VM spec.
 *
 * For example:
 *   - java.lang.String is Ljava/lang/String;
 *   - an int is I
 *   - a 2 dimensional array of strings is [[Ljava/lang/String;
 *   - an array of floats is [F
 * @return char[]
 */
public char[] getTypeName() {
	if (signature == null) {
		// read the signature
		int utf8Offset = constantPoolOffsets[u2At(4)] - structOffset;
		signature = utf8At(utf8Offset + 3, u2At(utf8Offset + 1));
	}
	return signature;
}
/**
 * Return a wrapper that contains the constant of the field.
 * Throws a java.ibm.compiler.java.classfmt.ClassFormatException in case the signature is 
 * incompatible with the constant tag.
 * 
 * @exception java.ibm.compiler.java.classfmt.ClassFormatException
 * @return java.lang.Object
 */
public Object getWrappedConstantValue() throws ClassFormatException {

	if (this.wrappedConstantValue == null) {
		if (hasConstant()) {
			Constant constant = getConstant();
			switch (constant.typeID()) {
				case T_int :
					this.wrappedConstantValue = new Integer(constant.intValue());
					break;
				case T_byte :
					this.wrappedConstantValue = new Byte(constant.byteValue());
					break;
				case T_short :
					this.wrappedConstantValue = new Short(constant.shortValue());
					break;
				case T_char :
					this.wrappedConstantValue = new Character(constant.charValue());
					break;
				case T_float :
					this.wrappedConstantValue = new Float(constant.floatValue());
					break;
				case T_double :
					this.wrappedConstantValue = new Double(constant.doubleValue());
					break;
				case T_boolean :
					this.wrappedConstantValue = new Boolean(constant.booleanValue());
					break;
				case T_long :
					this.wrappedConstantValue = new Long(constant.longValue());
					break;
				case T_String :
					this.wrappedConstantValue = constant.stringValue();
			}
		}
	}
	return this.wrappedConstantValue;
}
/**
 * Return true if the field has a constant value attribute, false otherwise.
 * @return boolean
 */
public boolean hasConstant() {
	return getConstant() != Constant.NotAConstant;
}
/**
 * Return true if the field is a synthetic field, false otherwise.
 * @return boolean
 */
public boolean isSynthetic() {
	return (getModifiers() & AccSynthetic) != 0;
}

private void readConstantAttribute() {
	int attributesCount = u2At(6);
	int readOffset = 8;
	boolean isConstant = false;
	for (int i = 0; i < attributesCount; i++) {
		int utf8Offset = constantPoolOffsets[u2At(readOffset)] - structOffset;
		char[] attributeName = utf8At(utf8Offset + 3, u2At(utf8Offset + 1));
		if (CharOperation
			.equals(attributeName, ConstantValueName)) {
			isConstant = true;
			// read the right constant
			int relativeOffset = constantPoolOffsets[u2At(readOffset + 6)] - structOffset;
			switch (u1At(relativeOffset)) {
				case IntegerTag :
					char[] sign = getTypeName();
					if (sign.length == 1) {
						switch (sign[0]) {
							case 'Z' : // boolean constant
								constant = new BooleanConstant(i4At(relativeOffset + 1) == 1);
								break;
							case 'I' : // integer constant
								constant = new IntConstant(i4At(relativeOffset + 1));
								break;
							case 'C' : // char constant
								constant = new CharConstant((char) i4At(relativeOffset + 1));
								break;
							case 'B' : // byte constant
								constant = new ByteConstant((byte) i4At(relativeOffset + 1));
								break;
							case 'S' : // short constant
								constant = new ShortConstant((short) i4At(relativeOffset + 1));
								break;
							default:
								constant = Constant.NotAConstant;                   
						}
					} else {
						constant = Constant.NotAConstant;
					}
					break;
				case FloatTag :
					constant = new FloatConstant(floatAt(relativeOffset + 1));
					break;
				case DoubleTag :
					constant = new DoubleConstant(doubleAt(relativeOffset + 1));
					break;
				case LongTag :
					constant = new LongConstant(i8At(relativeOffset + 1));
					break;
				case StringTag :
					utf8Offset = constantPoolOffsets[u2At(relativeOffset + 1)] - structOffset;
					constant = 
						new StringConstant(
							String.valueOf(utf8At(utf8Offset + 3, u2At(utf8Offset + 1)))); 
					break;
			}
		}
		readOffset += (6 + u4At(readOffset + 2));
	}
	if (!isConstant) {
		constant = Constant.NotAConstant;
	}
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
/**
 * Answer the size of the receiver in bytes.
 * 
 * @return int
 */
public int sizeInBytes() {
	return attributeBytes;
}
public void throwFormatException() throws ClassFormatException {
	throw new ClassFormatException(ClassFormatException.ErrBadFieldInfo);
}
public String toString() {
	StringBuffer buffer = new StringBuffer(this.getClass().getName());
	int modifiers = getModifiers();
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
		.append(getTypeName())
		.append(" ") //$NON-NLS-1$
		.append(getName())
		.append(" ") //$NON-NLS-1$
		.append(getConstant())
		.append("}") //$NON-NLS-1$
		.toString(); 
}

public int compareTo(Object o) {
	if (!(o instanceof FieldInfo)) {
		throw new ClassCastException();
	}
	return new String(this.getName()).compareTo(new String(((FieldInfo) o).getName()));
}
/**
 * This method is used to fully initialize the contents of the receiver. All methodinfos, fields infos
 * will be therefore fully initialized and we can get rid of the bytes.
 */
void initialize() {
	getModifiers();
	getName();
	getConstant();
	getTypeName();
	reset();
}
protected void reset() {
	this.constantPoolOffsets = null;
	super.reset();
}

}
