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


import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ConstructorDeclaration;

/**
 * AspectJ - hooks for subtypes
 */
public class MethodBinding extends Binding implements BaseTypes, TypeConstants {
	public int modifiers;
	public char[] selector;
	public TypeBinding returnType;
	public TypeBinding[] parameters;
	public ReferenceBinding[] thrownExceptions;
	public ReferenceBinding declaringClass;
	


	char[] signature;
protected MethodBinding() {
}
public MethodBinding(int modifiers, char[] selector, TypeBinding returnType, TypeBinding[] args, ReferenceBinding[] exceptions, ReferenceBinding declaringClass) {
	this.modifiers = modifiers;
	this.selector = selector;
	this.returnType = returnType;
	this.parameters = (args == null || args.length == 0) ? NoParameters : args;
	this.thrownExceptions = (exceptions == null || exceptions.length == 0) ? NoExceptions : exceptions;
	this.declaringClass = declaringClass;

	// propagate the strictfp & deprecated modifiers
	if (this.declaringClass != null) {
		if (this.declaringClass.isStrictfp())
			if (!(isNative() || isAbstract()))
				this.modifiers |= AccStrictfp;
		if (this.declaringClass.isViewedAsDeprecated() && !isDeprecated())
			this.modifiers |= AccDeprecatedImplicitly;
	}
}
public MethodBinding(int modifiers, TypeBinding[] args, ReferenceBinding[] exceptions, ReferenceBinding declaringClass) {
	this(modifiers, ConstructorDeclaration.ConstantPoolName, VoidBinding, args, exceptions, declaringClass);
}
// special API used to change method declaring class for runtime visibility check
public MethodBinding(MethodBinding initialMethodBinding, ReferenceBinding declaringClass) {
	this.modifiers = initialMethodBinding.modifiers;
	this.selector = initialMethodBinding.selector;
	this.returnType = initialMethodBinding.returnType;
	this.parameters = initialMethodBinding.parameters;
	this.thrownExceptions = initialMethodBinding.thrownExceptions;
	this.declaringClass = declaringClass;
}
/* Answer true if the argument types & the receiver's parameters are equal
*/

public final boolean areParametersEqual(MethodBinding method) {
	TypeBinding[] args = method.parameters;
	if (parameters == args)
		return true;

	int length = parameters.length;
	if (length != args.length)
		return false;
	
	for (int i = 0; i < length; i++)
		if (parameters[i] != args[i])
			return false;
	return true;
}
/* API
* Answer the receiver's binding type from Binding.BindingID.
*/

public final int bindingType() {
	return METHOD;
}
/* Answer true if the receiver is visible to the type provided by the scope.
* InvocationSite implements isSuperAccess() to provide additional information
* if the receiver is protected.
*
* NOTE: This method should ONLY be sent if the receiver is a constructor.
*
* NOTE: Cannot invoke this method with a compilation unit scope.
*/

public boolean canBeSeenBy(InvocationSite invocationSite, Scope scope) {
	if (isPublic()) return true;

	SourceTypeBinding invocationType = scope.invocationType();
	if (invocationType == declaringClass) return true;

	if (isProtected()) {
		// answer true if the receiver is in the same package as the invocationType
		if (invocationType.fPackage == declaringClass.fPackage) return true;
		return invocationSite.isSuperAccess();
	}

	if (isPrivate()) {
		// answer true if the invocationType and the declaringClass have a common enclosingType
		// already know they are not the identical type
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
		return outerInvocationType == outerDeclaringClass;
	}

	// isDefault()
	return invocationType.fPackage == declaringClass.fPackage;
}
/* Answer true if the receiver is visible to the type provided by the scope.
* InvocationSite implements isSuperAccess() to provide additional information
* if the receiver is protected.
*
* NOTE: Cannot invoke this method with a compilation unit scope.
*/
public boolean canBeSeenBy(TypeBinding receiverType, InvocationSite invocationSite, Scope scope) {
	if (isPublic()) return true;
    //XXX invocation vs. source
	SourceTypeBinding invocationType = scope.invocationType();
	if (invocationType == declaringClass && invocationType == receiverType) return true;

	if (isProtected()) {
		// answer true if the invocationType is the declaringClass or they are in the same package
		// OR the invocationType is a subclass of the declaringClass
		//    AND the receiverType is the invocationType or its subclass
		//    OR the method is a static method accessed directly through a type
		//    OR previous assertions are true for one of the enclosing type
		if (invocationType == declaringClass) return true;
		if (invocationType.fPackage == declaringClass.fPackage) return true;
		
		// for protected we need to check based on the type of this
		ReferenceBinding currentType = scope.enclosingSourceType();;
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
/* Answer the receiver's constant pool name.
*
* <init> for constructors
* <clinit> for clinit methods
* or the source name of the method
*/

public final char[] constantPoolName() {
	return selector;
}
public final int getAccessFlags() {
	return modifiers & AccJustFlag;
}
/* Answer true if the receiver is an abstract method
*/

public final boolean isAbstract() {
	return (modifiers & AccAbstract) != 0;
}
/* Answer true if the receiver is a constructor
*/

public final boolean isConstructor() {
	return selector == ConstructorDeclaration.ConstantPoolName;
}
protected boolean isConstructorRelated() {
	return isConstructor();
}
/* Answer true if the receiver has default visibility
*/

public final boolean isDefault() {
	return !isPublic() && !isProtected() && !isPrivate();
}
/* Answer true if the receiver is a system generated default abstract method
*/

public final boolean isDefaultAbstract() {
	return (modifiers & AccDefaultAbstract) != 0;
}
/* Answer true if the receiver is a deprecated method
*/

public final boolean isDeprecated() {
	return (modifiers & AccDeprecated) != 0;
}
/* Answer true if the receiver is final and cannot be overridden
*/

public final boolean isFinal() {
	return (modifiers & AccFinal) != 0;
}
/* Answer true if the receiver is a native method
*/

public final boolean isNative() {
	return (modifiers & AccNative) != 0;
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
/* Answer true if the receiver got requested to clear the private modifier
 * during private access emulation.
 */

public final boolean isRequiredToClearPrivateModifier() {
	return (modifiers & AccClearPrivateModifier) != 0;
}
/* Answer true if the receiver is a static method
*/

public final boolean isStatic() {
	return (modifiers & AccStatic) != 0;
}
/* Answer true if all float operations must adher to IEEE 754 float/double rules
*/

public final boolean isStrictfp() {
	return (modifiers & AccStrictfp) != 0;
}
/* Answer true if the receiver is a synchronized method
*/

public final boolean isSynchronized() {
	return (modifiers & AccSynchronized) != 0;
}
/* Answer true if the receiver has public visibility
*/

public final boolean isSynthetic() {
	return (modifiers & AccSynthetic) != 0;
}
/* Answer true if the receiver's declaring type is deprecated (or any of its enclosing types)
*/

public final boolean isViewedAsDeprecated() {
	return (modifiers & AccDeprecated) != 0 ||
		(modifiers & AccDeprecatedImplicitly) != 0;
}
public char[] readableName() /* foo(int, Thread) */ {
	StringBuffer buffer = new StringBuffer(parameters.length + 1 * 20);
	if (isConstructor())
		buffer.append(declaringClass.sourceName());
	else
		buffer.append(selector);
	buffer.append('(');
	if (parameters != NoParameters) {
		for (int i = 0, length = parameters.length; i < length; i++) {
			if (i > 0)
				buffer.append(", "); //$NON-NLS-1$
			buffer.append(parameters[i].sourceName());
		}
	}
	buffer.append(')');
	return buffer.toString().toCharArray();
}
protected final void selector(char[] selector) {
	this.selector = selector;
	this.signature = null;
}
/* Answer the receiver's signature.
*
* NOTE: This method should only be used during/after code gen.
* The signature is cached so if the signature of the return type or any parameter
* type changes, the cached state is invalid.
*/

public final char[] signature() /* (ILjava/lang/Thread;)Ljava/lang/Object; */ {
	if (signature != null)
		return signature;

	StringBuffer buffer = new StringBuffer(parameters.length + 1 * 20);
	buffer.append('(');
	if (isConstructorRelated() && declaringClass.isNestedType()) {
		// take into account the synthetic argument type signatures as well
		ReferenceBinding[] syntheticArgumentTypes = declaringClass.syntheticEnclosingInstanceTypes();
		int count = syntheticArgumentTypes == null ? 0 : syntheticArgumentTypes.length;
		for (int i = 0; i < count; i++)
			buffer.append(syntheticArgumentTypes[i].signature());
		SyntheticArgumentBinding[] syntheticArguments = declaringClass.syntheticOuterLocalVariables();
		count = syntheticArguments == null ? 0 : syntheticArguments.length;
		for (int i = 0; i < count; i++)
			buffer.append(syntheticArguments[i].type.signature());
	}
	if (parameters != NoParameters)
		for (int i = 0, length = parameters.length; i < length; i++)
			buffer.append(parameters[i].signature());
	buffer.append(')');
	buffer.append(returnType.signature());
	return signature = buffer.toString().toCharArray();
}
public final int sourceEnd() {
	AbstractMethodDeclaration method = sourceMethod();
	if (method == null)
		return 0;
	else
		return method.sourceEnd;
}
public AbstractMethodDeclaration sourceMethod() {
	SourceTypeBinding sourceType;
	if (declaringClass instanceof BinaryTypeBinding) return null;
	try {
		sourceType = (SourceTypeBinding) declaringClass;
	} catch (ClassCastException e) {
		return null;		
	}

	AbstractMethodDeclaration[] methods = sourceType.scope.referenceContext.methods;
	for (int i = methods.length; --i >= 0;)
		if (this == methods[i].binding)
			return methods[i];
	return null;		
}
public final int sourceStart() {
	AbstractMethodDeclaration method = sourceMethod();
	if (method == null)
		return 0;
	else
		return method.sourceStart;
}

	
	/**
	 * Subtypes can override this to return true if an access method should be
	 * used when referring to this method binding.  Currently used
	 * for AspectJ's inter-type method declarations.
	 */
	public boolean alwaysNeedsAccessMethod() { return false; }


	/**
	 * This will only be called if alwaysNeedsAccessMethod() returns true.
	 * In that case it should return the access method to be used.
	 */
	public MethodBinding getAccessMethod(boolean staticReference) {
		throw new RuntimeException("unimplemented");
	}

/* During private access emulation, the binding can be requested to loose its
 * private visibility when the class file is dumped.
 */

public final void tagForClearingPrivateModifier() {
	modifiers |= AccClearPrivateModifier;
}
public String toString() {
	String s = (returnType != null) ? returnType.debugName() : "NULL TYPE"; //$NON-NLS-1$
	s += " "; //$NON-NLS-1$
	s += (selector != null) ? new String(selector) : "UNNAMED METHOD"; //$NON-NLS-1$

	s += "("; //$NON-NLS-1$
	if (parameters != null) {
		if (parameters != NoParameters) {
			for (int i = 0, length = parameters.length; i < length; i++) {
				if (i  > 0)
					s += ", "; //$NON-NLS-1$
				s += (parameters[i] != null) ? parameters[i].debugName() : "NULL TYPE"; //$NON-NLS-1$
			}
		}
	} else {
		s += "NULL PARAMETERS"; //$NON-NLS-1$
	}
	s += ") "; //$NON-NLS-1$

	if (thrownExceptions != null) {
		if (thrownExceptions != NoExceptions) {
			s += "throws "; //$NON-NLS-1$
			for (int i = 0, length = thrownExceptions.length; i < length; i++) {
				if (i  > 0)
					s += ", "; //$NON-NLS-1$
				s += (thrownExceptions[i] != null) ? thrownExceptions[i].debugName() : "NULL TYPE"; //$NON-NLS-1$
			}
		}
	} else {
		s += "NULL THROWN EXCEPTIONS"; //$NON-NLS-1$
	}
	return s;
}
}
