/*******************************************************************************
 * Copyright (c) 2000, 2001, 2002 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Palo Alto Research Center, Incorporated - AspectJ adaptation
 ******************************************************************************/
package org.eclipse.jdt.internal.compiler.lookup;

import org.eclipse.jdt.internal.compiler.env.IDependent;
import org.eclipse.jdt.internal.compiler.util.CharOperation;

/*
Not all fields defined by this type (& its subclasses) are initialized when it is created.
Some are initialized only when needed.

Accessors have been provided for some public fields so all TypeBindings have the same API...
but access public fields directly whenever possible.
Non-public fields have accessors which should be used everywhere you expect the field to be initialized.

null is NOT a valid value for a non-public field... it just means the field is not initialized.
*/


// AspectJ - added hooks for more sophisticated field lookup
abstract public class ReferenceBinding extends TypeBinding implements IDependent {
	public char[][] compoundName;
	public char[] sourceName;
	public int modifiers;
	public PackageBinding fPackage;

	char[] fileName;
	char[] constantPoolName;
	char[] signature;

public FieldBinding[] availableFields() {
	return fields();
}

public MethodBinding[] availableMethods() {
	return methods();
}	
/* Answer true if the receiver can be instantiated
*/

public boolean canBeInstantiated() {
	return !(isAbstract() || isInterface());
}
/* Answer true if the receiver is visible to the invocationPackage.
*/

public final boolean canBeSeenBy(PackageBinding invocationPackage) {
	if (isPublic()) return true;
	if (isPrivate()) return false;

	// isProtected() or isDefault()
	return invocationPackage == fPackage;
}
/* Answer true if the receiver is visible to the receiverType and the invocationType.
*/

public final boolean canBeSeenBy(ReferenceBinding receiverType, SourceTypeBinding invocationType) {
	boolean ret = innerCanBeSeenBy(receiverType, invocationType);
	if (ret) return true;

	//System.err.println("trying to see: " + new String(sourceName));

	if (Scope.findPrivilegedHandler(invocationType) != null) {
		Scope.findPrivilegedHandler(invocationType).notePrivilegedTypeAccess(this, null);
		return true;
	}
	return false;
}

private final boolean innerCanBeSeenBy(ReferenceBinding receiverType, SourceTypeBinding invocationType) {
	if (isPublic()) return true;

	if (invocationType == this && invocationType == receiverType) return true;

	if (isProtected()) {

		// answer true if the invocationType is the declaringClass or they are in the same package
		// OR the invocationType is a subclass of the declaringClass
		//    AND the invocationType is the invocationType or its subclass
		//    OR the type is a static method accessed directly through a type
		//    OR previous assertions are true for one of the enclosing type
		if (invocationType == this) return true;
		if (invocationType.fPackage == fPackage) return true;

		ReferenceBinding currentType = invocationType;
		ReferenceBinding declaringClass = enclosingType(); // protected types always have an enclosing one
		if (declaringClass == null) return false; // could be null if incorrect top-level protected type
		//int depth = 0;
		do {
			if (declaringClass == invocationType) return true;
			if (declaringClass.isSuperclassOf(currentType)) return true;
			//depth++;
			currentType = currentType.enclosingType();
		} while (currentType != null);
		return false;
	}

	if (isPrivate()) {
		// answer true if the receiverType is the receiver or its enclosingType
		// AND the invocationType and the receiver have a common enclosingType
		if (!(receiverType == this || receiverType == enclosingType())) return false;
		
		if (invocationType != this) {
			ReferenceBinding outerInvocationType = invocationType;
			ReferenceBinding temp = outerInvocationType.enclosingType();
			while (temp != null) {
				outerInvocationType = temp;
				temp = temp.enclosingType();
			}

			ReferenceBinding outerDeclaringClass = this;
			temp = outerDeclaringClass.enclosingType();
			while (temp != null) {
				outerDeclaringClass = temp;
				temp = temp.enclosingType();
			}
			if (outerInvocationType != outerDeclaringClass) return false;
		}
		return true;
	}

	// isDefault()
	if (invocationType.fPackage != fPackage) return false;

	ReferenceBinding type = receiverType;
	ReferenceBinding declaringClass = enclosingType() == null ? this : enclosingType();
	do {
		if (declaringClass == type) return true;
		if (fPackage != type.fPackage) return false;
	} while ((type = type.superclass()) != null);
	return false;
}
/* Answer true if the receiver is visible to the type provided by the scope.
*
* NOTE: Cannot invoke this method with a compilation unit scope.
*/

public final boolean canBeSeenBy(Scope scope) {
	boolean ret = innerCanBeSeenBy(scope);
	if (ret) return true;
	
	SourceTypeBinding invocationType = scope.invocationType();
//	System.err.println("trying to see (scope): " + new String(sourceName) + 
//			" from " + new String(invocationType.sourceName));


	if (Scope.findPrivilegedHandler(invocationType) != null) {
		//System.err.println("    is privileged!");
		Scope.findPrivilegedHandler(invocationType).notePrivilegedTypeAccess(this, null);
		return true;
	}
	return false;
}
		
private final boolean innerCanBeSeenBy(Scope scope) {
	if (isPublic()) return true;

	SourceTypeBinding invocationType = scope.enclosingSourceType();
	if (invocationType == this) return true;
	

	if (isProtected()) {
		// answer true if the invocationType is the declaringClass or they are in the same package
		// OR the invocationType is a subclass of the declaringClass
		//    AND the invocationType is the invocationType or its subclass
		//    OR the type is a static method accessed directly through a type
		//    OR previous assertions are true for one of the enclosing type
		if (invocationType.fPackage == fPackage) return true;

		ReferenceBinding currentType = invocationType;
		ReferenceBinding declaringClass = enclosingType(); // protected types always have an enclosing one
		if (declaringClass == null) return false; // could be null if incorrect top-level protected type
		// int depth = 0;
		do {
			if (declaringClass == invocationType) return true;
			if (declaringClass.isSuperclassOf(currentType)) return true;
			// depth++;
			currentType = currentType.enclosingType();
		} while (currentType != null);
		return false;
	}
	if (isPrivate()) {
		// answer true if the receiver and the invocationType have a common enclosingType
		// already know they are not the identical type
		ReferenceBinding outerInvocationType = invocationType;
		ReferenceBinding temp = outerInvocationType.enclosingType();
		while (temp != null) {
			outerInvocationType = temp;
			temp = temp.enclosingType();
		}

		ReferenceBinding outerDeclaringClass = this;
		temp = outerDeclaringClass.enclosingType();
		while (temp != null) {
			outerDeclaringClass = temp;
			temp = temp.enclosingType();
		}
		return outerInvocationType == outerDeclaringClass;
	}

	// isDefault()
	return invocationType.fPackage == fPackage;
}
public void computeId() {
	if (compoundName.length != 3) {
		if (compoundName.length == 4 && CharOperation.equals(JAVA_LANG_REFLECT_CONSTRUCTOR, compoundName)) {
			id = T_JavaLangReflectConstructor;
			return;
		}
		return;		// all other types are in java.*.*
	}

	if (!CharOperation.equals(JAVA, compoundName[0]))
		return;		// assumes we only look up types in java

	if (!CharOperation.equals(LANG, compoundName[1])) {
		if (CharOperation.equals(JAVA_IO_PRINTSTREAM, compoundName)) {
			id = T_JavaIoPrintStream;
			return;
		}
		return;		// all other types are in java.lang
	}

	if (CharOperation.equals(JAVA_LANG_OBJECT, compoundName)) {
		id = T_JavaLangObject;
		return;
	}
	if (CharOperation.equals(JAVA_LANG_STRING, compoundName)) {
		id = T_JavaLangString;
		return;
	}

	// well-known exception types
	if (CharOperation.equals(JAVA_LANG_THROWABLE, compoundName)) {
		id = T_JavaLangThrowable;
		return;
	}
	if (CharOperation.equals(JAVA_LANG_ERROR, compoundName)) {
		id = T_JavaLangError;
		return;
	}
	if (CharOperation.equals(JAVA_LANG_EXCEPTION, compoundName)) {
		id = T_JavaLangException;
		return;
	}
	if (CharOperation.equals(JAVA_LANG_CLASSNOTFOUNDEXCEPTION, compoundName)) {
		id = T_JavaLangClassNotFoundException;
		return;
	}
	if (CharOperation.equals(JAVA_LANG_NOCLASSDEFERROR, compoundName)) {
		id = T_JavaLangNoClassDefError;
		return;
	}

	// other well-known types
	if (CharOperation.equals(JAVA_LANG_CLASS, compoundName)) {
		id = T_JavaLangClass;
		return;
	}
	if (CharOperation.equals(JAVA_LANG_STRINGBUFFER, compoundName)) {
		id = T_JavaLangStringBuffer;
		return;
	}
	if (CharOperation.equals(JAVA_LANG_SYSTEM, compoundName)) {
		id = T_JavaLangSystem;
		return;
	}

	if (CharOperation.equals(JAVA_LANG_INTEGER, compoundName)) {
		id = T_JavaLangInteger;
		return;
	}

	if (CharOperation.equals(JAVA_LANG_BYTE, compoundName)) {
		id = T_JavaLangByte;
		return;
	}	

	if (CharOperation.equals(JAVA_LANG_CHARACTER, compoundName)) {
		id = T_JavaLangCharacter;
		return;
	}

	if (CharOperation.equals(JAVA_LANG_FLOAT, compoundName)) {
		id = T_JavaLangFloat;
		return;
	}

	if (CharOperation.equals(JAVA_LANG_DOUBLE, compoundName)) {
		id = T_JavaLangDouble;
		return;
	}

	if (CharOperation.equals(JAVA_LANG_BOOLEAN, compoundName)) {
		id = T_JavaLangBoolean;
		return;
	}

	if (CharOperation.equals(JAVA_LANG_SHORT, compoundName)) {
		id = T_JavaLangShort;
		return;
	}

	if (CharOperation.equals(JAVA_LANG_LONG, compoundName)) {
		id = T_JavaLangLong;
		return;
	}

	if (CharOperation.equals(JAVA_LANG_VOID, compoundName)) {
		id = T_JavaLangVoid;
		return;
	}
	
	if (CharOperation.equals(JAVA_LANG_ASSERTIONERROR, compoundName)) {
		id = T_JavaLangAssertionError;
		return;
	}
}
/* Answer the receiver's constant pool name.
*
* NOTE: This method should only be used during/after code gen.
*/

public char[] constantPoolName() /* java/lang/Object */ {
	if (constantPoolName != null) 	return constantPoolName;
	return constantPoolName = CharOperation.concatWith(compoundName, '/');
}
String debugName() {
	return (compoundName != null) ? new String(readableName()) : "UNNAMED TYPE"; //$NON-NLS-1$
}
public final int depth() {
	int depth = 0;
	ReferenceBinding current = this;
	while ((current = current.enclosingType()) != null)
		depth++;
	return depth;
}
/* Answer the receiver's enclosing type... null if the receiver is a top level type.
*/

public ReferenceBinding enclosingType() {
	return null;
}
public final ReferenceBinding enclosingTypeAt(int relativeDepth) {
	ReferenceBinding current = this;
	while (relativeDepth-- > 0 && current != null)
		current = current.enclosingType();
	return current;
}
public int fieldCount() {
	return fields().length;
}
public FieldBinding[] fields() {
	return NoFields;
}
public final int getAccessFlags() {
	return modifiers & AccJustFlag;
}
public MethodBinding getExactConstructor(TypeBinding[] argumentTypes) {
	return null;
}
public MethodBinding getExactMethod(char[] selector, TypeBinding[] argumentTypes) {
	return null;
}
public FieldBinding getField(char[] fieldName) {
	return null;
}


public FieldBinding getBestField(char[] fieldName, InvocationSite site, Scope scope) {
	//XXX this is mostly correct
	return getField(fieldName, site, scope);
}


/**
 * Where multiple fields with the same name are defined, this will
 * return the one most visible one...
 */
public FieldBinding getField(char[] fieldName, InvocationSite site, Scope scope) {
	return getField(fieldName);
}



/**
 * Answer the file name which defines the type.
 *
 * The path part (optional) must be separated from the actual
 * file proper name by a java.io.File.separator.
 *
 * The proper file name includes the suffix extension (e.g. ".java")
 *
 * e.g. "c:/com/ibm/compiler/java/api/Compiler.java" 
 */

public char[] getFileName() {
	return fileName;
}
public ReferenceBinding getMemberType(char[] typeName) {
	ReferenceBinding[] memberTypes = memberTypes();
	for (int i = memberTypes.length; --i >= 0;)
		if (CharOperation.equals(memberTypes[i].sourceName, typeName))
			return memberTypes[i];
	return null;
}
public MethodBinding[] getMethods(char[] selector) {
	return NoMethods;
}
public PackageBinding getPackage() {
	return fPackage;
}
/* Answer true if the receiver implements anInterface or is identical to anInterface.
* If searchHierarchy is true, then also search the receiver's superclasses.
*
* NOTE: Assume that anInterface is an interface.
*/

public boolean implementsInterface(ReferenceBinding anInterface, boolean searchHierarchy) {
	if (this == anInterface)
		return true;

	ReferenceBinding[][] interfacesToVisit = new ReferenceBinding[5][];
	int lastPosition = -1;
	ReferenceBinding currentType = this;
	do {
		ReferenceBinding[] itsInterfaces = currentType.superInterfaces();
		if (itsInterfaces != NoSuperInterfaces) {
			if (++lastPosition == interfacesToVisit.length)
				System.arraycopy(interfacesToVisit, 0, interfacesToVisit = new ReferenceBinding[lastPosition * 2][], 0, lastPosition);
			interfacesToVisit[lastPosition] = itsInterfaces;
		}
	} while (searchHierarchy && (currentType = currentType.superclass()) != null);
			
	for (int i = 0; i <= lastPosition; i++) {
		ReferenceBinding[] interfaces = interfacesToVisit[i];
		for (int j = 0, length = interfaces.length; j < length; j++) {
			if ((currentType = interfaces[j]) == anInterface)
				return true;

			ReferenceBinding[] itsInterfaces = currentType.superInterfaces();
			if (itsInterfaces != NoSuperInterfaces) {
				if (++lastPosition == interfacesToVisit.length)
					System.arraycopy(interfacesToVisit, 0, interfacesToVisit = new ReferenceBinding[lastPosition * 2][], 0, lastPosition);
				interfacesToVisit[lastPosition] = itsInterfaces;
			}
		}
	}
	return false;
}
// Internal method... assume its only sent to classes NOT interfaces

boolean implementsMethod(MethodBinding method) {
	ReferenceBinding type = this;
	while (type != null) {
		MethodBinding[] methods = type.getMethods(method.selector);
		for (int i = methods.length; --i >= 0;)
			if (methods[i].areParametersEqual(method))
				return true;
		type = type.superclass();
	}
	return false;
}
/* Answer true if the receiver is an abstract type
*/

public final boolean isAbstract() {
	return (modifiers & AccAbstract) != 0;
}
public final boolean isAnonymousType() {
	return (tagBits & IsAnonymousType) != 0;
}
public final boolean isBinaryBinding() {
	return (tagBits & IsBinaryBinding) != 0;
}
public final boolean isClass() {
	return (modifiers & AccInterface) == 0;
}
/* Answer true if the receiver type can be assigned to the argument type (right)
*/
	
boolean isCompatibleWith(TypeBinding right) {
	if (right == this)
		return true;
	if (right.id == T_Object)
		return true;
	if (!(right instanceof ReferenceBinding))
		return false;

	ReferenceBinding referenceBinding = (ReferenceBinding) right;
	if (referenceBinding.isInterface())
		return implementsInterface(referenceBinding, true);
	if (isInterface())  // Explicit conversion from an interface to a class is not allowed
		return false;
	return referenceBinding.isSuperclassOf(this);
}
/* Answer true if the receiver has default visibility
*/

public final boolean isDefault() {
	return (modifiers & (AccPublic | AccProtected | AccPrivate)) == 0;
}
/* Answer true if the receiver is a deprecated type
*/

public final boolean isDeprecated() {
	return (modifiers & AccDeprecated) != 0;
}
/* Answer true if the receiver is final and cannot be subclassed
*/

public final boolean isFinal() {
	return (modifiers & AccFinal) != 0;
}
public final boolean isInterface() {
	return (modifiers & AccInterface) != 0;
}
public final boolean isLocalType() {
	return (tagBits & IsLocalType) != 0;
}
public final boolean isMemberType() {
	return (tagBits & IsMemberType) != 0;
}
public final boolean isNestedType() {
	return (tagBits & IsNestedType) != 0;
}
/* Answer true if the receiver has private visibility
*/

public final boolean isPrivate() {
	return (modifiers & AccPrivate) != 0;
}
/* Answer true if the receiver has protected visibility
*/

public final boolean isProtected() {
	return (modifiers & AccProtected) != 0;
}
/* Answer true if the receiver has public visibility
*/

public final boolean isPublic() {
	return (modifiers & AccPublic) != 0;
}
/* Answer true if the receiver is a static member type (or toplevel)
 */

public final boolean isStatic() {
	return (modifiers & (AccStatic | AccInterface)) != 0 ||
		    (tagBits & IsNestedType) == 0;
}
/* Answer true if all float operations must adher to IEEE 754 float/double rules
*/

public final boolean isStrictfp() {
	return (modifiers & AccStrictfp) != 0;
}
/* Answer true if the receiver is in the superclass hierarchy of aType
*
* NOTE: Object.isSuperclassOf(Object) -> false
*/

public boolean isSuperclassOf(ReferenceBinding type) {
	do {
		if (this == (type = type.superclass())) return true;
	} while (type != null);

	return false;
}
/* Answer true if the receiver is deprecated (or any of its enclosing types)
*/

public final boolean isViewedAsDeprecated() {
	return (modifiers & AccDeprecated) != 0 ||
		(modifiers & AccDeprecatedImplicitly) != 0;
}
public ReferenceBinding[] memberTypes() {
	return NoMemberTypes;
}
public MethodBinding[] methods() {
	return NoMethods;
}
/**
* Answer the source name for the type.
* In the case of member types, as the qualified name from its top level type.
* For example, for a member type N defined inside M & A: "A.M.N".
*/

public char[] qualifiedSourceName() {
	if (isMemberType()) {
		return CharOperation.concat(enclosingType().qualifiedSourceName(), sourceName(), '.');
	} else {
		return sourceName();
	}
}
public char[] readableName() /*java.lang.Object*/ {
	if (isMemberType())
		return CharOperation.concat(enclosingType().readableName(), sourceName, '.');
	else
		return CharOperation.concatWith(compoundName, '.');
}
/* Answer the receiver's signature.
*
* NOTE: This method should only be used during/after code gen.
*/

public char[] signature() /* Ljava/lang/Object; */ {
	if (signature != null)
		return signature;

	return signature = CharOperation.concat('L', constantPoolName(), ';');
}
public char[] sourceName() {
	return sourceName;
}
public ReferenceBinding superclass() {
	return null;
}
public ReferenceBinding[] superInterfaces() {
	return NoSuperInterfaces;
}
public ReferenceBinding[] syntheticEnclosingInstanceTypes() {
	if (isStatic()) return null;

	ReferenceBinding enclosingType = enclosingType();
	if (enclosingType == null)
		return null;
	else
		return new ReferenceBinding[] {enclosingType};
}
public SyntheticArgumentBinding[] syntheticOuterLocalVariables() {
	return null;		// is null if no enclosing instances are required
}
}
