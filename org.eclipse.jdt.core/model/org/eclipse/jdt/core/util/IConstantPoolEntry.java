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
package org.eclipse.jdt.core.util;

/**
 * Description of a constant pool entry as described in the JVM specifications.
 * Its contents is initialized according to its kind.
 * 
 * This interface may be implemented by clients.
 *  
 * @since 2.0
 */
public interface IConstantPoolEntry {

	/**
	 * Answer back the type of this entry.
	 * 
	 * @return the type of this entry
	 */
	int getKind();

	/**
	 * Answer back the name index for a CONSTANT_Class type entry.
	 * 
	 * @return the name index for a CONSTANT_Class type entry
	 */
	int getClassInfoNameIndex();

	/**
	 * Answer back the class index for a CONSTANT_Fieldref,
	 * CONSTANT_Methodref, CONSTANT_InterfaceMethodref type entry.
	 * 
	 * @return the class index for a CONSTANT_Fieldref,
	 * CONSTANT_Methodref, CONSTANT_InterfaceMethodref type entry
	 */
	int getClassIndex();

	/**
	 * Answer back the nameAndType index for a CONSTANT_Fieldref,
	 * CONSTANT_Methodref, CONSTANT_InterfaceMethodref type entry.
	 * 
	 * @return the nameAndType index for a CONSTANT_Fieldref,
	 * CONSTANT_Methodref, CONSTANT_InterfaceMethodref type entry
	 */
	int getNameAndTypeIndex();
	
	/**
	 * Answer back the string index for a CONSTANT_String type entry.
	 * 
	 * @return the string index for a CONSTANT_String type entry
	 */
	int getStringIndex();

	/**
	 * Answer back the string value for a CONSTANT_String type entry.
	 * 
	 * @return the string value for a CONSTANT_String type entry
	 */
	String getStringValue();
	
	/**
	 * Answer back the integer value for a CONSTANT_Integer type entry.
	 * 
	 * @return the integer value for a CONSTANT_Integer type entry
	 */
	int getIntegerValue();

	/**
	 * Answer back the float value for a CONSTANT_Float type entry.
	 * 
	 * @return the float value for a CONSTANT_Float type entry
	 */
	float getFloatValue();

	/**
	 * Answer back the double value for a CONSTANT_Double type entry.
	 * 
	 * @return the double value for a CONSTANT_Double type entry
	 */
	double getDoubleValue();

	/**
	 * Answer back the long value for a CONSTANT_Long type entry.
	 * 
	 * @return the long value for a CONSTANT_Long type entry
	 */
	long getLongValue();
	
	/**
	 * Answer back the descriptor index for a CONSTANT_NameAndType type entry.
	 * 
	 * @return the descriptor index for a CONSTANT_NameAndType type entry
	 */
	int getNameAndTypeInfoDescriptorIndex();

	/**
	 * Answer back the name index for a CONSTANT_NameAndType type entry.
	 * 
	 * @return the name index for a CONSTANT_NameAndType type entry
	 */
	int getNameAndTypeInfoNameIndex();

	/**
	 * Answer back the class name for a CONSTANT_Class type entry.
	 * 
	 * @return the class name for a CONSTANT_Class type entry
	 */
	char[] getClassInfoName();

	/**
	 * Answer back the class name for a CONSTANT_Fieldref,
	 * CONSTANT_Methodref, CONSTANT_InterfaceMethodref type entry.
	 * 
	 * @return the class name for a CONSTANT_Fieldref,
	 * CONSTANT_Methodref, CONSTANT_InterfaceMethodref type entry
	 */
	char[] getClassName();

	/**
	 * Answer back the field name for a CONSTANT_Fieldref type entry.
	 * 
	 * @return the field name for a CONSTANT_Fieldref type entry
	 */
	char[] getFieldName();
	
	/**
	 * Answer back the field name for a CONSTANT_Methodref or CONSTANT_InterfaceMethodred
	 * type entry.
	 * 
	 * @return the field name for a CONSTANT_Methodref or CONSTANT_InterfaceMethodred
	 * type entry
	 */
	char[] getMethodName();

	/**
	 * Answer back the field descriptor value for a CONSTANT_Fieldref type entry. This value
	 * is set only when decoding the CONSTANT_Fieldref entry. 
	 * 
	 * @return the field descriptor value for a CONSTANT_Fieldref type entry. This value
	 * is set only when decoding the CONSTANT_Fieldref entry
	 */
	char[] getFieldDescriptor();

	/**
	 * Answer back the method descriptor value for a CONSTANT_Methodref or
	 * CONSTANT_InterfaceMethodref type entry. This value is set only when decoding the 
	 * CONSTANT_Methodref or CONSTANT_InterfaceMethodref entry. 
	 * 
	 * @return the method descriptor value for a CONSTANT_Methodref or
	 * CONSTANT_InterfaceMethodref type entry. This value is set only when decoding the 
	 * CONSTANT_Methodref or CONSTANT_InterfaceMethodref entry
	 */
	char[] getMethodDescriptor();
	
	/**
	 * Answer back the utf8 value for a CONSTANT_Utf8 type entry. This value is set only when
	 * decoding a UTF8 entry.
	 * 
	 * @return the utf8 value for a CONSTANT_Utf8 type entry. This value is set only when
	 * decoding a UTF8 entry
	 */
	char[] getUtf8Value();
	
	/**
	 * Answer back the utf8 length for a CONSTANT_Utf8 type entry. This value is set only when
	 * decoding a UTF8 entry.
	 * 
	 * @return the utf8 length for a CONSTANT_Utf8 type entry. This value is set only when
	 * decoding a UTF8 entry
	 */
	int getUtf8Length();
}
