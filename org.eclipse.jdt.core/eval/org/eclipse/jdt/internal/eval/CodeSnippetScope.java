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
package org.eclipse.jdt.internal.eval;

import org.eclipse.jdt.internal.compiler.ast.ConstructorDeclaration;
import org.eclipse.jdt.internal.compiler.lookup.ArrayBinding;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.InvocationSite;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodScope;
import org.eclipse.jdt.internal.compiler.lookup.PackageBinding;
import org.eclipse.jdt.internal.compiler.lookup.ProblemBinding;
import org.eclipse.jdt.internal.compiler.lookup.ProblemFieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.ProblemMethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ProblemReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.VariableBinding;
import org.eclipse.jdt.internal.compiler.util.CharOperation;
import org.eclipse.jdt.internal.compiler.util.ObjectVector;

/**
 * This scope is used for code snippet lookup to emulate private, protected and default access.
 * These accesses inside inner classes are not managed yet.
 */
public class CodeSnippetScope extends BlockScope {
/**
 * CodeSnippetScope constructor comment.
 * @param kind int
 * @param parent org.eclipse.jdt.internal.compiler.lookup.Scope
 */
protected CodeSnippetScope(int kind, Scope parent) {
	super(kind, parent);
}
/**
 * CodeSnippetScope constructor comment.
 * @param parent org.eclipse.jdt.internal.compiler.lookup.BlockScope
 */
public CodeSnippetScope(BlockScope parent) {
	super(parent);
}
/**
 * CodeSnippetScope constructor comment.
 * @param parent org.eclipse.jdt.internal.compiler.lookup.BlockScope
 * @param variableCount int
 */
public CodeSnippetScope(BlockScope parent, int variableCount) {
	super(parent, variableCount);
}
/* Answer true if the receiver is visible to the type provided by the scope.
* InvocationSite implements isSuperAccess() to provide additional information
* if the receiver is protected.
*
* NOTE: Cannot invoke this method with a compilation unit scope.
*/

public final boolean canBeSeenByForCodeSnippet(FieldBinding fieldBinding, TypeBinding receiverType, InvocationSite invocationSite, Scope scope) {
	if (fieldBinding.isPublic()) return true;

	ReferenceBinding invocationType = (ReferenceBinding) receiverType;
	if (invocationType == fieldBinding.declaringClass) return true;

	if (fieldBinding.isProtected()) {
		// answer true if the invocationType is the declaringClass or they are in the same package
		// OR the invocationType is a subclass of the declaringClass
		//    AND the receiverType is the invocationType or its subclass
		//    OR the field is a static field accessed directly through a type
		if (invocationType == fieldBinding.declaringClass) return true;
		if (invocationType.fPackage == fieldBinding.declaringClass.fPackage) return true;
		if (fieldBinding.declaringClass.isSuperclassOf(invocationType)) {
			if (invocationSite.isSuperAccess()) return true;
			// receiverType can be an array binding in one case... see if you can change it
			if (receiverType instanceof ArrayBinding)
				return false;
			if (invocationType == receiverType || invocationType.isSuperclassOf((ReferenceBinding) receiverType))
				return true;
			if (fieldBinding.isStatic())
				return true; // see 1FMEPDL - return invocationSite.isTypeAccess();
		}
		return false;
	}

	if (fieldBinding.isPrivate()) {
		// answer true if the receiverType is the declaringClass
		// AND the invocationType and the declaringClass have a common enclosingType
		if (receiverType != fieldBinding.declaringClass) return false;

		if (invocationType != fieldBinding.declaringClass) {
			ReferenceBinding outerInvocationType = invocationType;
			ReferenceBinding temp = outerInvocationType.enclosingType();
			while (temp != null) {
				outerInvocationType = temp;
				temp = temp.enclosingType();
			}

			ReferenceBinding outerDeclaringClass = fieldBinding.declaringClass;
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
	if (invocationType.fPackage != fieldBinding.declaringClass.fPackage) return false;

	// receiverType can be an array binding in one case... see if you can change it
	if (receiverType instanceof ArrayBinding)
		return false;
	ReferenceBinding type = (ReferenceBinding) receiverType;
	PackageBinding declaringPackage = fieldBinding.declaringClass.fPackage;
	do {
		if (fieldBinding.declaringClass == type) return true;
		if (declaringPackage != type.fPackage) return false;
	} while ((type = type.superclass()) != null);
	return false;
}
/* Answer true if the receiver is visible to the type provided by the scope.
* InvocationSite implements isSuperAccess() to provide additional information
* if the receiver is protected.
*
* NOTE: Cannot invoke this method with a compilation unit scope.
*/
public final boolean canBeSeenByForCodeSnippet(MethodBinding methodBinding, TypeBinding receiverType, InvocationSite invocationSite, Scope scope) {
	if (methodBinding.isPublic()) return true;

	ReferenceBinding invocationType = (ReferenceBinding) receiverType;
	if (invocationType == methodBinding.declaringClass && invocationType == receiverType) return true;

	if (methodBinding.isProtected()) {
		// answer true if the invocationType is the declaringClass or they are in the same package
		// OR the invocationType is a subclass of the declaringClass
		//    AND the receiverType is the invocationType or its subclass
		//    OR the method is a static method accessed directly through a type
		if (invocationType == methodBinding.declaringClass) return true;
		if (invocationType.fPackage == methodBinding.declaringClass.fPackage) return true;
		if (methodBinding.declaringClass.isSuperclassOf(invocationType)) {
			if (invocationSite.isSuperAccess()) return true;
			// receiverType can be an array binding in one case... see if you can change it
			if (receiverType instanceof ArrayBinding)
				return false;
			if (invocationType == receiverType || invocationType.isSuperclassOf((ReferenceBinding) receiverType))
				return true;
			if (methodBinding.isStatic())
				return true; // see 1FMEPDL - return invocationSite.isTypeAccess();
		}
		return false;
	}

	if (methodBinding.isPrivate()) {
		// answer true if the receiverType is the declaringClass
		// AND the invocationType and the declaringClass have a common enclosingType
		if (receiverType != methodBinding.declaringClass) return false;

		if (invocationType != methodBinding.declaringClass) {
			ReferenceBinding outerInvocationType = invocationType;
			ReferenceBinding temp = outerInvocationType.enclosingType();
			while (temp != null) {
				outerInvocationType = temp;
				temp = temp.enclosingType();
			}

			ReferenceBinding outerDeclaringClass = methodBinding.declaringClass;
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
	if (invocationType.fPackage != methodBinding.declaringClass.fPackage) return false;

	// receiverType can be an array binding in one case... see if you can change it
	if (receiverType instanceof ArrayBinding)
		return false;
	ReferenceBinding type = (ReferenceBinding) receiverType;
	PackageBinding declaringPackage = methodBinding.declaringClass.fPackage;
	do {
		if (methodBinding.declaringClass == type) return true;
		if (declaringPackage != type.fPackage) return false;
	} while ((type = type.superclass()) != null);
	return false;
}
/* Answer true if the receiver is visible to the type provided by the scope.
* InvocationSite implements isSuperAccess() to provide additional information
* if the receiver is protected.
*
* NOTE: Cannot invoke this method with a compilation unit scope.
*/

public final boolean canBeSeenByForCodeSnippet(ReferenceBinding referenceBinding, ReferenceBinding receiverType) {
	if (referenceBinding.isPublic()) return true;

	if (receiverType == referenceBinding) return true;

	if (referenceBinding.isProtected()) {
		// answer true if the receiver (or its enclosing type) is the superclass 
		//	of the receiverType or in the same package
		return receiverType.fPackage == referenceBinding.fPackage 
				|| referenceBinding.isSuperclassOf(receiverType)
				|| referenceBinding.enclosingType().isSuperclassOf(receiverType); // protected types always have an enclosing one
	}

	if (referenceBinding.isPrivate()) {
		// answer true if the receiver and the receiverType have a common enclosingType
		// already know they are not the identical type
		ReferenceBinding outerInvocationType = receiverType;
		ReferenceBinding temp = outerInvocationType.enclosingType();
		while (temp != null) {
			outerInvocationType = temp;
			temp = temp.enclosingType();
		}

		ReferenceBinding outerDeclaringClass = referenceBinding;
		temp = outerDeclaringClass.enclosingType();
		while (temp != null) {
			outerDeclaringClass = temp;
			temp = temp.enclosingType();
		}
		return outerInvocationType == outerDeclaringClass;
	}

	// isDefault()
	return receiverType.fPackage == referenceBinding.fPackage;
}
// Internal use only
public MethodBinding findExactMethod(ReferenceBinding receiverType, char[] selector, TypeBinding[] argumentTypes, InvocationSite invocationSite) {
	MethodBinding exactMethod = receiverType.getExactMethod(selector, argumentTypes);
	if (exactMethod != null){
		if (receiverType.isInterface() || canBeSeenByForCodeSnippet(exactMethod, receiverType, invocationSite, this))
			return exactMethod;
	}
	return null;
}
// Internal use only

/*	Answer the field binding that corresponds to fieldName.
	Start the lookup at the receiverType.
	InvocationSite implements
		isSuperAccess(); this is used to determine if the discovered field is visible.
	Only fields defined by the receiverType or its supertypes are answered;
	a field of an enclosing type will not be found using this API.

	If no visible field is discovered, null is answered.
*/

public FieldBinding findFieldForCodeSnippet(TypeBinding receiverType, char[] fieldName, InvocationSite invocationSite) {
	if (receiverType.isBaseType())
		return null;
	if (receiverType.isArrayType()) {
		if (CharOperation.equals(fieldName, LENGTH))
			return ArrayBinding.LengthField;
		return null;
	}

	ReferenceBinding currentType = (ReferenceBinding) receiverType;
	if (!currentType.canBeSeenBy(this))
		return new ProblemFieldBinding(currentType, fieldName, NotVisible); // *** Need a new problem id - TypeNotVisible?

	FieldBinding field = currentType.getField(fieldName);
	if (field != null) {
		if (canBeSeenByForCodeSnippet(field, currentType, invocationSite, this))
			return field;
		else
			return new ProblemFieldBinding(field.declaringClass, fieldName, NotVisible);
	}

	// collect all superinterfaces of receiverType until the field is found in a supertype
	ReferenceBinding[][] interfacesToVisit = null;
	int lastPosition = -1;
	FieldBinding visibleField = null;
	boolean keepLooking = true;
	boolean notVisible = false; // we could hold onto the not visible field for extra error reporting
	while (keepLooking) {
		ReferenceBinding[] itsInterfaces = currentType.superInterfaces();
		if (itsInterfaces != NoSuperInterfaces) {
			if (interfacesToVisit == null)
				interfacesToVisit = new ReferenceBinding[5][];
			if (++lastPosition == interfacesToVisit.length)
				System.arraycopy(interfacesToVisit, 0, interfacesToVisit = new ReferenceBinding[lastPosition * 2][], 0, lastPosition);
			interfacesToVisit[lastPosition] = itsInterfaces;
		}
		if ((currentType = currentType.superclass()) == null)
			break;

		if ((field = currentType.getField(fieldName)) != null) {
			keepLooking = false;
			if (canBeSeenByForCodeSnippet(field, receiverType, invocationSite, this)) {
				if (visibleField == null)
					visibleField = field;
				else
					return new ProblemFieldBinding(visibleField.declaringClass, fieldName, Ambiguous);
			} else {
				notVisible = true;
			}
		}
	}

	// walk all visible interfaces to find ambiguous references
	if (interfacesToVisit != null) {
		ProblemFieldBinding ambiguous = null;
		done : for (int i = 0; i <= lastPosition; i++) {
			ReferenceBinding[] interfaces = interfacesToVisit[i];
			for (int j = 0, length = interfaces.length; j < length; j++) {
				ReferenceBinding anInterface = interfaces[j];
				if ((anInterface.tagBits & InterfaceVisited) == 0) { // if interface as not already been visited
					anInterface.tagBits |= InterfaceVisited;
					if ((field = anInterface.getField(fieldName)) != null) {
						if (visibleField == null) {
							visibleField = field;
						} else {
							ambiguous = new ProblemFieldBinding(visibleField.declaringClass, fieldName, Ambiguous);
							break done;
						}
					} else {
						ReferenceBinding[] itsInterfaces = anInterface.superInterfaces();
						if (itsInterfaces != NoSuperInterfaces) {
							if (++lastPosition == interfacesToVisit.length)
								System.arraycopy(interfacesToVisit, 0, interfacesToVisit = new ReferenceBinding[lastPosition * 2][], 0, lastPosition);
							interfacesToVisit[lastPosition] = itsInterfaces;
						}
					}
				}
			}
		}

		// bit reinitialization
		for (int i = 0; i <= lastPosition; i++) {
			ReferenceBinding[] interfaces = interfacesToVisit[i];
			for (int j = 0, length = interfaces.length; j < length; j++)
				interfaces[j].tagBits &= ~InterfaceVisited;
		}
		if (ambiguous != null) return ambiguous;
	}

	if (visibleField != null)
		return visibleField;
	if (notVisible)
		return new ProblemFieldBinding(currentType, fieldName, NotVisible);
	return null;
}
// Internal use only
public MethodBinding findMethod(
	ReferenceBinding receiverType,
	char[] selector,
	TypeBinding[] argumentTypes,
	InvocationSite invocationSite) {

		ReferenceBinding currentType = receiverType;
		MethodBinding matchingMethod = null;
		ObjectVector found = new ObjectVector();

		//compilationUnitScope().recordTypeReference(receiverType);
		//compilationUnitScope().recordTypeReferences(argumentTypes);

		if (currentType.isInterface()) {
			MethodBinding[] currentMethods = currentType.getMethods(selector);
			int currentLength = currentMethods.length;
			if (currentLength == 1) {
				matchingMethod = currentMethods[0];
			} else if (currentLength > 1) {
				for (int f = 0; f < currentLength; f++)
					found.add(currentMethods[f]);
			}
			matchingMethod = findMethodInSuperInterfaces(currentType, selector, found, matchingMethod);
			currentType = getJavaLangObject();
		}

		// superclass lookup
		ReferenceBinding classHierarchyStart = currentType;
		
		while (currentType != null) {
			MethodBinding[] currentMethods = currentType.getMethods(selector);
			int currentLength = currentMethods.length;
			if (currentLength == 1 && matchingMethod == null && found.size == 0) {
				matchingMethod = currentMethods[0];
			} else if (currentLength > 0) {
				if (found.size == 0 && matchingMethod != null)
					found.add(matchingMethod);
				for (int f = 0; f < currentLength; f++)
					found.add(currentMethods[f]);
			}
			currentType = currentType.superclass();
		}

		int foundSize = found.size;
		if (foundSize == 0) {
			if (matchingMethod == null){
				MethodBinding interfaceMethod = findDefaultAbstractMethod(receiverType, selector, argumentTypes, invocationSite, classHierarchyStart, matchingMethod, found);
				if (interfaceMethod != null) return interfaceMethod;
			}
			return matchingMethod; // may be null - have not checked arg types or visibility
		}
		MethodBinding[] candidates = new MethodBinding[foundSize];
		int candidatesCount = 0;

		// argument type compatibility check
		for (int i = 0; i < foundSize; i++) {
			MethodBinding methodBinding = (MethodBinding) found.elementAt(i);
			if (areParametersAssignable(methodBinding.parameters, argumentTypes))
				candidates[candidatesCount++] = methodBinding;
		}
		if (candidatesCount == 1) {
			//compilationUnitScope().recordTypeReferences(candidates[0].thrownExceptions);
			return candidates[0]; // have not checked visibility
		}
		if (candidatesCount == 0) { // try to find a close match when the parameter order is wrong or missing some parameters
			MethodBinding interfaceMethod = findDefaultAbstractMethod(receiverType, selector, argumentTypes, invocationSite, classHierarchyStart, matchingMethod, found);
			if (interfaceMethod != null) return interfaceMethod;

			int argLength = argumentTypes.length;
			foundSize = found.size;
			nextMethod : for (int i = 0; i < foundSize; i++) {
				MethodBinding methodBinding = (MethodBinding) found.elementAt(i);
				TypeBinding[] params = methodBinding.parameters;
				int paramLength = params.length;
				nextArg: for (int a = 0; a < argLength; a++) {
					TypeBinding arg = argumentTypes[a];
					for (int p = 0; p < paramLength; p++)
						if (params[p] == arg)
							continue nextArg;
					continue nextMethod;
				}
				return methodBinding;
			}
			return (MethodBinding) found.elementAt(0); // no good match so just use the first one found
		}

		// visibility check
		int visiblesCount = 0;
		for (int i = 0; i < candidatesCount; i++) {
			MethodBinding methodBinding = candidates[i];
			if (canBeSeenByForCodeSnippet(methodBinding, receiverType, invocationSite, this)) {
				if (visiblesCount != i) {
					candidates[i] = null;
					candidates[visiblesCount] = methodBinding;
				}
				visiblesCount++;
			}
		}
		if (visiblesCount == 1) {
			//compilationUnitScope().recordTypeReferences(candidates[0].thrownExceptions);
			return candidates[0];
		}
		if (visiblesCount == 0) {
			MethodBinding interfaceMethod = findDefaultAbstractMethod(receiverType, selector, argumentTypes, invocationSite, classHierarchyStart, matchingMethod, found);
			if (interfaceMethod != null) return interfaceMethod;
			return new ProblemMethodBinding(
				candidates[0].selector,
				argumentTypes,
				candidates[0].declaringClass,
				NotVisible);
		}	
		if (candidates[0].declaringClass.isClass()) {
			return mostSpecificClassMethodBinding(candidates, visiblesCount);
		} else {
			return mostSpecificInterfaceMethodBinding(candidates, visiblesCount);
		}
	}

// Internal use only
public MethodBinding findMethodForArray(ArrayBinding receiverType, char[] selector, TypeBinding[] argumentTypes, InvocationSite invocationSite) {
	ReferenceBinding object = getJavaLangObject();
	MethodBinding methodBinding = object.getExactMethod(selector, argumentTypes);
	if (methodBinding != null) {
		// handle the method clone() specially... cannot be protected or throw exceptions
		if (argumentTypes == NoParameters && CharOperation.equals(selector, CLONE))
			return new MethodBinding((methodBinding.modifiers ^ AccProtected) | AccPublic, CLONE, methodBinding.returnType, argumentTypes, null, object);
		if (canBeSeenByForCodeSnippet(methodBinding, receiverType, invocationSite, this))
			return methodBinding;
	}

	// answers closest approximation, may not check argumentTypes or visibility
	methodBinding = findMethod(object, selector, argumentTypes, invocationSite);
	if (methodBinding == null)
		return new ProblemMethodBinding(selector, argumentTypes, NotFound);
	if (methodBinding.isValidBinding()) {
		if (!areParametersAssignable(methodBinding.parameters, argumentTypes))
			return new ProblemMethodBinding(methodBinding, selector, argumentTypes, NotFound);
		if (!canBeSeenByForCodeSnippet(methodBinding, receiverType, invocationSite, this))
			return new ProblemMethodBinding(selector, argumentTypes, methodBinding.declaringClass, NotVisible);
	}
	return methodBinding;
}
/* API
	flag is a mask of the following values VARIABLE (= FIELD or LOCAL), TYPE.
	Only bindings corresponding to the mask will be answered.

	if the VARIABLE mask is set then
		If the first name provided is a field (or local) then the field (or local) is answered
		Otherwise, package names and type names are consumed until a field is found.
		In this case, the field is answered.

	if the TYPE mask is set,
		package names and type names are consumed until the end of the input.
		Only if all of the input is consumed is the type answered

	All other conditions are errors, and a problem binding is returned.
	
	NOTE: If a problem binding is returned, senders should extract the compound name
	from the binding & not assume the problem applies to the entire compoundName.

	The VARIABLE mask has precedence over the TYPE mask.

	InvocationSite implements
		isSuperAccess(); this is used to determine if the discovered field is visible.
		setFieldIndex(int); this is used to record the number of names that were consumed.

	For example, getBinding({"foo","y","q", VARIABLE, site) will answer
	the binding for the field or local named "foo" (or an error binding if none exists).
	In addition, setFieldIndex(1) will be sent to the invocation site.
	If a type named "foo" exists, it will not be detected (and an error binding will be answered)

	IMPORTANT NOTE: This method is written under the assumption that compoundName is longer than length 1.
*/

public Binding getBinding(char[][] compoundName, int mask, InvocationSite invocationSite, ReferenceBinding receiverType) {
	Binding binding = getBinding(compoundName[0], mask | TYPE | PACKAGE, invocationSite);
	invocationSite.setFieldIndex(1);
	if (!binding.isValidBinding() || binding instanceof VariableBinding)
		return binding;

	int length = compoundName.length;
	int currentIndex = 1;
	foundType: if (binding instanceof PackageBinding) {
		PackageBinding packageBinding = (PackageBinding) binding;

		while (currentIndex < length) {
			binding = packageBinding.getTypeOrPackage(compoundName[currentIndex++]);
			invocationSite.setFieldIndex(currentIndex);
 			if (binding == null) {
	 			if (currentIndex == length) // must be a type if its the last name, otherwise we have no idea if its a package or type
					return new ProblemReferenceBinding(CharOperation.subarray(compoundName, 0, currentIndex), NotFound);
				else
					return new ProblemBinding(CharOperation.subarray(compoundName, 0, currentIndex), NotFound);
 			}
 			if (binding instanceof ReferenceBinding) {
	 			if (!binding.isValidBinding())
					return new ProblemReferenceBinding(CharOperation.subarray(compoundName, 0, currentIndex), binding.problemId());
	 			if (!this.canBeSeenByForCodeSnippet((ReferenceBinding) binding, receiverType))
					return new ProblemReferenceBinding(CharOperation.subarray(compoundName, 0, currentIndex), binding, NotVisible);
	 			break foundType;
 			}
 			packageBinding = (PackageBinding) binding;
		}

		// It is illegal to request a PACKAGE from this method.
		return new ProblemReferenceBinding(CharOperation.subarray(compoundName, 0, currentIndex), NotFound);
	}

	// know binding is now a ReferenceBinding
	while (currentIndex < length) {
		ReferenceBinding typeBinding = (ReferenceBinding) binding;
		char[] nextName = compoundName[currentIndex++];
		invocationSite.setFieldIndex(currentIndex);
		if ((binding = findFieldForCodeSnippet(typeBinding, nextName, invocationSite)) != null) {
			if (!binding.isValidBinding())
				return new ProblemFieldBinding(((FieldBinding)binding).declaringClass, CharOperation.subarray(compoundName, 0, currentIndex), binding.problemId());
			break; // binding is now a field
		}
		if ((binding = findMemberType(nextName, typeBinding)) == null)
			return new ProblemBinding(CharOperation.subarray(compoundName, 0, currentIndex), typeBinding, NotFound);
		 if (!binding.isValidBinding())
			return new ProblemReferenceBinding(CharOperation.subarray(compoundName, 0, currentIndex), binding.problemId());
	}

	if ((mask & FIELD) != 0 && (binding instanceof FieldBinding)) { // was looking for a field and found a field
		FieldBinding field = (FieldBinding) binding;
		if (!field.isStatic())
			return new ProblemFieldBinding(field.declaringClass, CharOperation.subarray(compoundName, 0, currentIndex), NonStaticReferenceInStaticContext);
		return binding;
	}
	if ((mask & TYPE) != 0 && (binding instanceof ReferenceBinding)) { // was looking for a type and found a type
		return binding;
	}

	// handle the case when a field or type was asked for but we resolved the compoundName to a type or field
	return new ProblemBinding(CharOperation.subarray(compoundName, 0, currentIndex), NotFound);
}
/* API

	Answer the constructor binding that corresponds to receiverType, argumentTypes.

	InvocationSite implements 
		isSuperAccess(); this is used to determine if the discovered constructor is visible.

	If no visible constructor is discovered, an error binding is answered.
*/

public MethodBinding getConstructor(ReferenceBinding receiverType, TypeBinding[] argumentTypes, InvocationSite invocationSite) {
	MethodBinding methodBinding = receiverType.getExactConstructor(argumentTypes);
	if (methodBinding != null)
		if (canBeSeenByForCodeSnippet(methodBinding, receiverType, invocationSite, this))
			return methodBinding;

	MethodBinding[] methods = receiverType.getMethods(ConstructorDeclaration.ConstantPoolName);
	if (methods == NoMethods)
		return new ProblemMethodBinding(ConstructorDeclaration.ConstantPoolName, argumentTypes, NotFound);

	MethodBinding[] compatible = new MethodBinding[methods.length];
	int compatibleIndex = 0;
	for (int i = 0, length = methods.length; i < length; i++)
		if (areParametersAssignable(methods[i].parameters, argumentTypes))
			compatible[compatibleIndex++] = methods[i];
	if (compatibleIndex == 0)
		return new ProblemMethodBinding(ConstructorDeclaration.ConstantPoolName, argumentTypes, NotFound); // need a more descriptive error... cannot convert from X to Y

	MethodBinding[] visible = new MethodBinding[compatibleIndex];
	int visibleIndex = 0;
	for (int i = 0; i < compatibleIndex; i++) {
		MethodBinding method = compatible[i];
		if (canBeSeenByForCodeSnippet(method, receiverType, invocationSite, this))
			visible[visibleIndex++] = method;
	}
	if (visibleIndex == 1)
		return visible[0];
	if (visibleIndex == 0)
		return new ProblemMethodBinding(ConstructorDeclaration.ConstantPoolName, argumentTypes, NotVisible);
	return mostSpecificClassMethodBinding(visible, visibleIndex);
}
/* API

	Answer the field binding that corresponds to fieldName.
	Start the lookup at the receiverType.
	InvocationSite implements
		isSuperAccess(); this is used to determine if the discovered field is visible.
	Only fields defined by the receiverType or its supertypes are answered;
	a field of an enclosing type will not be found using this API.

	If no visible field is discovered, an error binding is answered.
*/

public FieldBinding getFieldForCodeSnippet(TypeBinding receiverType, char[] fieldName, InvocationSite invocationSite) {
	FieldBinding field = findFieldForCodeSnippet(receiverType, fieldName, invocationSite);
	if (field == null)
		return new ProblemFieldBinding(receiverType instanceof ReferenceBinding ? (ReferenceBinding) receiverType : null, fieldName, NotFound);
	else
		return field;
}
/* API

	Answer the method binding that corresponds to selector, argumentTypes.
	Start the lookup at the enclosing type of the receiver.
	InvocationSite implements 
		isSuperAccess(); this is used to determine if the discovered method is visible.
		setDepth(int); this is used to record the depth of the discovered method
			relative to the enclosing type of the receiver. (If the method is defined
			in the enclosing type of the receiver, the depth is 0; in the next enclosing
			type, the depth is 1; and so on

	If no visible method is discovered, an error binding is answered.
*/

public MethodBinding getImplicitMethod(ReferenceBinding receiverType, char[] selector, TypeBinding[] argumentTypes, InvocationSite invocationSite) {
	boolean insideStaticContext = false;
	boolean insideConstructorCall = false;
	MethodBinding foundMethod = null;
	ProblemMethodBinding foundFuzzyProblem = null; // the weird method lookup case (matches method name in scope, then arg types, then visibility)
	ProblemMethodBinding foundInsideProblem = null; // inside Constructor call or inside static context
	Scope scope = this;
	boolean isExactMatch = true;
	// retrieve an exact visible match (if possible)
	MethodBinding methodBinding =
		(foundMethod == null)
			? findExactMethod(receiverType, selector, argumentTypes, invocationSite)
			: findExactMethod(receiverType, foundMethod.selector, foundMethod.parameters, invocationSite);
//						? findExactMethod(receiverType, selector, argumentTypes, invocationSite)
//						: findExactMethod(receiverType, foundMethod.selector, foundMethod.parameters, invocationSite);
	if (methodBinding == null && foundMethod == null) {
		// answers closest approximation, may not check argumentTypes or visibility
		isExactMatch = false;
		methodBinding = findMethod(receiverType, selector, argumentTypes, invocationSite);
//					methodBinding = findMethod(receiverType, selector, argumentTypes, invocationSite);
	}
	if (methodBinding != null) { // skip it if we did not find anything
		if (methodBinding.problemId() == Ambiguous) {
			if (foundMethod == null || foundMethod.problemId() == NotVisible)
				// supercedes any potential InheritedNameHidesEnclosingName problem
				return methodBinding;
			else
				// make the user qualify the method, likely wants the first inherited method (javac generates an ambiguous error instead)
				return new ProblemMethodBinding(selector, argumentTypes, InheritedNameHidesEnclosingName);
		}

		ProblemMethodBinding fuzzyProblem = null;
		ProblemMethodBinding insideProblem = null;
		if (methodBinding.isValidBinding()) {
			if (!isExactMatch) {
				if (!areParametersAssignable(methodBinding.parameters, argumentTypes)) {
					fuzzyProblem = new ProblemMethodBinding(methodBinding, selector, argumentTypes, NotFound);
				} else if (!canBeSeenByForCodeSnippet(methodBinding, receiverType, invocationSite, this)) {	
					// using <classScope> instead of <this> for visibility check does grant all access to innerclass
					fuzzyProblem = new ProblemMethodBinding(selector, argumentTypes, methodBinding.declaringClass, NotVisible);
				}
			}
			if (fuzzyProblem == null && !methodBinding.isStatic()) {
				if (insideConstructorCall) {
					insideProblem = new ProblemMethodBinding(methodBinding.selector, methodBinding.parameters, NonStaticReferenceInConstructorInvocation);
				} else if (insideStaticContext) {
					insideProblem = new ProblemMethodBinding(methodBinding.selector, methodBinding.parameters, NonStaticReferenceInStaticContext);
				}
			}
			if (receiverType == methodBinding.declaringClass || (receiverType.getMethods(selector)) != NoMethods) {
				// found a valid method in the 'immediate' scope (ie. not inherited)
				// OR the receiverType implemented a method with the correct name
				if (foundMethod == null) {
					// return the methodBinding if it is not declared in a superclass of the scope's binding (i.e. "inherited")
					if (fuzzyProblem != null)
						return fuzzyProblem;
					if (insideProblem != null)
						return insideProblem;
					return methodBinding;
				}
				// if a method was found, complain when another is found in an 'immediate' enclosing type (ie. not inherited)
				// NOTE: Unlike fields, a non visible method hides a visible method
				if (foundMethod.declaringClass != methodBinding.declaringClass) // ie. have we found the same method - do not trust field identity yet
					return new ProblemMethodBinding(methodBinding.selector, methodBinding.parameters, InheritedNameHidesEnclosingName);
			}
		}

		if (foundMethod == null || (foundMethod.problemId() == NotVisible && methodBinding.problemId() != NotVisible)) {
			// only remember the methodBinding if its the first one found or the previous one was not visible & methodBinding is...
			// remember that private methods are visible if defined directly by an enclosing class
			foundFuzzyProblem = fuzzyProblem;
			foundInsideProblem = insideProblem;
			if (fuzzyProblem == null)
				foundMethod = methodBinding; // only keep it if no error was found
		}
	}
	insideStaticContext |= receiverType.isStatic();
	// 1EX5I8Z - accessing outer fields within a constructor call is permitted
	// in order to do so, we change the flag as we exit from the type, not the method
	// itself, because the class scope is used to retrieve the fields.
	MethodScope enclosingMethodScope = scope.methodScope();
	insideConstructorCall = enclosingMethodScope == null ? false : enclosingMethodScope.isConstructorCall;

	if (foundFuzzyProblem != null)
		return foundFuzzyProblem;
	if (foundInsideProblem != null)
		return foundInsideProblem;
	if (foundMethod != null)
		return foundMethod;
	return new ProblemMethodBinding(selector, argumentTypes, NotFound);
}
}
