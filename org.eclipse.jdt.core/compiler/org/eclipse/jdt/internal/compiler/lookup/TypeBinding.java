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

/*
 * Not all fields defined by this type (& its subclasses) are initialized when it is created.
 * Some are initialized only when needed.
 *
 * Accessors have been provided for some public fields so all TypeBindings have the same API...
 * but access public fields directly whenever possible.
 * Non-public fields have accessors which should be used everywhere you expect the field to be initialized.
 *
 * null is NOT a valid value for a non-public field... it just means the field is not initialized.
 */
abstract public class TypeBinding extends Binding implements BaseTypes, TagBits, TypeConstants, TypeIds {
	public int id = NoId;
	public int tagBits = 0; // See values in the interface TagBits below
/* API
 * Answer the receiver's binding type from Binding.BindingID.
 */

public final int bindingType() {
	return TYPE;
}
/* Answer true if the receiver can be instantiated
 */

public boolean canBeInstantiated() {
	return !isBaseType();
}
/* Answer the receiver's constant pool name.
 *
 * NOTE: This method should only be used during/after code gen.
 */

public abstract char[] constantPoolName(); /* java/lang/Object */
String debugName() {
	return new String(readableName());
}
public abstract PackageBinding getPackage();
/* Answer true if the receiver is an array
*/

public final boolean isArrayType() {
	return (tagBits & IsArrayType) != 0;
}
/* Answer true if the receiver is a base type
*/

public final boolean isBaseType() {
	return (tagBits & IsBaseType) != 0;
}
public boolean isClass() {
	return false;
}
/* Answer true if the receiver type can be assigned to the argument type (right)
*/
	
abstract boolean isCompatibleWith(TypeBinding right);
/* Answer true if the receiver's hierarchy has problems (always false for arrays & base types)
*/

public final boolean isHierarchyInconsistent() {
	return (tagBits & HierarchyHasProblems) != 0;
}
public boolean isInterface() {
	return false;
}
public final boolean isNumericType() {
	switch (id) {
		case T_int :
		case T_float :
		case T_double :
		case T_short :
		case T_byte :
		case T_long :
		case T_char :
			return true;
		default :
			return false;
	}
}

public TypeBinding leafComponentType(){
	return this;
}

/**
 * Answer the qualified name of the receiver's package separated by periods
 * or an empty string if its the default package.
 *
 * For example, {java.util.Hashtable}.
 */

public char[] qualifiedPackageName() {
	return getPackage() == null ? NoChar : getPackage().readableName();
}
/**
* Answer the source name for the type.
* In the case of member types, as the qualified name from its top level type.
* For example, for a member type N defined inside M & A: "A.M.N".
*/

public abstract char[] qualifiedSourceName();
/* Answer the receiver's signature.
*
* Arrays & base types do not distinguish between signature() & constantPoolName().
*
* NOTE: This method should only be used during/after code gen.
*/

public char[] signature() {
	return constantPoolName();
}
public abstract char[] sourceName();
}
