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

import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.impl.Constant;

/**
 * AspectJ added hooks for inter-type field bindings as well as
 * proto-hooks for allowing privileged access
 */
public class FieldBinding extends VariableBinding {
	public ReferenceBinding declaringClass;
protected FieldBinding() {
}
public FieldBinding(char[] name, TypeBinding type, int modifiers, ReferenceBinding declaringClass, Constant constant) {
	this.modifiers = modifiers;
	this.type = type;
	this.name = name;
	this.declaringClass = declaringClass;
	this.constant = constant;

	// propagate the deprecated modifier
	if (this.declaringClass != null)
		if (this.declaringClass.isViewedAsDeprecated() && !isDeprecated())
			this.modifiers |= AccDeprecatedImplicitly;
}
public FieldBinding(FieldDeclaration field, TypeBinding type, ReferenceBinding declaringClass) {
	this(field.name, type, field.modifiers, declaringClass, null);

	field.binding = this;
}
// special API used to change field declaring class for runtime visibility check
public FieldBinding(FieldBinding initialFieldBinding, ReferenceBinding declaringClass) {
	this.modifiers = initialFieldBinding.modifiers;
	this.type = initialFieldBinding.type;
	this.name = initialFieldBinding.name;
	this.declaringClass = declaringClass;
	this.constant = initialFieldBinding.constant;
	this.id = initialFieldBinding.id;
}
/* API
* Answer the receiver's binding type from Binding.BindingID.
*/

public final int bindingType() {
	return FIELD;
}
/* Answer true if the receiver is visible to the type provided by the scope.
* InvocationSite implements isSuperAccess() to provide additional information
* if the receiver is protected.
*
* NOTE: Cannot invoke this method with a compilation unit scope.
*/
// made non-final for AspectJ
public boolean canBeSeenBy(TypeBinding receiverType, InvocationSite invocationSite, Scope scope) {
	if (isPublic()) return true;

	SourceTypeBinding invocationType = scope.invocationType();
	if (invocationType == declaringClass && invocationType == receiverType) return true;


//	if (invocationType.isPrivileged) {
//		System.out.println("privileged access to: " + this);
//		return true;
//	}
	
	if (isProtected()) {
		// answer true if the invocationType is the declaringClass or they are in the same package
		// OR the invocationType is a subclass of the declaringClass
		//    AND the receiverType is the invocationType or its subclass
		//    OR the method is a static method accessed directly through a type
		//    OR previous assertions are true for one of the enclosing type
		if (invocationType == declaringClass) return true;
		if (invocationType.fPackage == declaringClass.fPackage) return true;
		
		ReferenceBinding currentType = invocationType;
		int depth = 0;
		do {
			if (declaringClass.isSuperclassOf(currentType)) {
				if (invocationSite.isSuperAccess()){
					return true;
				}
				// receiverType can be an array binding in one case... see if you can change it
				if (receiverType instanceof ArrayBinding){
					return false;
				}
				if (isStatic()){
					return true; // see 1FMEPDL - return invocationSite.isTypeAccess();
				}
				if (currentType == receiverType || currentType.isSuperclassOf((ReferenceBinding) receiverType)){
					if (depth > 0) invocationSite.setDepth(depth);
					return true;
				}
			}
			depth++;
			currentType = currentType.enclosingType();
		} while (currentType != null);
		return false;
	}

	if (isPrivate()) {
		// answer true if the receiverType is the declaringClass
		// AND the invocationType and the declaringClass have a common enclosingType
		if (receiverType != declaringClass) return false;

		if (invocationType != declaringClass) {
			ReferenceBinding outerInvocationType = invocationType;
			ReferenceBinding temp = outerInvocationType.enclosingType();
			while (temp != null) {
				outerInvocationType = temp;
				temp = temp.enclosingType();
			}

			ReferenceBinding outerDeclaringClass = declaringClass;
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
	if (invocationType.fPackage != declaringClass.fPackage) return false;

	// receiverType can be an array binding in one case... see if you can change it
	if (receiverType instanceof ArrayBinding)
		return false;
	ReferenceBinding type = (ReferenceBinding) receiverType;
	PackageBinding declaringPackage = declaringClass.fPackage;
	do {
		if (declaringClass == type) return true;
		if (declaringPackage != type.fPackage) return false;
	} while ((type = type.superclass()) != null);
	return false;
}
public final int getAccessFlags() {
	return modifiers & AccJustFlag;
}
/* Answer true if the receiver has default visibility
*/

public final boolean isDefault() {
	return !isPublic() && !isProtected() && !isPrivate();
}
/* Answer true if the receiver is a deprecated field
*/

public final boolean isDeprecated() {
	return (modifiers & AccDeprecated) != 0;
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
/* Answer true if the receiver is a static field
*/

public final boolean isStatic() {
	return (modifiers & AccStatic) != 0;
}
/* Answer true if the receiver is not defined in the source of the declaringClass
*/

public final boolean isSynthetic() {
	return (modifiers & AccSynthetic) != 0;
}
/* Answer true if the receiver is a transient field
*/

public final boolean isTransient() {
	return (modifiers & AccTransient) != 0;
}
/* Answer true if the receiver's declaring type is deprecated (or any of its enclosing types)
*/

public final boolean isViewedAsDeprecated() {
	return (modifiers & AccDeprecated) != 0 ||
		(modifiers & AccDeprecatedImplicitly) != 0;
}
/* Answer true if the receiver is a volatile field
*/

public final boolean isVolatile() {
	return (modifiers & AccVolatile) != 0;
}

public boolean alwaysNeedsAccessMethod(boolean isReadAccess) { return false; }
public SyntheticAccessMethodBinding getAccessMethod(boolean isReadAccess) {
	throw new RuntimeException("unimplemented");
}

public FieldBinding getFieldBindingForLookup() { return this; }
}
