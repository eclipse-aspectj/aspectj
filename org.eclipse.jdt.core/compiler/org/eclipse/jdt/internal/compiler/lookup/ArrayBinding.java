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

import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.util.CharOperation;

public final class ArrayBinding extends TypeBinding {
	// creation and initialization of the length field
	// the declaringClass of this field is intentionally set to null so it can be distinguished.
	public static final FieldBinding LengthField = new FieldBinding(LENGTH, IntBinding, AccPublic | AccFinal, null, Constant.NotAConstant);

	public TypeBinding leafComponentType;
	public int dimensions;

	char[] constantPoolName;
public ArrayBinding(TypeBinding type, int dimensions) {
	this.tagBits |= IsArrayType;
	this.leafComponentType = type;
	this.dimensions = dimensions;
}
/* Answer the receiver's constant pool name.
*
* NOTE: This method should only be used during/after code gen.
*/

public char[] constantPoolName() /*	[Ljava/lang/Object; */ {
	if (constantPoolName != null)
		return constantPoolName;

	char[] brackets = new char[dimensions];
	for (int i = dimensions - 1; i >= 0; i--)
		brackets[i] = '[';
	return constantPoolName = CharOperation.concat(brackets, leafComponentType.signature());
}
String debugName() {
	StringBuffer brackets = new StringBuffer(dimensions * 2);
	for (int i = dimensions; --i >= 0;)
		brackets.append("[]"); //$NON-NLS-1$
	return leafComponentType.debugName() + brackets.toString();
}
/* Answer an array whose dimension size is one less than the receiver.
*
* When the receiver's dimension size is one then answer the leaf component type.
*/

public TypeBinding elementsType(Scope scope) {
	if (dimensions == 1)
		return leafComponentType;
	else
		return scope.createArray(leafComponentType, dimensions - 1);
}
public PackageBinding getPackage() {
	return leafComponentType.getPackage();
}
/* Answer true if the receiver type can be assigned to the argument type (right)
*/

boolean isCompatibleWith(TypeBinding right) {
	if (this == right)
		return true;

	char[][] rightName;
	if (right.isArrayType()) {
		ArrayBinding rightArray = (ArrayBinding) right;
		if (rightArray.leafComponentType.isBaseType())
			return false; // relying on the fact that all equal arrays are identical
		if (dimensions == rightArray.dimensions)
			return leafComponentType.isCompatibleWith(rightArray.leafComponentType);
		if (dimensions < rightArray.dimensions)
			return false; // cannot assign 'String[]' into 'Object[][]' but can assign 'byte[][]' into 'Object[]'
		rightName = ((ReferenceBinding) rightArray.leafComponentType).compoundName;
	} else {
		if (right.isBaseType())
			return false;
		rightName = ((ReferenceBinding) right).compoundName;
	}
	//Check dimensions - Java does not support explicitly sized dimensions for types.
	//However, if it did, the type checking support would go here.

	if (CharOperation.equals(rightName, JAVA_LANG_OBJECT))
		return true;
	if (CharOperation.equals(rightName, JAVA_LANG_CLONEABLE))
		return true;
	if (CharOperation.equals(rightName, JAVA_IO_SERIALIZABLE))
		return true;
	return false;
}

public TypeBinding leafComponentType(){
	return leafComponentType;
}

/* API
* Answer the problem id associated with the receiver.
* NoError if the receiver is a valid binding.
*/

public int problemId() {
	return leafComponentType.problemId();
}
/**
* Answer the source name for the type.
* In the case of member types, as the qualified name from its top level type.
* For example, for a member type N defined inside M & A: "A.M.N".
*/

public char[] qualifiedSourceName() {
	char[] brackets = new char[dimensions * 2];
	for (int i = dimensions * 2 - 1; i >= 0; i -= 2) {
		brackets[i] = ']';
		brackets[i - 1] = '[';
	}
	return CharOperation.concat(leafComponentType.qualifiedSourceName(), brackets);
}
public char[] readableName() /* java.lang.Object[] */ {
	char[] brackets = new char[dimensions * 2];
	for (int i = dimensions * 2 - 1; i >= 0; i -= 2) {
		brackets[i] = ']';
		brackets[i - 1] = '[';
	}
	return CharOperation.concat(leafComponentType.readableName(), brackets);
}
public char[] sourceName() {
	char[] brackets = new char[dimensions * 2];
	for (int i = dimensions * 2 - 1; i >= 0; i -= 2) {
		brackets[i] = ']';
		brackets[i - 1] = '[';
	}
	return CharOperation.concat(leafComponentType.sourceName(), brackets);
}
public String toString() {
	return leafComponentType != null ? debugName() : "NULL TYPE ARRAY"; //$NON-NLS-1$
}
}
