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
import org.eclipse.jdt.internal.compiler.ast.Clinit;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.impl.ReferenceContext;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.eclipse.jdt.internal.compiler.util.CharOperation;
import org.eclipse.jdt.internal.compiler.util.HashtableOfObject;

import java.util.*;

/**
 * AspectJ - added many hooks
 */
public class ClassScope extends Scope {
	public TypeDeclaration referenceContext;
	
	public ClassScope(Scope parent, TypeDeclaration context) {
		super(CLASS_SCOPE, parent);
		this.referenceContext = context;
	}
	
	void buildAnonymousTypeBinding(SourceTypeBinding enclosingType, ReferenceBinding supertype) {
		
		LocalTypeBinding anonymousType = buildLocalType(enclosingType, enclosingType.fPackage);

		SourceTypeBinding sourceType = referenceContext.binding;
		if (supertype.isInterface()) {
			sourceType.superclass = getJavaLangObject();
			sourceType.superInterfaces = new ReferenceBinding[] { supertype };
		} else {
			sourceType.superclass = supertype;
			sourceType.superInterfaces = TypeBinding.NoSuperInterfaces;
		}
		connectMemberTypes();
		buildFieldsAndMethods();
		anonymousType.faultInTypesForFieldsAndMethods();
		sourceType.verifyMethods(environment().methodVerifier());
	}
	
	private void buildFields() {
		if (referenceContext.fields == null) {
			referenceContext.binding.fields = NoFields;
			return;
		}
		// count the number of fields vs. initializers
		FieldDeclaration[] fields = referenceContext.fields;
		int size = fields.length;
		int count = 0;
		for (int i = 0; i < size; i++)
			if (fields[i].isField())
				count++;

		// iterate the field declarations to create the bindings, lose all duplicates
		FieldBinding[] fieldBindings = new FieldBinding[count];
		HashtableOfObject knownFieldNames = new HashtableOfObject(count);
		boolean duplicate = false;
		count = 0;
		for (int i = 0; i < size; i++) {
			FieldDeclaration field = fields[i];
			if (!field.isField()) {
				if (referenceContext.binding.isInterface())
					problemReporter().interfaceCannotHaveInitializers(referenceContext.binding, field);
			} else {
				FieldBinding fieldBinding = new FieldBinding(field, null, referenceContext.binding);
				// field's type will be resolved when needed for top level types
				checkAndSetModifiersForField(fieldBinding, field);

				if (knownFieldNames.containsKey(field.name)) {
					duplicate = true;
					FieldBinding previousBinding = (FieldBinding) knownFieldNames.get(field.name);
					if (previousBinding != null) {
						for (int f = 0; f < i; f++) {
							FieldDeclaration previousField = fields[f];
							if (previousField.binding == previousBinding) {
								problemReporter().duplicateFieldInType(referenceContext.binding, previousField);
								previousField.binding = null;
								break;
							}
						}
					}
					knownFieldNames.put(field.name, null); // ensure that the duplicate field is found & removed
					problemReporter().duplicateFieldInType(referenceContext.binding, field);
					field.binding = null;
				} else {
					knownFieldNames.put(field.name, fieldBinding);
					// remember that we have seen a field with this name
					if (fieldBinding != null)
						fieldBindings[count++] = fieldBinding;
				}
			}
		}
		// remove duplicate fields
		if (duplicate) {
			FieldBinding[] newFieldBindings = new FieldBinding[knownFieldNames.size() - 1];
			// we know we'll be removing at least 1 duplicate name
			size = count;
			count = 0;
			for (int i = 0; i < size; i++) {
				FieldBinding fieldBinding = fieldBindings[i];
				if (knownFieldNames.get(fieldBinding.name) != null)
					newFieldBindings[count++] = fieldBinding;
			}
			fieldBindings = newFieldBindings;
		}

		if (count != fieldBindings.length)
			System.arraycopy(fieldBindings, 0, fieldBindings = new FieldBinding[count], 0, count);
		for (int i = 0; i < count; i++)
			fieldBindings[i].id = i;
		referenceContext.binding.fields = fieldBindings;
	}
	
	void buildFieldsAndMethods() {
		postParse();
		
		buildFields();
		buildMethods();

		SourceTypeBinding sourceType = referenceContext.binding;
		if (sourceType.isMemberType() && !sourceType.isLocalType())
			 ((MemberTypeBinding) sourceType).checkSyntheticArgsAndFields();

		ReferenceBinding[] memberTypes = sourceType.memberTypes;
		for (int i = 0, length = memberTypes.length; i < length; i++)
			 ((SourceTypeBinding) memberTypes[i]).scope.buildFieldsAndMethods();
	}

