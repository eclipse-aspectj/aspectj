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

import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.eclipse.jdt.internal.compiler.util.CharOperation;
import org.eclipse.jdt.internal.compiler.util.HashtableOfObject;

public final class MethodVerifier implements TagBits, TypeConstants {
	SourceTypeBinding type;
	HashtableOfObject inheritedMethods;
	HashtableOfObject currentMethods;
	ReferenceBinding runtimeException;
	ReferenceBinding errorException;
/*
Binding creation is responsible for reporting all problems with types:
	- all modifier problems (duplicates & multiple visibility modifiers + incompatible combinations - abstract/final)
		- plus invalid modifiers given the context (the verifier did not do this before)
	- qualified name collisions between a type and a package (types in default packages are excluded)
	- all type hierarchy problems:
		- cycles in the superclass or superinterface hierarchy
		- an ambiguous, invisible or missing superclass or superinterface
		- extending a final class
		- extending an interface instead of a class
		- implementing a class instead of an interface
		- implementing the same interface more than once (ie. duplicate interfaces)
	- with nested types:
		- shadowing an enclosing type's source name
		- defining a static class or interface inside a non-static nested class
		- defining an interface as a local type (local types can only be classes)

verifyTypeStructure

	| hasHierarchyProblem superclass current names interfaces interfacesByIndentity duplicateExists invalidType |

	(type basicModifiers anyMask: AccModifierProblem | AccAlternateModifierProblem) ifTrue: [
		self reportModifierProblemsOnType: type].

	type controller isJavaDefaultPackage ifFalse: [
		(nameEnvironment class doesPackageExistNamed: type javaQualifiedName) ifTrue: [
			problemSummary
				reportVerificationProblem: #CollidesWithPackage
				args: (Array with: type javaQualifiedName)
				severity: nil
				forType: type]].

	hasHierarchyProblem := false.

	type isJavaClass
		ifTrue: [
			(superclass := self superclassFor: type) ~~ nil ifTrue: [
				superclass isBuilderClass ifTrue: [
					superclass := superclass newClass].
				superclass isJavaMissing
					ifTrue: [
						hasHierarchyProblem := true.
						type javaSuperclassIsMissing ifTrue: [
							problemSummary
								reportVerificationProblem: #MissingSuperclass
								args: (Array with: superclass javaQualifiedName with: superclass unmatchedDescriptor)
								severity: nil
								forType: type].
						type javaSuperclassCreatesCycle ifTrue: [
							problemSummary
								reportVerificationProblem: #CyclicSuperclass
								args: (Array with: superclass javaQualifiedName)
								severity: nil
								forType: type].
						type javaSuperclassIsInterface ifTrue: [
							problemSummary
								reportVerificationProblem: #ClassCannotExtendAnInterface
								args: (Array with: superclass javaQualifiedName)
								severity: nil
								forType: type]]
					ifFalse: [
						"NOTE:  If type is a Java class and its superclass is
						a valid descriptor then it should NEVER be an interface."

						superclass isJavaFinal ifTrue: [
							problemSummary
								reportVerificationProblem: #ClassCannotExtendFinalClass
								args: nil
								severity: nil
								forType: type]]]]
		ifFalse: [
			type isJavaLocalType ifTrue: [
				problemSummary
					reportVerificationProblem: #CannotDefineLocalInterface
					args: nil
					severity: nil
					forType: type]].

	type isJavaNestedType ifTrue: [
		(current := type) sourceName notEmpty ifTrue: [
			names := Set new.
			[(current := current enclosingType) ~~ nil] whileTrue: [
				names add: current sourceName].

			(names includes: type sourceName) ifTrue: [
				problemSummary
					reportVerificationProblem: #NestedTypeCannotShadowTypeName
					args: nil
					severity: nil
					forType: type]].

		(type enclosingType isJavaNestedType and: [type enclosingType isJavaClass]) ifTrue: [
			type enclosingType isJavaStatic ifFalse: [
				type isJavaClass
					ifTrue: [
						type isJavaStatic ifTrue: [
							problemSummary
								reportVerificationProblem: #StaticClassCannotExistInNestedClass
								args: nil
								severity: nil
								forType: type]]
					ifFalse: [
						problemSummary
							reportVerificationProblem: #InterfaceCannotExistInNestedClass
							args: nil
							severity: nil
							forType: type]]]].

	(interfaces := newClass superinterfaces) notEmpty ifTrue: [
		interfacesByIndentity := interfaces asSet.
		duplicateExists := interfaces size ~~ interfacesByIndentity size.

		interfacesByIndentity do: [:interface |
			duplicateExists ifTrue: [
				(interfaces occurrencesOf: interface) > 1 ifTrue: [
					problemSummary
						reportVerificationProblem: #InterfaceIsSpecifiedMoreThanOnce
						args: (Array with: interface javaQualifiedName)
						severity: nil
						forType: type]].

			interface isJavaMissing ifTrue: [
				hasHierarchyProblem := true.
				interface basicClass == JavaInterfaceIsClass basicClass
					ifTrue: [
						problemSummary
							reportVerificationProblem: #UsingClassWhereInterfaceIsRequired
							args: (Array with: interface javaQualifiedName)
							severity: nil
							forType: type]
					ifFalse: [
						interface basicClass == JavaMissingInterface basicClass
							ifTrue: [
								problemSummary
									reportVerificationProblem: #MissingInterface
									args: (Array with: interface javaQualifiedName with: interface unmatchedDescriptor)
									severity: nil
									forType: type]
							ifFalse: [
								problemSummary
									reportVerificationProblem: #CyclicSuperinterface
									args: (Array with: interface javaQualifiedName)
									severity: nil
									forType: type]]]]].

	hasHierarchyProblem ifFalse: [
		"Search up the type's hierarchy for
			1. missing superclass,
			2. superclass cycle, or
			3. superclass is interface."
		(invalidType := newClass findFirstInvalidSupertypeSkipping: EsIdentitySet new) ~~ nil ifTrue: [
			problemSummary
				reportVerificationProblem: #HasHierarchyProblem
				args: (Array with: invalidType javaReadableName)
				severity: nil
				forType: type]]

reportModifierProblemsOnType: aType

	(type basicModifiers anyMask: AccAlternateModifierProblem) ifTrue: [
		(type basicModifiers anyMask: AccModifierProblem)
			ifTrue: [
				^problemSummary
					reportVerificationProblem: #OnlyOneVisibilityModifierAllowed
					args: nil
					severity: nil
					forType: aType]
			ifFalse: [
				^problemSummary
					reportVerificationProblem: #DuplicateModifier
					args: nil
					severity: nil
					forType: aType]].

	type isJavaInterface ifTrue: [
		^problemSummary
			reportVerificationProblem: #IllegalModifierForInterface
			args: nil
			severity: nil
			forType: aType].

	(type basicModifiers allMask: AccAbstract | AccFinal) ifTrue: [
		^problemSummary
			reportVerificationProblem: #IllegalModifierCombinationAbstractFinal
			args: nil
			severity: nil
			forType: aType].

	^problemSummary
		reportVerificationProblem: #IllegalModifierForClass
		args: nil
		severity: nil
		forType: aType

void reportModifierProblems() {
	if (this.type.isAbstract() && this.type.isFinal())
		this.problemReporter.illegalModifierCombinationAbstractFinal(this.type);

	// Should be able to detect all 3 problems NOT just 1
	if ((type.modifiers() & Modifiers.AccAlternateModifierProblem) == 0) {
		if (this.type.isInterface())
			this.problemReporter.illegalModifierForInterface(this.type);
		else
			this.problemReporter.illegalModifier(this.type);
	} else {
		if ((type.modifiers() & Modifiers.AccModifierProblem) != 0)
			this.problemReporter.onlyOneVisibilityModifierAllowed(this.type);
		else
			this.problemReporter.duplicateModifier(this.type);
	}
}
*/
public MethodVerifier(LookupEnvironment environment) {
	this.type = null;		// Initialized with the public method verify(SourceTypeBinding)
	this.inheritedMethods = null;
	this.currentMethods = null;
	this.runtimeException = null;
	this.errorException = null;
}
private void checkAgainstInheritedMethods(MethodBinding currentMethod, MethodBinding[] methods, int length) {
	for (int i = length; --i >= 0;) {
		MethodBinding inheritedMethod = methods[i];
		if (currentMethod.returnType != inheritedMethod.returnType) {
			this.problemReporter(currentMethod).incompatibleReturnType(currentMethod, inheritedMethod);
		} else if (currentMethod.isStatic() != inheritedMethod.isStatic())	 {	// Cannot override a static method or hide an instance method
			this.problemReporter(currentMethod).staticAndInstanceConflict(currentMethod, inheritedMethod);
		} else {
			if (currentMethod.thrownExceptions != NoExceptions)
				this.checkExceptions(currentMethod, inheritedMethod);
			if (inheritedMethod.isFinal())
				this.problemReporter(currentMethod).finalMethodCannotBeOverridden(currentMethod, inheritedMethod);
			if (!this.isAsVisible(currentMethod, inheritedMethod))
				this.problemReporter(currentMethod).visibilityConflict(currentMethod, inheritedMethod);
			if (inheritedMethod.isViewedAsDeprecated())
				if (!currentMethod.isViewedAsDeprecated())
					this.problemReporter(currentMethod).overridesDeprecatedMethod(currentMethod, inheritedMethod);
		}
	}
}
/*
"8.4.4"
Verify that newExceptions are all included in inheritedExceptions.
Assumes all exceptions are valid and throwable.
Unchecked exceptions (compatible with runtime & error) are ignored (see the spec on pg. 203).
*/

private void checkExceptions(MethodBinding newMethod, MethodBinding inheritedMethod) {
	ReferenceBinding[] newExceptions = newMethod.thrownExceptions;
	ReferenceBinding[] inheritedExceptions = inheritedMethod.thrownExceptions;
	for (int i = newExceptions.length; --i >= 0;) {
		ReferenceBinding newException = newExceptions[i];
		int j = inheritedExceptions.length;
		while (--j > -1 && !this.isSameClassOrSubclassOf(newException, inheritedExceptions[j]));
		if (j == -1)
			if (!(newException.isCompatibleWith(this.runtimeException()) || newException.isCompatibleWith(this.errorException())))
				this.problemReporter(newMethod).incompatibleExceptionInThrowsClause(this.type, newMethod, inheritedMethod, newException);
	}
}
private void checkInheritedMethods(MethodBinding[] methods, int length) {
	TypeBinding returnType = methods[0].returnType;
	int index = length;
	while ((--index > 0) && (returnType == methods[index].returnType));
	if (index > 0) {		// All inherited methods do NOT have the same vmSignature
		this.problemReporter().inheritedMethodsHaveIncompatibleReturnTypes(this.type, methods, length);
		return;
	}

	MethodBinding concreteMethod = null;
	if (!type.isInterface()){ // ignore concrete methods for interfaces
		for (int i = length; --i >= 0;)		// Remember that only one of the methods can be non-abstract
			if (!methods[i].isAbstract()) {
				concreteMethod = methods[i];
				break;
			}
	}
	if (concreteMethod == null) {
		if (this.type.isClass() && !this.type.isAbstract()) {
			for (int i = length; --i >= 0;)
				if (!mustImplementAbstractMethod(methods[i]))
					return;		// in this case, we have already reported problem against the concrete superclass

				TypeDeclaration typeDeclaration = this.type.scope.referenceContext;
				if (typeDeclaration != null) {
					MethodDeclaration missingAbstractMethod = typeDeclaration.addMissingAbstractMethodFor(methods[0]);
					missingAbstractMethod.scope.problemReporter().abstractMethodMustBeImplemented(this.type, methods[0]);
				} else {
					this.problemReporter().abstractMethodMustBeImplemented(this.type, methods[0]);
				}
		}
		return;
	}

	MethodBinding[] abstractMethods = new MethodBinding[length - 1];
	index = 0;
	for (int i = length; --i >= 0;)
		if (methods[i] != concreteMethod)
			abstractMethods[index++] = methods[i];

	// Remember that interfaces can only define public instance methods
	if (concreteMethod.isStatic())
		// Cannot inherit a static method which is specified as an instance method by an interface
		this.problemReporter().staticInheritedMethodConflicts(type, concreteMethod, abstractMethods);	
	if (!concreteMethod.isPublic())
		// Cannot reduce visibility of a public method specified by an interface
		this.problemReporter().inheritedMethodReducesVisibility(type, concreteMethod, abstractMethods);
	if (concreteMethod.thrownExceptions != NoExceptions)
		for (int i = abstractMethods.length; --i >= 0;)
			this.checkExceptions(concreteMethod, abstractMethods[i]);
}
/*
For each inherited method identifier (message pattern - vm signature minus the return type)
	if current method exists
		if current's vm signature does not match an inherited signature then complain 
		else compare current's exceptions & visibility against each inherited method
	else
		if inherited methods = 1
			if inherited is abstract && type is NOT an interface or abstract, complain
		else
			if vm signatures do not match complain
			else
				find the concrete implementation amongst the abstract methods (can only be 1)
				if one exists then
					it must be a public instance method
					compare concrete's exceptions against each abstract method
				else
					complain about missing implementation only if type is NOT an interface or abstract
*/

private void checkMethods() {
	boolean mustImplementAbstractMethods = this.type.isClass() && !this.type.isAbstract();
	char[][] methodSelectors = this.inheritedMethods.keyTable;
	for (int s = methodSelectors.length; --s >= 0;) {
		if (methodSelectors[s] != null) {
			MethodBinding[] current = (MethodBinding[]) this.currentMethods.get(methodSelectors[s]);
			MethodBinding[] inherited = (MethodBinding[]) this.inheritedMethods.valueTable[s];

			int index = -1;
			MethodBinding[] matchingInherited = new MethodBinding[inherited.length];
			if (current != null) {
				for (int i = 0, length1 = current.length; i < length1; i++) {
					while (index >= 0) matchingInherited[index--] = null; // clear the previous contents of the matching methods
					MethodBinding currentMethod = current[i];
					for (int j = 0, length2 = inherited.length; j < length2; j++) {
						if (inherited[j] != null && currentMethod.areParametersEqual(inherited[j])) {
							matchingInherited[++index] = inherited[j];
							inherited[j] = null; // do not want to find it again
						}
					}
					if (index >= 0)
						this.checkAgainstInheritedMethods(currentMethod, matchingInherited, index + 1); // pass in the length of matching
				}
			}
			for (int i = 0, length = inherited.length; i < length; i++) {
				while (index >= 0) matchingInherited[index--] = null; // clear the previous contents of the matching methods
				if (inherited[i] != null) {
					matchingInherited[++index] = inherited[i];
					for (int j = i + 1; j < length; j++) {
						if (inherited[j] != null && inherited[i].areParametersEqual(inherited[j])) {
							matchingInherited[++index] = inherited[j];
							inherited[j] = null; // do not want to find it again
						}
					}
				}
				if (index > 0) {
					this.checkInheritedMethods(matchingInherited, index + 1); // pass in the length of matching
				} else {
					if (mustImplementAbstractMethods && index == 0 && matchingInherited[0].isAbstract())
						if (mustImplementAbstractMethod(matchingInherited[0])) {
							TypeDeclaration typeDeclaration = this.type.scope.referenceContext;
							if (typeDeclaration != null) {
								MethodDeclaration missingAbstractMethod = typeDeclaration.addMissingAbstractMethodFor(matchingInherited[0]);
								missingAbstractMethod.scope.problemReporter().abstractMethodMustBeImplemented(this.type, matchingInherited[0]);
							} else {
								this.problemReporter().abstractMethodMustBeImplemented(this.type, matchingInherited[0]);
							}
						}
				}
			}
		}
	}
}
/*
Binding creation is responsible for reporting:
	- all modifier problems (duplicates & multiple visibility modifiers + incompatible combinations)
		- plus invalid modifiers given the context... examples:
			- interface methods can only be public
			- abstract methods can only be defined by abstract classes
	- collisions... 2 methods with identical vmSelectors
	- multiple methods with the same message pattern but different return types
	- ambiguous, invisible or missing return/argument/exception types
	- check the type of any array is not void
	- check that each exception type is Throwable or a subclass of it
*/
private void computeInheritedMethods() {
	this.inheritedMethods = new HashtableOfObject(51); // maps method selectors to an array of methods... must search to match paramaters & return type
	ReferenceBinding[][] interfacesToVisit = new ReferenceBinding[5][];
	int lastPosition = 0;
	interfacesToVisit[lastPosition] = type.superInterfaces();

	ReferenceBinding superType;
	if (this.type.isClass()) {
		superType = this.type.superclass();
	} else { // check interface methods against Object
		superType = this.type.scope.getJavaLangObject();
	}
	MethodBinding[] nonVisibleDefaultMethods = null;
	int nonVisibleCount = 0;

	while (superType != null) {
		if (superType.isValidBinding()) {
			ReferenceBinding[] itsInterfaces = superType.superInterfaces();
			if (itsInterfaces != NoSuperInterfaces) {
				if (++lastPosition == interfacesToVisit.length)
					System.arraycopy(interfacesToVisit, 0, interfacesToVisit = new ReferenceBinding[lastPosition * 2][], 0, lastPosition);
				interfacesToVisit[lastPosition] = itsInterfaces;
			}

			MethodBinding[] methods = superType.methods();
			nextMethod : for (int m = methods.length; --m >= 0;) {
				MethodBinding method = methods[m];
				if (!(method.isPrivate() || method.isConstructor() || method.isDefaultAbstract())) { // look at all methods which are NOT private or constructors or default abstract
					MethodBinding[] existingMethods = (MethodBinding[]) this.inheritedMethods.get(method.selector);
					if (existingMethods != null)
						for (int i = 0, length = existingMethods.length; i < length; i++)
							if (method.returnType == existingMethods[i].returnType)
								if (method.areParametersEqual(existingMethods[i]))
									continue nextMethod;
					if (nonVisibleDefaultMethods != null)
						for (int i = 0; i < nonVisibleCount; i++)
							if (method.returnType == nonVisibleDefaultMethods[i].returnType)
								if (CharOperation.equals(method.selector, nonVisibleDefaultMethods[i].selector))
									if (method.areParametersEqual(nonVisibleDefaultMethods[i]))
										continue nextMethod;

					if (!(method.isDefault() && (method.declaringClass.fPackage != type.fPackage))) { // ignore methods which have default visibility and are NOT defined in another package
						if (existingMethods == null)
							existingMethods = new MethodBinding[1];
						else
							System.arraycopy(existingMethods, 0,
								(existingMethods = new MethodBinding[existingMethods.length + 1]), 0, existingMethods.length - 1);
						existingMethods[existingMethods.length - 1] = method;
						this.inheritedMethods.put(method.selector, existingMethods);
					} else {
						if (nonVisibleDefaultMethods == null)
							nonVisibleDefaultMethods = new MethodBinding[10];
						else if (nonVisibleCount == nonVisibleDefaultMethods.length)
							System.arraycopy(nonVisibleDefaultMethods, 0,
								(nonVisibleDefaultMethods = new MethodBinding[nonVisibleCount * 2]), 0, nonVisibleCount);
						nonVisibleDefaultMethods[nonVisibleCount++] = method;

						if (method.isAbstract() && !this.type.isAbstract()) // non visible abstract methods cannot be overridden so the type must be defined abstract
							this.problemReporter().abstractMethodCannotBeOverridden(this.type, method);

						MethodBinding[] current = (MethodBinding[]) this.currentMethods.get(method.selector);
						if (current != null) { // non visible methods cannot be overridden so a warning is issued
							foundMatch : for (int i = 0, length = current.length; i < length; i++) {
								if (method.returnType == current[i].returnType) {
									if (method.areParametersEqual(current[i])) {
										this.problemReporter().overridesPackageDefaultMethod(current[i], method);
										break foundMatch;
									}
								}
							}
						}
					}
				}
			}
			superType = superType.superclass();
		}
	}

	for (int i = 0; i <= lastPosition; i++) {
		ReferenceBinding[] interfaces = interfacesToVisit[i];
		for (int j = 0, length = interfaces.length; j < length; j++) {
			superType = interfaces[j];
			if ((superType.tagBits & InterfaceVisited) == 0) {
				superType.tagBits |= InterfaceVisited;
				if (superType.isValidBinding()) {
					ReferenceBinding[] itsInterfaces = superType.superInterfaces();
					if (itsInterfaces != NoSuperInterfaces) {
						if (++lastPosition == interfacesToVisit.length)
							System.arraycopy(interfacesToVisit, 0, interfacesToVisit = new ReferenceBinding[lastPosition * 2][], 0, lastPosition);
						interfacesToVisit[lastPosition] = itsInterfaces;
					}

					MethodBinding[] methods = superType.methods();
					for (int m = methods.length; --m >= 0;) { // Interface methods are all abstract public
						MethodBinding method = methods[m];
						MethodBinding[] existingMethods = (MethodBinding[]) this.inheritedMethods.get(method.selector);
						if (existingMethods == null)
							existingMethods = new MethodBinding[1];
						else
							System.arraycopy(existingMethods, 0,
								(existingMethods = new MethodBinding[existingMethods.length + 1]), 0, existingMethods.length - 1);
						existingMethods[existingMethods.length - 1] = method;
						this.inheritedMethods.put(method.selector, existingMethods);
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
}
/*
computeInheritedMethodMembers

	"8.4.6.4"
	"Compute all of the members for the type that are inherited from its supertypes.
		This includes:
			All of the methods implemented in the supertype hierarchy that are not overridden.
			PROBLEM:  Currently we do not remove overridden methods in the interface hierarchy.
			This could cause a non-existent exception error to be detected."

	| supertype allSuperinterfaces methodsSeen interfacesSeen |
	inheritedMethodMembers := LookupTable new: 50.
	allSuperinterfaces := OrderedCollection new.

	type isJavaClass ifTrue: [
		supertype := type.
		methodsSeen := EsIdentitySet new: 20.
		[(supertype := self superclassFor: supertype) == nil] whileFalse: [
			(supertype isBuilderClass or: [supertype isValidDescriptor]) ifTrue: [
				allSuperinterfaces addAll: (self superinterfacesFor: supertype).
				supertype javaUserDefinedMethodsDo: [:method |
					(method isJavaPrivate or: [method isJavaConstructor]) ifFalse: [
						(method isJavaDefault and: [method declaringClass package symbol ~= type package symbol]) ifFalse: [
							(methodsSeen includes: method selector) ifFalse: [
								methodsSeen add: method selector.
								(inheritedMethodMembers
									at: (self methodSignatureFor: method selector)
									ifAbsentPut: [OrderedCollection new: 3])
										add: method]]]]]]].

	allSuperinterfaces addAll: (self superinterfacesFor: type).
	interfacesSeen := EsIdentitySet new: allSuperinterfaces size * 2.
	[allSuperinterfaces notEmpty] whileTrue: [
		supertype := allSuperinterfaces removeFirst.
		(interfacesSeen includes: supertype) ifFalse: [
			interfacesSeen add: supertype.
			(supertype isBuilderClass or: [supertype isValidDescriptor]) ifTrue: [
				allSuperinterfaces addAll: (self superinterfacesFor: supertype).
				supertype javaUserDefinedMethodsDo: [:method |		"Interface methods are all abstract public."
					(inheritedMethodMembers
						at: (self methodSignatureFor: method selector)
						ifAbsentPut: [OrderedCollection new: 3])
							add: method]]]]
*/
private void computeMethods() {
	MethodBinding[] methods = type.methods();
	int size = methods.length;
	this.currentMethods = new HashtableOfObject(size == 0 ? 1 : size); // maps method selectors to an array of methods... must search to match paramaters & return type
	for (int m = size; --m >= 0;) {
		MethodBinding method = methods[m];
		if (!(method.isConstructor() || method.isDefaultAbstract())) { // keep all methods which are NOT constructors or default abstract
			MethodBinding[] existingMethods = (MethodBinding[]) this.currentMethods.get(method.selector);
			if (existingMethods == null)
				existingMethods = new MethodBinding[1];
			else
				System.arraycopy(existingMethods, 0,
					(existingMethods = new MethodBinding[existingMethods.length + 1]), 0, existingMethods.length - 1);
			existingMethods[existingMethods.length - 1] = method;
			this.currentMethods.put(method.selector, existingMethods);
		}
	}
}
private ReferenceBinding errorException() {
	if (errorException == null)
		this.errorException = this.type.scope.getJavaLangError();
	return errorException;
}
private boolean isAsVisible(MethodBinding newMethod, MethodBinding inheritedMethod) {
	if (inheritedMethod.modifiers == newMethod.modifiers) return true;

	if (newMethod.isPublic()) return true;		// Covers everything
	if (inheritedMethod.isPublic()) return false;

	if (newMethod.isProtected()) return true;
	if (inheritedMethod.isProtected()) return false;

	return !newMethod.isPrivate();		// The inheritedMethod cannot be private since it would not be visible
}
private boolean isSameClassOrSubclassOf(ReferenceBinding testClass, ReferenceBinding superclass) {
	do {
		if (testClass == superclass) return true;
	} while ((testClass = testClass.superclass()) != null);
	return false;
}
private boolean mustImplementAbstractMethod(MethodBinding abstractMethod) {
	// if the type's superclass is an abstract class, then all abstract methods must be implemented
	// otherwise, skip it if the type's superclass must implement any of the inherited methods
	ReferenceBinding superclass = this.type.superclass();
	ReferenceBinding declaringClass = abstractMethod.declaringClass;
	if (declaringClass.isClass()) {
		while (superclass.isAbstract() && superclass != declaringClass)
			superclass = superclass.superclass(); // find the first concrete superclass or the abstract declaringClass
	} else {
		if (this.type.implementsInterface(declaringClass, false))
			return !this.type.isAbstract();
		while (superclass.isAbstract() && !superclass.implementsInterface(declaringClass, false))
			superclass = superclass.superclass(); // find the first concrete superclass or the superclass which implements the interface
	}
	return superclass.isAbstract();		// if it is a concrete class then we have already reported problem against it
}
private ProblemReporter problemReporter() {
	return this.type.scope.problemReporter();
}
private ProblemReporter problemReporter(MethodBinding currentMethod) {
	ProblemReporter reporter = problemReporter();
	if (currentMethod.declaringClass == type)	// only report against the currentMethod if its implemented by the type
		reporter.referenceContext = currentMethod.sourceMethod();
	return reporter;
}
private ReferenceBinding runtimeException() {
	if (runtimeException == null)
		this.runtimeException = this.type.scope.getJavaLangRuntimeException();
	return runtimeException;
}
public void verify(SourceTypeBinding type) {
	this.type = type;
	this.computeMethods();
	this.computeInheritedMethods();
	this.checkMethods();
}
private void zzFieldProblems() {
}
/*
Binding creation is responsible for reporting all problems with fields:
	- all modifier problems (duplicates & multiple visibility modifiers + incompatible combinations - final/volatile)
		- plus invalid modifiers given the context (the verifier did not do this before)
		- include initializers in the modifier checks even though bindings are not created
	- collisions... 2 fields with same name
	- interfaces cannot define initializers
	- nested types cannot define static fields
	- with the type of the field:
		- void is not a valid type (or for an array)
		- an ambiguous, invisible or missing type

verifyFields

	| toSearch |
	(toSearch := newClass fields) notEmpty ifTrue: [
		newClass fromJavaClassFile
			ifTrue: [
				toSearch do: [:field |
					field isJavaInitializer ifFalse: [
						self verifyFieldType: field]]]
			ifFalse: [
				toSearch do: [:field |
					field isJavaInitializer
						ifTrue: [self verifyFieldInitializer: field]
						ifFalse: [self verifyField: field]]]]

verifyFieldInitializer: field

	type isJavaInterface
		ifTrue: [
			problemSummary
				reportVerificationProblem: #InterfacesCannotHaveInitializers
				args: #()
				severity: nil
				forField: field]
		ifFalse: [
			field isJavaStatic
				ifTrue: [
					field modifiers == AccStatic ifFalse: [
						problemSummary
							reportVerificationProblem: #IllegalModifierForStaticInitializer
							args: #()
							severity: nil
							forField: field]]
				ifFalse: [
					field modifiers == 0 ifFalse: [
						problemSummary
							reportVerificationProblem: #IllegalModifierForInitializer
							args: #()
							severity: nil
							forField: field]]]

verifyField: field

	(field basicModifiers anyMask: AccAlternateModifierProblem | AccModifierProblem) ifTrue: [
		self reportModifierProblemsOnField: field].

	field isJavaStatic ifTrue: [
		type isJavaStatic ifFalse: [
			(type isJavaNestedType and: [type isJavaClass]) ifTrue: [
				problemSummary
					reportVerificationProblem: #NestedClassCannotHaveStaticField
					args: #()
					severity: nil
					forField: field]]].

	self verifyFieldType: field

verifyFieldType: field

	| descriptor fieldType |
	"8.3 (Class) 9.3 (Interface)"
	"Optimize the base type case"
	field typeIsBaseType
		ifTrue: [
			field typeName = 'V' ifTrue: [  "$NON-NLS$"
				problemSummary
					reportVerificationProblem: #IllegalTypeForField
					args: (Array with: JavaVoid)
					severity: nil
					forField: field]]
		ifFalse: [
			descriptor := field asDescriptorIn: nameEnvironment.
			(fieldType := descriptor type) isValidDescriptor
				ifTrue: [
					(fieldType isArrayType and: [fieldType leafComponentType isVoidType]) ifTrue: [
						problemSummary
							reportVerificationProblem: #InvalidArrayType
							args: (Array with: fieldType javaReadableName)
							severity: nil
							forField: field]]
				ifFalse: [
					problemSummary
						reportVerificationProblem: #UnboundTypeForField
						args: (Array with: fieldType javaReadableName with: fieldType leafComponentType)
						severity: nil
						forField: field]].

reportModifierProblemsOnField: field

	(field basicModifiers anyMask: AccAlternateModifierProblem) ifTrue: [
		(field basicModifiers anyMask: AccModifierProblem)
			ifTrue: [
				^problemSummary
					reportVerificationProblem: #OnlyOneVisibilityModifierAllowed
					args: #()
					severity: ErrorInfo::ConflictingModifier
					forField: field]
			ifFalse: [
				^problemSummary
					reportVerificationProblem: #DuplicateModifier
					args: #()
					severity: ErrorInfo::ConflictingModifier
					forField: field]].

	type isJavaInterface ifTrue: [
		^problemSummary
			reportVerificationProblem: #IllegalModifierForInterfaceField
			args: #()
			severity: nil
			forField: field].

	(field basicModifiers allMask: AccFinal |  AccVolatile) ifTrue: [
		^problemSummary
			reportVerificationProblem: #IllegalModifierCombinationFinalVolatile
			args: #()
			severity: nil
			forField: field].

	^problemSummary
		reportVerificationProblem: #IllegalModifierForField
		args: #()
		severity: nil
		forField: field

void reportModifierProblems(FieldBinding field) {
	if (field.isFinal() && field.isVolatile())
		this.problemReporter.illegalModifierCombinationFinalVolatile(field);

	// Should be able to detect all 3 problems NOT just 1
	if ((type.modifiers() & Modifiers.AccAlternateModifierProblem) == 0) {
		if (this.type.isInterface())
			this.problemReporter.illegalModifierForInterfaceField(field);
		else
			this.problemReporter.illegalModifier(field);
	} else {
		if ((field.modifiers & Modifiers.AccModifierProblem) != 0)
			this.problemReporter.onlyOneVisibilityModifierAllowed(field);
		else
			this.problemReporter.duplicateModifier(field);
	}
}
*/
private void zzImportProblems() {
}
/*
Binding creation is responsible for reporting all problems with imports:
	- on demand imports which refer to missing packages
	- with single type imports:
		- resolves to an ambiguous, invisible or missing type
		- conflicts with the type's source name
		- has the same simple name as another import

Note: VAJ ignored duplicate imports (only one was kept)

verifyImports

	| importsBySimpleName nameEnvClass imports cl first |
	importsBySimpleName := LookupTable new.
	nameEnvClass := nameEnvironment class.

	"7.5.2"
	type imports do: [:import |
		import isOnDemand
			ifTrue: [
				(nameEnvClass doesPackageExistNamed: import javaPackageName) ifFalse: [
					(nameEnvClass findJavaClassNamedFrom: import javaPackageName) == nil ifTrue: [
						problemSummary
							reportVerificationProblem: #OnDemandImportRefersToMissingPackage
							args: (Array with: import asString)
							severity: ErrorInfo::ImportVerification
							forType: type]]]
			ifFalse: [
				(imports := importsBySimpleName at: import javaSimpleName ifAbsent: []) == nil
					ifTrue: [
						importsBySimpleName at: import javaSimpleName put: (Array with: import)]
					ifFalse: [
						(imports includes: import) ifFalse: [
							importsBySimpleName at: import javaSimpleName put: imports, (Array with: import)]].

				"Ignore any imports which are simple names - we will treat these as no-ops."

				import javaPackageName notEmpty ifTrue: [
					cl := nameEnvClass findJavaClassNamedFrom: import asString.

					(cl ~~ nil and: [cl isJavaPublic or: [cl controller symbol == type controller symbol]]) ifFalse: [
						problemSummary
							reportVerificationProblem: #SingleTypeImportRefersToInvisibleType
							args: (Array with: import asString)
							severity: ErrorInfo::ImportVerification
							forType: type]]]].

	importsBySimpleName notEmpty ifTrue: [
		importsBySimpleName keysAndValuesDo: [:simpleName :matching |
			matching size == 1
				ifTrue: [
					simpleName = type sourceName ifTrue: [
						matching first javaReadableName = type javaReadableName ifFalse: [
							problemSummary
								reportVerificationProblem: #SingleTypeImportConflictsWithType
								args: #()
								severity: nil
								forType: type]]]
				ifFalse: [
					problemSummary
						reportVerificationProblem: #SingleTypeImportsHaveSameSimpleName
						args: (Array with: simpleName)
						severity: nil
						forType: type]]]
*/
private void zzTypeProblems() {
}
}
