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
package org.eclipse.jdt.internal.compiler.lookup;

public final class BaseTypeBinding extends TypeBinding {
	public char[] simpleName;
	private char [] constantPoolName;
BaseTypeBinding(int id, char[] name, char[] constantPoolName) {
	this.tagBits |= IsBaseType;
	this.id = id;
	this.simpleName = name;
	this.constantPoolName = constantPoolName;
}
/* Answer the receiver's constant pool name.
*/

public char[] constantPoolName() {
	return constantPoolName;
}
public PackageBinding getPackage() {
	return null;
}
/* Answer true if the receiver type can be assigned to the argument type (right)
*/

final boolean isCompatibleWith(TypeBinding right) {
	if (this == right)
		return true;
	if (!right.isBaseType())
		return this == NullBinding;

	switch (right.id) {
		case T_boolean :
		case T_byte :
		case T_char :
			return false;
		case T_double :
			switch (id) {
				case T_byte :
				case T_char :
				case T_short :
				case T_int :
				case T_long :
				case T_float :
					return true;
				default :
					return false;
			}
		case T_float :
			switch (id) {
				case T_byte :
				case T_char :
				case T_short :
				case T_int :
				case T_long :
					return true;
				default :
					return false;
			}
		case T_long :
			switch (id) {
				case T_byte :
				case T_char :
				case T_short :
				case T_int :
					return true;
				default :
					return false;
			}
		case T_int :
			switch (id) {
				case T_byte :
				case T_char :
				case T_short :
					return true;
				default :
					return false;
			}
		case T_short :
			return (id == T_byte);
	}
	return false;
}
public static final boolean isNarrowing(int left, int right) {
	//can "left" store a "right" using some narrowing conversion
	//(is left smaller than right)

	switch (left) {
		case T_boolean :
			return right == T_boolean;
		case T_char :
		case T_byte :
			if (right == T_byte) return true;
		case T_short :
			if (right == T_short) return true;
			if (right == T_char) return true;
		case T_int :
			if (right == T_int) return true;
		case T_long :
			if (right == T_long) return true;
		case T_float :
			if (right == T_float) return true;
		case T_double :
			if (right == T_double) return true;
		default :
			return false;
	}
}
public static final boolean isWidening(int left, int right) {
	//can "left" store a "right" using some widening conversion
	//(is left "bigger" than right)

	switch (left) {
		case T_boolean :
			return right == T_boolean;
		case T_char :
			return right == T_char;
		case T_double :
			if (right == T_double) return true;
		case T_float :
			if (right == T_float) return true;
		case T_long :
			if (right == T_long) return true;
		case T_int :
			if (right == T_int) return true;
			if (right == T_char) return true;
		case T_short :
			if (right == T_short) return true;
		case T_byte :
			if (right == T_byte) return true;
		default :
			return false;
	}
}
public char[] qualifiedSourceName() {
	return simpleName;
}
public char[] readableName() {
	return simpleName;
}
public char[] sourceName() {
	return simpleName;
}
public String toString() {
	return new String(constantPoolName) + " (id=" + id + ")"; //$NON-NLS-1$ //$NON-NLS-2$
}
}