	// AspectJ - hook
	private void postParse() {
		TypeDeclaration typeDec = referenceContext;
		AbstractMethodDeclaration[] methods = typeDec.methods;
		if (methods == null) return;
		for (int i=0, len=methods.length; i < len; i++) {
			methods[i].postParse(typeDec);
		}
	}
	
	
	private LocalTypeBinding buildLocalType(
		SourceTypeBinding enclosingType,
		PackageBinding packageBinding) {
		referenceContext.scope = this;
		referenceContext.staticInitializerScope = new MethodScope(this, referenceContext, true);
		referenceContext.initializerScope = new MethodScope(this, referenceContext, false);

		// build the binding or the local type
		LocalTypeBinding localType = new LocalTypeBinding(this, enclosingType);
		referenceContext.binding = localType;
		checkAndSetModifiers();

		// Look at member types
		ReferenceBinding[] memberTypeBindings = NoMemberTypes;
		if (referenceContext.memberTypes != null) {
			int size = referenceContext.memberTypes.length;
			memberTypeBindings = new ReferenceBinding[size];
			int count = 0;
			nextMember : for (int i = 0; i < size; i++) {
				TypeDeclaration memberContext = referenceContext.memberTypes[i];
				if (memberContext.isInterface()) {
					problemReporter().nestedClassCannotDeclareInterface(memberContext);
					continue nextMember;
				}
				ReferenceBinding type = localType;
				// check that the member does not conflict with an enclosing type
				do {
					if (CharOperation.equals(type.sourceName, memberContext.name)) {
						problemReporter().hidingEnclosingType(memberContext);
						continue nextMember;
					}
					type = type.enclosingType();
				} while (type != null);
				// check the member type does not conflict with another sibling member type
				for (int j = 0; j < i; j++) {
					if (CharOperation.equals(referenceContext.memberTypes[j].name, memberContext.name)) {
						problemReporter().duplicateNestedType(memberContext);
						continue nextMember;
					}
				}

				ClassScope memberScope = new ClassScope(this, referenceContext.memberTypes[i]);
				LocalTypeBinding memberBinding = memberScope.buildLocalType(localType, packageBinding);
				memberBinding.setAsMemberType();
				memberTypeBindings[count++] = memberBinding;
			}
			if (count != size)
				System.arraycopy(memberTypeBindings, 0, memberTypeBindings = new ReferenceBinding[count], 0, count);
		}
		localType.memberTypes = memberTypeBindings;
		return localType;
	}
	
	void buildLocalTypeBinding(SourceTypeBinding enclosingType) {

		LocalTypeBinding localType = buildLocalType(enclosingType, enclosingType.fPackage);
		connectTypeHierarchy();
		buildFieldsAndMethods();
		localType.faultInTypesForFieldsAndMethods();

		referenceContext.binding.verifyMethods(environment().methodVerifier());
	}
	
	private void buildMethods() {
		if (referenceContext.methods == null) {
			referenceContext.binding.methods = NoMethods;
			return;
		}

		// iterate the method declarations to create the bindings
		AbstractMethodDeclaration[] methods = referenceContext.methods;
		int size = methods.length;
		int clinitIndex = -1;
		for (int i = 0; i < size; i++) {
			if (methods[i] instanceof Clinit) {
				clinitIndex = i;
				break;
			}
		}
		MethodBinding[] methodBindings = new MethodBinding[clinitIndex == -1 ? size : size - 1];

		int count = 0;
		for (int i = 0; i < size; i++) {
			if (i != clinitIndex) {
				MethodScope scope = new MethodScope(this, methods[i], false);
				MethodBinding methodBinding = scope.createMethod(methods[i]);
				if (methodBinding != null) // is null if binding could not be created
					methodBindings[count++] = methodBinding;
			}
		}
		if (count != methodBindings.length)
			System.arraycopy(methodBindings, 0, methodBindings = new MethodBinding[count], 0, count);

		referenceContext.binding.methods = methodBindings;
		referenceContext.binding.modifiers |= AccUnresolved; // until methods() is sent
	}
	SourceTypeBinding buildType(SourceTypeBinding enclosingType, PackageBinding packageBinding) {
		// provide the typeDeclaration with needed scopes
		referenceContext.scope = this;
		referenceContext.staticInitializerScope = new MethodScope(this, referenceContext, true);
		referenceContext.initializerScope = new MethodScope(this, referenceContext, false);

		if (enclosingType == null) {
			char[][] className = CharOperation.arrayConcat(packageBinding.compoundName, referenceContext.name);
			referenceContext.binding = new SourceTypeBinding(className, packageBinding, this);
		} else {
			char[][] className = CharOperation.deepCopy(enclosingType.compoundName);
			className[className.length - 1] =
				CharOperation.concat(className[className.length - 1], referenceContext.name, '$');
			referenceContext.binding = new MemberTypeBinding(className, this, enclosingType);
		}

		SourceTypeBinding sourceType = referenceContext.binding;
		sourceType.fPackage.addType(sourceType);
		checkAndSetModifiers();

		// Look at member types
		ReferenceBinding[] memberTypeBindings = NoMemberTypes;
		if (referenceContext.memberTypes != null) {
			int size = referenceContext.memberTypes.length;
			memberTypeBindings = new ReferenceBinding[size];
			int count = 0;
			nextMember : for (int i = 0; i < size; i++) {
				TypeDeclaration memberContext = referenceContext.memberTypes[i];
				if (memberContext.isInterface()
					&& sourceType.isNestedType()
					&& sourceType.isClass()
					&& !sourceType.isStatic()) {
					problemReporter().nestedClassCannotDeclareInterface(memberContext);
					continue nextMember;
				}
				ReferenceBinding type = sourceType;
				// check that the member does not conflict with an enclosing type
				do {
					if (CharOperation.equals(type.sourceName, memberContext.name)) {
						problemReporter().hidingEnclosingType(memberContext);
						continue nextMember;
					}
					type = type.enclosingType();
				} while (type != null);
				// check that the member type does not conflict with another sibling member type
				for (int j = 0; j < i; j++) {
					if (CharOperation.equals(referenceContext.memberTypes[j].name, memberContext.name)) {
						problemReporter().duplicateNestedType(memberContext);
						continue nextMember;
					}
				}

				ClassScope memberScope = new ClassScope(this, memberContext);
				memberTypeBindings[count++] = memberScope.buildType(sourceType, packageBinding);
			}
			if (count != size)
				System.arraycopy(memberTypeBindings, 0, memberTypeBindings = new ReferenceBinding[count], 0, count);
		}
		sourceType.memberTypes = memberTypeBindings;
		return sourceType;
	}
	
	private void checkAndSetModifiers() {
		SourceTypeBinding sourceType = referenceContext.binding;
		int modifiers = sourceType.modifiers;
		if ((modifiers & AccAlternateModifierProblem) != 0)
			problemReporter().duplicateModifierForType(sourceType);

		ReferenceBinding enclosingType = sourceType.enclosingType();
		boolean isMemberType = sourceType.isMemberType();
		
		if (isMemberType) {
			// checks for member types before local types to catch local members
			if (enclosingType.isStrictfp())
				modifiers |= AccStrictfp;
			if (enclosingType.isDeprecated())
				modifiers |= AccDeprecatedImplicitly;
			if (enclosingType.isInterface())
				modifiers |= AccPublic;
		} else if (sourceType.isLocalType()) {
			if (sourceType.isAnonymousType())
				modifiers |= AccFinal;
			ReferenceContext refContext = methodScope().referenceContext;
			if (refContext instanceof TypeDeclaration) {
				ReferenceBinding type = ((TypeDeclaration) refContext).binding;
				if (type.isStrictfp())
					modifiers |= AccStrictfp;
				if (type.isDeprecated())
					modifiers |= AccDeprecatedImplicitly;
			} else {
				MethodBinding method = ((AbstractMethodDeclaration) refContext).binding;
				if (method != null){
					if (method.isStrictfp())
						modifiers |= AccStrictfp;
					if (method.isDeprecated())
						modifiers |= AccDeprecatedImplicitly;
				}
			}
		}
		// after this point, tests on the 16 bits reserved.
		int realModifiers = modifiers & AccJustFlag;

		if ((realModifiers & AccInterface) != 0) {
			// detect abnormal cases for interfaces
			if (isMemberType) {
				int unexpectedModifiers =
					~(AccPublic | AccPrivate | AccProtected | AccStatic | AccAbstract | AccInterface | AccStrictfp);
				if ((realModifiers & unexpectedModifiers) != 0)
					problemReporter().illegalModifierForMemberInterface(sourceType);
				/*
				} else if (sourceType.isLocalType()) { //interfaces cannot be defined inside a method
					int unexpectedModifiers = ~(AccAbstract | AccInterface | AccStrictfp);
					if ((realModifiers & unexpectedModifiers) != 0)
						problemReporter().illegalModifierForLocalInterface(sourceType);
				*/
			} else {
				int unexpectedModifiers = ~(AccPublic | AccAbstract | AccInterface | AccStrictfp);
				if ((realModifiers & unexpectedModifiers) != 0)
					problemReporter().illegalModifierForInterface(sourceType);
			}
			modifiers |= AccAbstract;
		} else {
			// detect abnormal cases for types
			if (isMemberType) { // includes member types defined inside local types
				int unexpectedModifiers =
					~(AccPublic | AccPrivate | AccProtected | AccStatic | AccAbstract | AccFinal | AccStrictfp);
				if ((realModifiers & unexpectedModifiers) != 0)
					problemReporter().illegalModifierForMemberClass(sourceType);
			} else if (sourceType.isLocalType()) {
				int unexpectedModifiers = ~(AccAbstract | AccFinal | AccStrictfp);
				if ((realModifiers & unexpectedModifiers) != 0)
					problemReporter().illegalModifierForLocalClass(sourceType);
			} else {
				int unexpectedModifiers = ~(AccPublic | AccAbstract | AccFinal | AccStrictfp);
				if ((realModifiers & unexpectedModifiers) != 0)
					problemReporter().illegalModifierForClass(sourceType);
			}

			// check that Final and Abstract are not set together
			if ((realModifiers & (AccFinal | AccAbstract)) == (AccFinal | AccAbstract))
				problemReporter().illegalModifierCombinationFinalAbstractForClass(sourceType);
		}

		if (isMemberType) {
			// test visibility modifiers inconsistency, isolate the accessors bits
			if (enclosingType.isInterface()) {
				if ((realModifiers & (AccProtected | AccPrivate)) != 0) {
					problemReporter().illegalVisibilityModifierForInterfaceMemberType(sourceType);

					// need to keep the less restrictive
					if ((realModifiers & AccProtected) != 0)
						modifiers ^= AccProtected;
					if ((realModifiers & AccPrivate) != 0)
						modifiers ^= AccPrivate;
				}
			} else {
				int accessorBits = realModifiers & (AccPublic | AccProtected | AccPrivate);
				if ((accessorBits & (accessorBits - 1)) > 1) {
					problemReporter().illegalVisibilityModifierCombinationForMemberType(sourceType);

					// need to keep the less restrictive
					if ((accessorBits & AccPublic) != 0) {
						if ((accessorBits & AccProtected) != 0)
							modifiers ^= AccProtected;
						if ((accessorBits & AccPrivate) != 0)
							modifiers ^= AccPrivate;
					}
					if ((accessorBits & AccProtected) != 0)
						if ((accessorBits & AccPrivate) != 0)
							modifiers ^= AccPrivate;
				}
			}

			// static modifier test
			if ((realModifiers & AccStatic) == 0) {
				if (enclosingType.isInterface())
					modifiers |= AccStatic;
			} else {
				if (!enclosingType.isStatic())
					// error the enclosing type of a static field must be static or a top-level type
					problemReporter().illegalStaticModifierForMemberType(sourceType);
			}
		}

		sourceType.modifiers = modifiers;
	}
	
	/* This method checks the modifiers of a field.
	*
	* 9.3 & 8.3
	* Need to integrate the check for the final modifiers for nested types
	*
	* Note : A scope is accessible by : fieldBinding.declaringClass.scope
	*/
	private void checkAndSetModifiersForField(FieldBinding fieldBinding, FieldDeclaration fieldDecl) {
		int modifiers = fieldBinding.modifiers;
		if ((modifiers & AccAlternateModifierProblem) != 0)
			problemReporter().duplicateModifierForField(fieldBinding.declaringClass, fieldDecl);

		if (fieldBinding.declaringClass.isInterface()) {
			int expectedValue = AccPublic | AccStatic | AccFinal;
			// set the modifiers
			modifiers |= expectedValue;

			// and then check that they are the only ones
			if ((modifiers & AccJustFlag) != expectedValue)
				problemReporter().illegalModifierForInterfaceField(fieldBinding.declaringClass, fieldDecl);
			fieldBinding.modifiers = modifiers;
			return;
		}

		// after this point, tests on the 16 bits reserved.
		int realModifiers = modifiers & AccJustFlag;
		int unexpectedModifiers =
			~(AccPublic | AccPrivate | AccProtected | AccFinal | AccStatic | AccTransient | AccVolatile);
		if ((realModifiers & unexpectedModifiers) != 0)
			problemReporter().illegalModifierForField(fieldBinding.declaringClass, fieldDecl);

		int accessorBits = realModifiers & (AccPublic | AccProtected | AccPrivate);
		if ((accessorBits & (accessorBits - 1)) > 1) {
			problemReporter().illegalVisibilityModifierCombinationForField(
				fieldBinding.declaringClass,
				fieldDecl);

			// need to keep the less restrictive
			if ((accessorBits & AccPublic) != 0) {
				if ((accessorBits & AccProtected) != 0)
					modifiers ^= AccProtected;
				if ((accessorBits & AccPrivate) != 0)
					modifiers ^= AccPrivate;
			}
			if ((accessorBits & AccProtected) != 0)
				if ((accessorBits & AccPrivate) != 0)
					modifiers ^= AccPrivate;
		}

		if ((realModifiers & (AccFinal | AccVolatile)) == (AccFinal | AccVolatile))
			problemReporter().illegalModifierCombinationFinalVolatileForField(
				fieldBinding.declaringClass,
				fieldDecl);

		fieldBinding.modifiers = modifiers;
	}
	
	private void checkForInheritedMemberTypes(SourceTypeBinding sourceType) {
		// search up the hierarchy of the sourceType to see if any superType defines a member type
		// when no member types are defined, tag the sourceType & each superType with the HasNoMemberTypes bit
		ReferenceBinding currentType = sourceType;
		ReferenceBinding[][] interfacesToVisit = null;
		int lastPosition = -1;
		do {
			if ((currentType.tagBits & HasNoMemberTypes) != 0)
				break; // already know it has no inherited member types, can stop looking up
			if (currentType.memberTypes() != NoMemberTypes)
				return; // has member types
			ReferenceBinding[] itsInterfaces = currentType.superInterfaces();
			if (itsInterfaces != NoSuperInterfaces) {
				if (interfacesToVisit == null)
					interfacesToVisit = new ReferenceBinding[5][];
				if (++lastPosition == interfacesToVisit.length)
					System.arraycopy(
						interfacesToVisit,
						0,
						interfacesToVisit = new ReferenceBinding[lastPosition * 2][],
						0,
						lastPosition);
				interfacesToVisit[lastPosition] = itsInterfaces;
			}
		} while ((currentType = currentType.superclass()) != null);

		boolean hasMembers = false;
		if (interfacesToVisit != null) {
			done : for (int i = 0; i <= lastPosition; i++) {
				ReferenceBinding[] interfaces = interfacesToVisit[i];
				for (int j = 0, length = interfaces.length; j < length; j++) {
					ReferenceBinding anInterface = interfaces[j];
					if ((anInterface.tagBits & InterfaceVisited) == 0) { // if interface as not already been visited
						anInterface.tagBits |= InterfaceVisited;
						if ((anInterface.tagBits & HasNoMemberTypes) != 0)
							continue; // already know it has no inherited member types
						if (anInterface.memberTypes() != NoMemberTypes) {
							hasMembers = true;
							break done;
						}

						ReferenceBinding[] itsInterfaces = anInterface.superInterfaces();
						if (itsInterfaces != NoSuperInterfaces) {
							if (++lastPosition == interfacesToVisit.length)
								System.arraycopy(
									interfacesToVisit,
									0,
									interfacesToVisit = new ReferenceBinding[lastPosition * 2][],
									0,
									lastPosition);
							interfacesToVisit[lastPosition] = itsInterfaces;
						}
					}
				}
			}

			for (int i = 0; i <= lastPosition; i++) {
				ReferenceBinding[] interfaces = interfacesToVisit[i];
				for (int j = 0, length = interfaces.length; j < length; j++) {
					interfaces[j].tagBits &= ~InterfaceVisited;
					if (!hasMembers)
						interfaces[j].tagBits |= HasNoMemberTypes;
				}
			}
		}

		if (!hasMembers) {
			currentType = sourceType;
			do {
				currentType.tagBits |= HasNoMemberTypes;
			} while ((currentType = currentType.superclass()) != null);
		}
	}
	
	private void connectMemberTypes() {
		SourceTypeBinding sourceType = referenceContext.binding;
		if (sourceType.memberTypes != NoMemberTypes)
			for (int i = 0, size = sourceType.memberTypes.length; i < size; i++)
				 ((SourceTypeBinding) sourceType.memberTypes[i]).scope.connectTypeHierarchy();
	}
	/*
		Our current belief based on available JCK tests is:
			inherited member types are visible as a potential superclass.
			inherited interfaces are not visible when defining a superinterface.
	
		Error recovery story:
			ensure the superclass is set to java.lang.Object if a problem is detected
			resolving the superclass.
	
		Answer false if an error was reported against the sourceType.
	*/
	private boolean connectSuperclass() {
		SourceTypeBinding sourceType = referenceContext.binding;
		if (referenceContext.superclass == null) {
			if (isJavaLangObject(sourceType))
				return true;
			sourceType.superclass = getJavaLangObject();
			return !detectCycle(sourceType, sourceType.superclass, null);
			// ensure Object is initialized if it comes from a source file
		}
		ReferenceBinding superclass = findSupertype(referenceContext.superclass);
		if (superclass != null) { // is null if a cycle was detected cycle
			if (!superclass.isValidBinding()) {
				problemReporter().invalidSuperclass(sourceType, referenceContext.superclass, superclass);
			} else if (superclass.isInterface()) {
				problemReporter().superclassMustBeAClass(sourceType, referenceContext.superclass, superclass);
			} else if (superclass.isFinal()) {
				problemReporter().classExtendFinalClass(sourceType, referenceContext.superclass, superclass);
			} else if (isJavaLangObject(sourceType)) {
				// can only happen if Object extends another type... will never happen unless we're testing for it.
				sourceType.tagBits |= HierarchyHasProblems;
				sourceType.superclass = null;
				return true;
			} else {
				// only want to reach here when no errors are reported
				referenceContext.superclass.binding = superclass;
				sourceType.superclass = superclass;
				return true;
			}
		}
		sourceType.tagBits |= HierarchyHasProblems;
		if (!isJavaLangObject(sourceType)) {
			sourceType.superclass = getJavaLangObject();
			if ((sourceType.superclass.tagBits & BeginHierarchyCheck) == 0)
				detectCycle(sourceType, sourceType.superclass, null);
			// ensure Object is initialized if it comes from a source file
		}
		return false; // reported some error against the source type
	}

	/*
		Our current belief based on available JCK 1.3 tests is:
			inherited member types are visible as a potential superclass.
			inherited interfaces are visible when defining a superinterface.
	
		Error recovery story:
			ensure the superinterfaces contain only valid visible interfaces.
	
		Answer false if an error was reported against the sourceType.
	*/
	private boolean connectSuperInterfaces() {
		SourceTypeBinding sourceType = referenceContext.binding;
		sourceType.superInterfaces = NoSuperInterfaces;
		if (referenceContext.superInterfaces == null)
			return true;

		boolean noProblems = true;
		int length = referenceContext.superInterfaces.length;
		ReferenceBinding[] interfaceBindings = new ReferenceBinding[length];
		int count = 0;
		nextInterface : for (int i = 0; i < length; i++) {
			ReferenceBinding superInterface = findSupertype(referenceContext.superInterfaces[i]);
			if (superInterface == null) { // detected cycle
				noProblems = false;
				continue nextInterface;
			}
			if (!superInterface.isValidBinding()) {
				problemReporter().invalidSuperinterface(
					sourceType,
					referenceContext.superInterfaces[i],
					superInterface);
				sourceType.tagBits |= HierarchyHasProblems;
				noProblems = false;
				continue nextInterface;
			}
			// Check for a duplicate interface once the name is resolved, otherwise we may be confused (ie : a.b.I and c.d.I)
			for (int k = 0; k < count; k++) {
				if (interfaceBindings[k] == superInterface) {
					// should this be treated as a warning?
					problemReporter().duplicateSuperinterface(sourceType, referenceContext, superInterface);
					continue nextInterface;
				}
			}
			if (superInterface.isClass()) {
				problemReporter().superinterfaceMustBeAnInterface(sourceType, referenceContext, superInterface);
				sourceType.tagBits |= HierarchyHasProblems;
				noProblems = false;
				continue nextInterface;
			}
			referenceContext.superInterfaces[i].binding = superInterface;
			// only want to reach here when no errors are reported
			interfaceBindings[count++] = superInterface;
		}
		// hold onto all correctly resolved superinterfaces
		if (count > 0) {
			if (count != length)
				System.arraycopy(interfaceBindings, 0, interfaceBindings = new ReferenceBinding[count], 0, count);
			sourceType.superInterfaces = interfaceBindings;
		}
		return noProblems;
	}
	
	void connectTypeHierarchy() {
		SourceTypeBinding sourceType = referenceContext.binding;
		if ((sourceType.tagBits & BeginHierarchyCheck) == 0) {
			boolean noProblems = true;
			sourceType.tagBits |= BeginHierarchyCheck;
			if (sourceType.isClass())
				noProblems &= connectSuperclass();
			noProblems &= connectSuperInterfaces();
			sourceType.tagBits |= EndHierarchyCheck;
			if (noProblems && sourceType.isHierarchyInconsistent())
				problemReporter().hierarchyHasProblems(sourceType);
		}
		connectMemberTypes();
		checkForInheritedMemberTypes(sourceType);
	}
	
	private void connectTypeHierarchyWithoutMembers() {
		// must ensure the imports are resolved
		if (parent instanceof CompilationUnitScope) {
			if (((CompilationUnitScope) parent).imports == null)
				 ((CompilationUnitScope) parent).checkAndSetImports();
		} else if (parent instanceof ClassScope) {
			// ensure that the enclosing type has already been checked
			 ((ClassScope) parent).connectTypeHierarchyWithoutMembers();
		}

		// double check that the hierarchy search has not already begun...
		SourceTypeBinding sourceType = referenceContext.binding;
		if ((sourceType.tagBits & BeginHierarchyCheck) != 0)
			return;

		boolean noProblems = true;
		sourceType.tagBits |= BeginHierarchyCheck;
		if (sourceType.isClass())
			noProblems &= connectSuperclass();
		noProblems &= connectSuperInterfaces();
		sourceType.tagBits |= EndHierarchyCheck;
		if (noProblems && sourceType.isHierarchyInconsistent())
			problemReporter().hierarchyHasProblems(sourceType);
	}
	
	// Answer whether a cycle was found between the sourceType & the superType
	private boolean detectCycle(
		SourceTypeBinding sourceType,
		ReferenceBinding superType,
		TypeReference reference) {
		if (sourceType == superType) {
			problemReporter().hierarchyCircularity(sourceType, superType, reference);
			sourceType.tagBits |= HierarchyHasProblems;
			return true;
		}

		if (superType.isBinaryBinding()) {
			// force its superclass & superinterfaces to be found... 2 possibilities exist - the source type is included in the hierarchy of:
			//		- a binary type... this case MUST be caught & reported here
			//		- another source type... this case is reported against the other source type
			boolean hasCycle = false;
			if (superType.superclass() != null) {
				if (sourceType == superType.superclass()) {
					problemReporter().hierarchyCircularity(sourceType, superType, reference);
					sourceType.tagBits |= HierarchyHasProblems;
					superType.tagBits |= HierarchyHasProblems;
					return true;
				}
				hasCycle |= detectCycle(sourceType, superType.superclass(), reference);
				if ((superType.superclass().tagBits & HierarchyHasProblems) != 0) {
					sourceType.tagBits |= HierarchyHasProblems;
					superType.tagBits |= HierarchyHasProblems; // propagate down the hierarchy
				}
			}

			ReferenceBinding[] itsInterfaces = superType.superInterfaces();
			if (itsInterfaces != NoSuperInterfaces) {
				for (int i = 0, length = itsInterfaces.length; i < length; i++) {
					ReferenceBinding anInterface = itsInterfaces[i];
					if (sourceType == anInterface) {
						problemReporter().hierarchyCircularity(sourceType, superType, reference);
						sourceType.tagBits |= HierarchyHasProblems;
						superType.tagBits |= HierarchyHasProblems;
						return true;
					}
					hasCycle |= detectCycle(sourceType, anInterface, reference);
					if ((anInterface.tagBits & HierarchyHasProblems) != 0) {
						sourceType.tagBits |= HierarchyHasProblems;
						superType.tagBits |= HierarchyHasProblems;
					}
				}
			}
			return hasCycle;
		}

		if ((superType.tagBits & EndHierarchyCheck) == 0
			&& (superType.tagBits & BeginHierarchyCheck) != 0) {
			problemReporter().hierarchyCircularity(sourceType, superType, reference);
			sourceType.tagBits |= HierarchyHasProblems;
			superType.tagBits |= HierarchyHasProblems;
			return true;
		}
		if ((superType.tagBits & BeginHierarchyCheck) == 0)
			// ensure if this is a source superclass that it has already been checked
			 ((SourceTypeBinding) superType).scope.connectTypeHierarchyWithoutMembers();
		if ((superType.tagBits & HierarchyHasProblems) != 0)
			sourceType.tagBits |= HierarchyHasProblems;
		return false;
	}
	
	private ReferenceBinding findSupertype(TypeReference typeReference) {
		typeReference.aboutToResolve(this); // allows us to trap completion & selection nodes
		char[][] compoundName = typeReference.getTypeName();
		compilationUnitScope().recordQualifiedReference(compoundName);
		SourceTypeBinding sourceType = referenceContext.binding;
		int size = compoundName.length;
		int n = 1;
		ReferenceBinding superType;

		// resolve the first name of the compoundName
		if (CharOperation.equals(compoundName[0], sourceType.sourceName)) {
			superType = sourceType;
			// match against the sourceType even though nested members cannot be supertypes
		} else {
			Binding typeOrPackage = parent.getTypeOrPackage(compoundName[0], TYPE | PACKAGE);
			if (typeOrPackage == null || !typeOrPackage.isValidBinding())
				return new ProblemReferenceBinding(
					compoundName[0],
					typeOrPackage == null ? NotFound : typeOrPackage.problemId());

			boolean checkVisibility = false;
			for (; n < size; n++) {
				if (!(typeOrPackage instanceof PackageBinding))
					break;
				PackageBinding packageBinding = (PackageBinding) typeOrPackage;
				typeOrPackage = packageBinding.getTypeOrPackage(compoundName[n]);
				if (typeOrPackage == null || !typeOrPackage.isValidBinding())
					return new ProblemReferenceBinding(
						CharOperation.subarray(compoundName, 0, n + 1),
						typeOrPackage == null ? NotFound : typeOrPackage.problemId());
				checkVisibility = true;
			}

			// convert to a ReferenceBinding
			if (typeOrPackage instanceof PackageBinding) // error, the compoundName is a packageName
				return new ProblemReferenceBinding(CharOperation.subarray(compoundName, 0, n), NotFound);
			superType = (ReferenceBinding) typeOrPackage;
			compilationUnitScope().recordTypeReference(superType); // to record supertypes

			if (checkVisibility
				&& n == size) { // if we're finished and know the final supertype then check visibility
				if (!superType.canBeSeenBy(sourceType.fPackage))
					// its a toplevel type so just check package access
					return new ProblemReferenceBinding(CharOperation.subarray(compoundName, 0, n), superType, NotVisible);
			}
		}
		// at this point we know we have a type but we have to look for cycles
		while (true) {
			// must detect cycles & force connection up the hierarchy... also handle cycles with binary types.
			// must be guaranteed that the superType knows its entire hierarchy
			if (detectCycle(sourceType, superType, typeReference))
				return null; // cycle error was already reported

			if (n >= size)
				break;

			// retrieve the next member type
			char[] typeName = compoundName[n++];
			superType = findMemberType(typeName, superType);
			if (superType == null)
				return new ProblemReferenceBinding(CharOperation.subarray(compoundName, 0, n), NotFound);
			if (!superType.isValidBinding()) {
				superType.compoundName = CharOperation.subarray(compoundName, 0, n);
				return superType;
			}
		}
		return superType;
	}

	/* Answer the problem reporter to use for raising new problems.
	*
	* Note that as a side-effect, this updates the current reference context
	* (unit, type or method) in case the problem handler decides it is necessary
	* to abort.
	*/
	public ProblemReporter problemReporter() {
		MethodScope outerMethodScope;
		if ((outerMethodScope = outerMostMethodScope()) == null) {
			ProblemReporter problemReporter = referenceCompilationUnit().problemReporter;
			problemReporter.referenceContext = referenceContext;
			return problemReporter;
		} else {
			return outerMethodScope.problemReporter();
		}
	}

	/* Answer the reference type of this scope.
	*
	* i.e. the nearest enclosing type of this scope.
	*/
	public TypeDeclaration referenceType() {
		return referenceContext;
	}
	
	public String toString() {
		if (referenceContext != null)
			return "--- Class Scope ---\n\n"  //$NON-NLS-1$
			+referenceContext.binding.toString();
		else
			return "--- Class Scope ---\n\n Binding not initialized" ; //$NON-NLS-1$
	}
	
	// AspectJ - hook for subclasses to override
	public int addDepth() {
		return 1;
	}
	
	public SourceTypeBinding invocationType() {
		return referenceContext.binding;
	}

}