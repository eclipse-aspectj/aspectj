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

import org.eclipse.jdt.internal.compiler.ast.AstNode;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.eclipse.jdt.internal.compiler.util.CharOperation;
import org.eclipse.jdt.internal.compiler.util.ObjectVector;

// AspectJ - hooks, added distinction between enclosingSourceType and invocationType
public abstract class Scope
	implements
		BaseTypes,
		BindingIds,
		CompilerModifiers,
		ProblemReasons,
		TagBits,
		TypeConstants,
		TypeIds {

	public Scope parent;
	public int kind;

	public final static int BLOCK_SCOPE = 1;
	public final static int METHOD_SCOPE = 2;
	public final static int CLASS_SCOPE = 3;
	public final static int COMPILATION_UNIT_SCOPE = 4;
	protected Scope(int kind, Scope parent) {
		this.kind = kind;
		this.parent = parent;
	}

	public abstract ProblemReporter problemReporter();

	// Internal use only
	protected final boolean areParametersAssignable(TypeBinding[] parameters, TypeBinding[] arguments) {
		if (parameters == arguments)
			return true;

		int length = parameters.length;
		if (length != arguments.length)
			return false;

		for (int i = 0; i < length; i++)
			if (parameters[i] != arguments[i])
				if (!arguments[i].isCompatibleWith(parameters[i]))
					return false;
		return true;
	}

	/* Answer true if the left type can be assigned to right
	*/
	public static boolean areTypesCompatible(TypeBinding left, TypeBinding right) {
		return left.isCompatibleWith(right);
	}

	/* Answer an int describing the relationship between the given types.
	*
	* 		NotRelated 
	* 		EqualOrMoreSpecific : left is compatible with right
	* 		MoreGeneric : right is compatible with left
	*/
	public static int compareTypes(TypeBinding left, TypeBinding right) {
		if (areTypesCompatible(left, right))
			return EqualOrMoreSpecific;
		if (areTypesCompatible(right, left))
			return MoreGeneric;
		return NotRelated;
	}

	/* Answer an int describing the relationship between the given type and unchecked exceptions.
	*
	* 	NotRelated 
	* 	EqualOrMoreSpecific : type is known for sure to be an unchecked exception type
	* 	MoreGeneric : type is a supertype of an actual unchecked exception type
	*/
	public int compareUncheckedException(ReferenceBinding type) {
		int comparison = compareTypes(type, getJavaLangRuntimeException());
		if (comparison != 0) return comparison;
		return compareTypes(type, getJavaLangError());
	}

	public final CompilationUnitScope compilationUnitScope() {
		Scope lastScope = null;
		Scope scope = this;
		do {
			lastScope = scope;
			scope = scope.parent;
		} while (scope != null);
		return (CompilationUnitScope) lastScope;
	}

	public ArrayBinding createArray(TypeBinding type, int dimension) {
		if (type.isValidBinding())
			return environment().createArrayType(type, dimension);
		else
			return new ArrayBinding(type, dimension);
	}

	/* Answer the receiver's enclosing source type.
	 * 
	 * Except for inter-type declarations where the return value is the declaration type.
	*/
	public final SourceTypeBinding enclosingSourceType() {
		Scope scope = this;
		do {
			if (scope instanceof ClassScope)
				return ((ClassScope) scope).referenceContext.binding;
			scope = scope.parent;
		} while (scope != null);
		return null;
	}
	
	/**
	 * For Java scopes, the invocationType is always the same as the enclosingSourceType
	 * This distinction is important for AspectJ's inter-type declarations
	 * 
	 * For inter-type declarations, the invocationType is the lexically enclosing type.
	 */
	public SourceTypeBinding invocationType() {
		Scope scope = this;
		do {
			if (scope instanceof ClassScope)
				return ((ClassScope) scope).invocationType();
			scope = scope.parent;
		} while (scope != null);
		return null;
		//return enclosingSourceType();
	}
	
	public final LookupEnvironment environment() {
		Scope scope, unitScope = this;
		while ((scope = unitScope.parent) != null)
			unitScope = scope;
		return ((CompilationUnitScope) unitScope).environment;
	}

	// Internal use only
	public ReferenceBinding findDirectMemberType(char[] typeName, ReferenceBinding enclosingType) {
		if ((enclosingType.tagBits & HasNoMemberTypes) != 0)
			return null; // know it has no member types (nor inherited member types)

		SourceTypeBinding enclosingSourceType = enclosingSourceType();
		compilationUnitScope().recordReference(enclosingType.compoundName, typeName);
		ReferenceBinding memberType = enclosingType.getMemberType(typeName);
		if (memberType != null) {
			compilationUnitScope().recordTypeReference(memberType); // to record supertypes
			if (enclosingSourceType == null
				? memberType.canBeSeenBy(getCurrentPackage())
				: memberType.canBeSeenBy(enclosingType, enclosingSourceType))
				return memberType;
			else
				return new ProblemReferenceBinding(typeName, memberType, NotVisible);
		}
		return null;
	}

	// Internal use only
	public MethodBinding findExactMethod(
		ReferenceBinding receiverType,
		char[] selector,
		TypeBinding[] argumentTypes,
		InvocationSite invocationSite) {

		compilationUnitScope().recordTypeReference(receiverType);
		compilationUnitScope().recordTypeReferences(argumentTypes);
		MethodBinding exactMethod = receiverType.getExactMethod(selector, argumentTypes);
		if (exactMethod != null) {
			compilationUnitScope().recordTypeReferences(exactMethod.thrownExceptions);
			compilationUnitScope().recordTypeReference(exactMethod.declaringClass); // for inter-type decls
			if (receiverType.isInterface() || exactMethod.canBeSeenBy(receiverType, invocationSite, this)) {
				return exactMethod;
			} else {
				IPrivilegedHandler handler = findPrivilegedHandler(invocationType());
				if (handler != null) {
					//???System.err.println("privileged access: ");
					return invocationType().privilegedHandler.getPrivilegedAccessMethod(exactMethod, (AstNode)invocationSite);
				}
			}
		}
		return null;
	}
	
	public static final IPrivilegedHandler findPrivilegedHandler(ReferenceBinding type) {
		if (type == null) return null;
		if (type instanceof SourceTypeBinding) {
			if (((SourceTypeBinding)type).privilegedHandler != null) {
				return ((SourceTypeBinding)type).privilegedHandler;
			}
		}
		return findPrivilegedHandler(type.enclosingType());
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
	public FieldBinding findField(TypeBinding receiverType, char[] fieldName, InvocationSite invocationSite) {
		if (receiverType.isBaseType()) return null;
		if (receiverType.isArrayType()) {
			if (CharOperation.equals(fieldName, LENGTH))
				return ArrayBinding.LengthField;
			return null;
		}

		compilationUnitScope().recordTypeReference(receiverType);

		ReferenceBinding currentType = (ReferenceBinding) receiverType;
		if (!currentType.canBeSeenBy(this))
			return new ProblemFieldBinding(currentType, fieldName, NotVisible);
		// *** Need a new problem id - TypeNotVisible?

		FieldBinding field = currentType.getField(fieldName, invocationSite, this);
		if (field != null) {
			if (field.canBeSeenBy(currentType, invocationSite, this))
				return field;
			else
				return new ProblemFieldBinding(field.declaringClass, fieldName, NotVisible);
		}
		// collect all superinterfaces of receiverType until the field is found in a supertype
		ReferenceBinding[][] interfacesToVisit = null;
		int lastPosition = -1;
		FieldBinding visibleField = null;
		boolean keepLooking = true;
		boolean notVisible = false;
		// we could hold onto the not visible field for extra error reporting
		while (keepLooking) {
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
			if ((currentType = currentType.superclass()) == null)
				break;

			if ((field = currentType.getBestField(fieldName, invocationSite, this)) != null) {
				keepLooking = false;
				if (field.canBeSeenBy(receiverType, invocationSite, this)) {
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
					if ((anInterface.tagBits & InterfaceVisited) == 0) {
						// if interface as not already been visited
						anInterface.tagBits |= InterfaceVisited;
						if ((field = anInterface.getBestField(fieldName, invocationSite, this)) != null) {
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
			}

			// bit reinitialization
			for (int i = 0; i <= lastPosition; i++) {
				ReferenceBinding[] interfaces = interfacesToVisit[i];
				for (int j = 0, length = interfaces.length; j < length; j++)
					interfaces[j].tagBits &= ~InterfaceVisited;
			}
			if (ambiguous != null)
				return ambiguous;
		}

		if (visibleField != null)
			return visibleField;
		if (notVisible)
			return new ProblemFieldBinding(currentType, fieldName, NotVisible);
		return null;
	}

	// Internal use only
	public ReferenceBinding findMemberType(char[] typeName, ReferenceBinding enclosingType) {
		if ((enclosingType.tagBits & HasNoMemberTypes) != 0)
			return null; // know it has no member types (nor inherited member types)

		SourceTypeBinding enclosingSourceType = enclosingSourceType();
		PackageBinding currentPackage = getCurrentPackage();
		compilationUnitScope().recordReference(enclosingType.compoundName, typeName);
		ReferenceBinding memberType = enclosingType.getMemberType(typeName);
		if (memberType != null) {
			compilationUnitScope().recordTypeReference(memberType); // to record supertypes
			if (enclosingSourceType == null
				? memberType.canBeSeenBy(currentPackage)
				: memberType.canBeSeenBy(enclosingType, enclosingSourceType))
				return memberType;
			else
				return new ProblemReferenceBinding(typeName, memberType, NotVisible);
		}

		// collect all superinterfaces of receiverType until the memberType is found in a supertype
		ReferenceBinding currentType = enclosingType;
		ReferenceBinding[][] interfacesToVisit = null;
		int lastPosition = -1;
		ReferenceBinding visibleMemberType = null;
		boolean keepLooking = true;
		ReferenceBinding notVisible = null;
		// we could hold onto the not visible field for extra error reporting
		while (keepLooking) {
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
			if ((currentType = currentType.superclass()) == null)
				break;

			compilationUnitScope().recordReference(currentType.compoundName, typeName);
			if ((memberType = currentType.getMemberType(typeName)) != null) {
				compilationUnitScope().recordTypeReference(memberType); // to record supertypes
				keepLooking = false;
				if (enclosingSourceType == null
					? memberType.canBeSeenBy(currentPackage)
					: memberType.canBeSeenBy(enclosingType, enclosingSourceType)) {
						if (visibleMemberType == null)
							visibleMemberType = memberType;
						else
							return new ProblemReferenceBinding(typeName, Ambiguous);
				} else {
					notVisible = memberType;
				}
			}
		}
		// walk all visible interfaces to find ambiguous references
		if (interfacesToVisit != null) {
			ProblemReferenceBinding ambiguous = null;
			done : for (int i = 0; i <= lastPosition; i++) {
				ReferenceBinding[] interfaces = interfacesToVisit[i];
				for (int j = 0, length = interfaces.length; j < length; j++) {
					ReferenceBinding anInterface = interfaces[j];
					if ((anInterface.tagBits & InterfaceVisited) == 0) {
						// if interface as not already been visited
						anInterface.tagBits |= InterfaceVisited;
						compilationUnitScope().recordReference(anInterface.compoundName, typeName);
						if ((memberType = anInterface.getMemberType(typeName)) != null) {
							compilationUnitScope().recordTypeReference(memberType); // to record supertypes
							if (visibleMemberType == null) {
								visibleMemberType = memberType;
							} else {
								ambiguous = new ProblemReferenceBinding(typeName, Ambiguous);
								break done;
							}
						} else {
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
			}

			// bit reinitialization
			for (int i = 0; i <= lastPosition; i++) {
				ReferenceBinding[] interfaces = interfacesToVisit[i];
				for (int j = 0, length = interfaces.length; j < length; j++)
					interfaces[j].tagBits &= ~InterfaceVisited;
			}
			if (ambiguous != null)
				return ambiguous;
		}
		if (visibleMemberType != null)
			return visibleMemberType;
		if (notVisible != null)
			return new ProblemReferenceBinding(typeName, notVisible, NotVisible);
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

		compilationUnitScope().recordTypeReference(receiverType);
		compilationUnitScope().recordTypeReferences(argumentTypes);

		if (currentType.isInterface()) {
			MethodBinding[] currentMethods = currentType.getMethods(selector);
			int currentLength = currentMethods.length;
			if (currentLength == 1) {
				matchingMethod = currentMethods[0];
			} else if (currentLength > 1) {
				found.addAll(currentMethods);
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
				if (matchingMethod != null) {
					found.add(matchingMethod);
					matchingMethod = null;
				}
				found.addAll(currentMethods);
			}
			currentType = currentType.superclass();
		}

		int foundSize = found.size;
		if (foundSize == 0) {
			if (matchingMethod != null && areParametersAssignable(matchingMethod.parameters, argumentTypes))
				return matchingMethod;
			return findDefaultAbstractMethod(receiverType, selector, argumentTypes, invocationSite, classHierarchyStart, matchingMethod, found);
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
			compilationUnitScope().recordTypeReferences(candidates[0].thrownExceptions);
			return candidates[0]; // have not checked visibility
		}
		if (candidatesCount == 0) { // try to find a close match when the parameter order is wrong or missing some parameters
			MethodBinding interfaceMethod =
				findDefaultAbstractMethod(receiverType, selector, argumentTypes, invocationSite, classHierarchyStart, matchingMethod, found);
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
			if (methodBinding.canBeSeenBy(receiverType, invocationSite, this)) {
				if (visiblesCount != i) {
					candidates[i] = null;
					candidates[visiblesCount] = methodBinding;
				}
				visiblesCount++;
			}
		}
		if (visiblesCount == 1) {
			compilationUnitScope().recordTypeReferences(candidates[0].thrownExceptions);
			return candidates[0];
		}
		if (visiblesCount == 0) {
			MethodBinding interfaceMethod =
				findDefaultAbstractMethod(receiverType, selector, argumentTypes, invocationSite, classHierarchyStart, matchingMethod, found);
			if (interfaceMethod != null) return interfaceMethod;
			return new ProblemMethodBinding(candidates[0].selector, argumentTypes, candidates[0].declaringClass, NotVisible);
		}
		if (candidates[0].declaringClass.isClass()) {
			return mostSpecificClassMethodBinding(candidates, visiblesCount);
		} else {
			return mostSpecificInterfaceMethodBinding(candidates, visiblesCount);
		}
	}

	// abstract method lookup lookup (since maybe missing default abstract methods)
	public MethodBinding findDefaultAbstractMethod(
		ReferenceBinding receiverType, 
		char[] selector,
		TypeBinding[] argumentTypes,
		InvocationSite invocationSite,
		ReferenceBinding classHierarchyStart,
		MethodBinding matchingMethod,
		ObjectVector found) {

		int startFoundSize = found.size;
		ReferenceBinding currentType = classHierarchyStart;
		while (currentType != null) {
			matchingMethod = findMethodInSuperInterfaces(currentType, selector, found, matchingMethod);
			currentType = currentType.superclass();
		}
		int foundSize = found.size;
		if (foundSize == startFoundSize) return matchingMethod; // maybe null

		MethodBinding[] candidates = new MethodBinding[foundSize - startFoundSize];
		int candidatesCount = 0;
		// argument type compatibility check
		for (int i = startFoundSize; i < foundSize; i++) {
			MethodBinding methodBinding = (MethodBinding) found.elementAt(i);
			if (areParametersAssignable(methodBinding.parameters, argumentTypes))
				candidates[candidatesCount++] = methodBinding;
		}
		if (candidatesCount == 1) {
			compilationUnitScope().recordTypeReferences(candidates[0].thrownExceptions);
			return candidates[0]; 
		}
		if (candidatesCount == 0) { // try to find a close match when the parameter order is wrong or missing some parameters
			int argLength = argumentTypes.length;
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
		// no need to check for visibility - interface methods are public
		return mostSpecificInterfaceMethodBinding(candidates, candidatesCount);
	}

	public MethodBinding findMethodInSuperInterfaces(
		ReferenceBinding currentType,
		char[] selector,
		ObjectVector found,
		MethodBinding matchingMethod) {

		ReferenceBinding[] itsInterfaces = currentType.superInterfaces();
		if (itsInterfaces != NoSuperInterfaces) {
			ReferenceBinding[][] interfacesToVisit = new ReferenceBinding[5][];
			int lastPosition = -1;
			if (++lastPosition == interfacesToVisit.length)
				System.arraycopy(
					interfacesToVisit, 0,
					interfacesToVisit = new ReferenceBinding[lastPosition * 2][], 0,
					lastPosition);
			interfacesToVisit[lastPosition] = itsInterfaces;

			for (int i = 0; i <= lastPosition; i++) {
				ReferenceBinding[] interfaces = interfacesToVisit[i];
				for (int j = 0, length = interfaces.length; j < length; j++) {
					currentType = interfaces[j];
					if ((currentType.tagBits & InterfaceVisited) == 0) {
						// if interface as not already been visited
						currentType.tagBits |= InterfaceVisited;

						MethodBinding[] currentMethods = currentType.getMethods(selector);
						int currentLength = currentMethods.length;
						if (currentLength == 1 && matchingMethod == null && found.size == 0) {
							matchingMethod = currentMethods[0];
						} else if (currentLength > 0) {
							if (matchingMethod != null) {
								found.add(matchingMethod);
								matchingMethod = null;
							}
							found.addAll(currentMethods);
						}
						itsInterfaces = currentType.superInterfaces();
						if (itsInterfaces != NoSuperInterfaces) {
							if (++lastPosition == interfacesToVisit.length)
								System.arraycopy(
									interfacesToVisit, 0,
									interfacesToVisit = new ReferenceBinding[lastPosition * 2][], 0,
									lastPosition);
							interfacesToVisit[lastPosition] = itsInterfaces;
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
		return matchingMethod;
	}
	
	// Internal use only
	public MethodBinding findMethodForArray(
		ArrayBinding receiverType,
		char[] selector,
		TypeBinding[] argumentTypes,
		InvocationSite invocationSite) {

		ReferenceBinding object = getJavaLangObject();
		MethodBinding methodBinding = object.getExactMethod(selector, argumentTypes);
		if (methodBinding != null) {
			// handle the method clone() specially... cannot be protected or throw exceptions
			if (argumentTypes == NoParameters && CharOperation.equals(selector, CLONE))
				return new MethodBinding(
					(methodBinding.modifiers ^ AccProtected) | AccPublic,
					CLONE,
					methodBinding.returnType,
					argumentTypes,
					null,
					object);
			if (methodBinding.canBeSeenBy(receiverType, invocationSite, this))
				return methodBinding;
		}
		// answers closest approximation, may not check argumentTypes or visibility
		methodBinding = findMethod(object, selector, argumentTypes, invocationSite);
		if (methodBinding == null)
			return new ProblemMethodBinding(selector, argumentTypes, NotFound);
		if (methodBinding.isValidBinding()) {
			if (!areParametersAssignable(methodBinding.parameters, argumentTypes))
				return new ProblemMethodBinding(
					methodBinding,
					selector,
					argumentTypes,
					NotFound);
			if (!methodBinding.canBeSeenBy(receiverType, invocationSite, this))
				return new ProblemMethodBinding(
					selector,
					argumentTypes,
					methodBinding.declaringClass,
					NotVisible);
		}
		return methodBinding;
	}

	// Internal use only
	public ReferenceBinding findType(
		char[] typeName,
		PackageBinding declarationPackage,
		PackageBinding invocationPackage) {

		compilationUnitScope().recordReference(declarationPackage.compoundName, typeName);
		ReferenceBinding typeBinding = declarationPackage.getType(typeName);
		if (typeBinding == null)
			return null;

		if (typeBinding.isValidBinding()) {
// Not convinced that this is necessary
//			compilationUnitScope().recordTypeReference(typeBinding); // to record supertypes
			if (declarationPackage != invocationPackage && !typeBinding.canBeSeenBy(invocationPackage))
				return new ProblemReferenceBinding(typeName, typeBinding, NotVisible);
		}
		return typeBinding;
	}

	public TypeBinding getBaseType(char[] name) {
		// list should be optimized (with most often used first)
		int length = name.length;
		if (length > 2 && length < 8) {
			switch (name[0]) {
				case 'i' :
					if (length == 3 && name[1] == 'n' && name[2] == 't')
						return IntBinding;
					break;
				case 'v' :
					if (length == 4 && name[1] == 'o' && name[2] == 'i' && name[3] == 'd')
						return VoidBinding;
					break;
				case 'b' :
					if (length == 7
						&& name[1] == 'o'
						&& name[2] == 'o'
						&& name[3] == 'l'
						&& name[4] == 'e'
						&& name[5] == 'a'
						&& name[6] == 'n')
						return BooleanBinding;
					if (length == 4 && name[1] == 'y' && name[2] == 't' && name[3] == 'e')
						return ByteBinding;
					break;
				case 'c' :
					if (length == 4 && name[1] == 'h' && name[2] == 'a' && name[3] == 'r')
						return CharBinding;
					break;
				case 'd' :
					if (length == 6
						&& name[1] == 'o'
						&& name[2] == 'u'
						&& name[3] == 'b'
						&& name[4] == 'l'
						&& name[5] == 'e')
						return DoubleBinding;
					break;
				case 'f' :
					if (length == 5
						&& name[1] == 'l'
						&& name[2] == 'o'
						&& name[3] == 'a'
						&& name[4] == 't')
						return FloatBinding;
					break;
				case 'l' :
					if (length == 4 && name[1] == 'o' && name[2] == 'n' && name[3] == 'g')
						return LongBinding;
					break;
				case 's' :
					if (length == 5
						&& name[1] == 'h'
						&& name[2] == 'o'
						&& name[3] == 'r'
						&& name[4] == 't')
						return ShortBinding;
			}
		}
		return null;
	}

	public final PackageBinding getCurrentPackage() {
		Scope scope, unitScope = this;
		while ((scope = unitScope.parent) != null)
			unitScope = scope;
		return ((CompilationUnitScope) unitScope).fPackage;
	}

	public final ReferenceBinding getJavaIoSerializable() {
		compilationUnitScope().recordQualifiedReference(JAVA_IO_SERIALIZABLE);
		ReferenceBinding type = environment().getType(JAVA_IO_SERIALIZABLE);
		if (type != null) return type;
	
		problemReporter().isClassPathCorrect(JAVA_IO_SERIALIZABLE, referenceCompilationUnit());
		return null; // will not get here since the above error aborts the compilation
	}

	public final ReferenceBinding getJavaLangClass() {
		compilationUnitScope().recordQualifiedReference(JAVA_LANG_CLASS);
		ReferenceBinding type = environment().getType(JAVA_LANG_CLASS);
		if (type != null) return type;
	
		problemReporter().isClassPathCorrect(JAVA_LANG_CLASS, referenceCompilationUnit());
		return null; // will not get here since the above error aborts the compilation
	}

	public final ReferenceBinding getJavaLangCloneable() {
		compilationUnitScope().recordQualifiedReference(JAVA_LANG_CLONEABLE);
		ReferenceBinding type = environment().getType(JAVA_LANG_CLONEABLE);
		if (type != null) return type;
	
		problemReporter().isClassPathCorrect(JAVA_LANG_CLONEABLE, referenceCompilationUnit());
		return null; // will not get here since the above error aborts the compilation
	}

	public final ReferenceBinding getJavaLangError() {
		compilationUnitScope().recordQualifiedReference(JAVA_LANG_ERROR);
		ReferenceBinding type = environment().getType(JAVA_LANG_ERROR);
		if (type != null) return type;
	
		problemReporter().isClassPathCorrect(JAVA_LANG_ERROR, referenceCompilationUnit());
		return null; // will not get here since the above error aborts the compilation
	}

	public final ReferenceBinding getJavaLangAssertionError() {
		compilationUnitScope().recordQualifiedReference(JAVA_LANG_ASSERTIONERROR);
		ReferenceBinding type = environment().getType(JAVA_LANG_ASSERTIONERROR);
		if (type != null) return type;
		problemReporter().isClassPathCorrect(JAVA_LANG_ASSERTIONERROR, referenceCompilationUnit());
		return null; // will not get here since the above error aborts the compilation
	}

	public final ReferenceBinding getJavaLangObject() {
		compilationUnitScope().recordQualifiedReference(JAVA_LANG_OBJECT);
		ReferenceBinding type = environment().getType(JAVA_LANG_OBJECT);
		if (type != null) return type;
	
		problemReporter().isClassPathCorrect(JAVA_LANG_OBJECT, referenceCompilationUnit());
		return null; // will not get here since the above error aborts the compilation
	}

	public final ReferenceBinding getJavaLangRuntimeException() {
		compilationUnitScope().recordQualifiedReference(JAVA_LANG_RUNTIMEEXCEPTION);
		ReferenceBinding type = environment().getType(JAVA_LANG_RUNTIMEEXCEPTION);
		if (type != null) return type;
	
		problemReporter().isClassPathCorrect(JAVA_LANG_RUNTIMEEXCEPTION, referenceCompilationUnit());
		return null; // will not get here since the above error aborts the compilation
	}

	public final ReferenceBinding getJavaLangString() {
		compilationUnitScope().recordQualifiedReference(JAVA_LANG_STRING);
		ReferenceBinding type = environment().getType(JAVA_LANG_STRING);
		if (type != null) return type;
	
		problemReporter().isClassPathCorrect(JAVA_LANG_STRING, referenceCompilationUnit());
		return null; // will not get here since the above error aborts the compilation
	}

	public final ReferenceBinding getJavaLangThrowable() {
		compilationUnitScope().recordQualifiedReference(JAVA_LANG_THROWABLE);
		ReferenceBinding type = environment().getType(JAVA_LANG_THROWABLE);
		if (type != null) return type;
	
		problemReporter().isClassPathCorrect(JAVA_LANG_THROWABLE, referenceCompilationUnit());
		return null; // will not get here since the above error aborts the compilation
	}

	/* Answer the type binding corresponding to the typeName argument, relative to the enclosingType.
	*/
	public final ReferenceBinding getMemberType(char[] typeName, ReferenceBinding enclosingType) {
		ReferenceBinding memberType = findMemberType(typeName, enclosingType);
		if (memberType != null) return memberType;
		return new ProblemReferenceBinding(typeName, NotFound);
	}

	/* Answer the type binding corresponding to the compoundName.
	*
	* NOTE: If a problem binding is returned, senders should extract the compound name
	* from the binding & not assume the problem applies to the entire compoundName.
	*/
	public final TypeBinding getType(char[][] compoundName) {
		int typeNameLength = compoundName.length;
		if (typeNameLength == 1) {
			// Would like to remove this test and require senders to specially handle base types
			TypeBinding binding = getBaseType(compoundName[0]);
			if (binding != null) return binding;
		}

		compilationUnitScope().recordQualifiedReference(compoundName);
		Binding binding =
			getTypeOrPackage(compoundName[0], typeNameLength == 1 ? TYPE : TYPE | PACKAGE);
		if (binding == null)
			return new ProblemReferenceBinding(compoundName[0], NotFound);
		if (!binding.isValidBinding())
			return (ReferenceBinding) binding;

		int currentIndex = 1;
		boolean checkVisibility = false;
		if (binding instanceof PackageBinding) {
			PackageBinding packageBinding = (PackageBinding) binding;
			while (currentIndex < typeNameLength) {
				binding = packageBinding.getTypeOrPackage(compoundName[currentIndex++]); // does not check visibility
				if (binding == null)
					return new ProblemReferenceBinding(
						CharOperation.subarray(compoundName, 0, currentIndex),
						NotFound);
				if (!binding.isValidBinding())
					return new ProblemReferenceBinding(
						CharOperation.subarray(compoundName, 0, currentIndex),
						binding.problemId());
				if (!(binding instanceof PackageBinding))
					break;
				packageBinding = (PackageBinding) binding;
			}
			if (binding instanceof PackageBinding)
				return new ProblemReferenceBinding(
					CharOperation.subarray(compoundName, 0, currentIndex),
					NotFound);
			checkVisibility = true;
		}

		// binding is now a ReferenceBinding
		ReferenceBinding typeBinding = (ReferenceBinding) binding;
		compilationUnitScope().recordTypeReference(typeBinding); // to record supertypes
		if (checkVisibility) // handles the fall through case
			if (!typeBinding.canBeSeenBy(this))
				return new ProblemReferenceBinding(
					CharOperation.subarray(compoundName, 0, currentIndex),
					typeBinding,
					NotVisible);

		while (currentIndex < typeNameLength) {
			typeBinding = getMemberType(compoundName[currentIndex++], typeBinding);
			if (!typeBinding.isValidBinding())
				return new ProblemReferenceBinding(
					CharOperation.subarray(compoundName, 0, currentIndex),
					typeBinding.problemId());
		}
		return typeBinding;
	}

	/* Answer the type binding that corresponds the given name, starting the lookup in the receiver.
	* The name provided is a simple source name (e.g., "Object" , "Point", ...)
	*/
	// The return type of this method could be ReferenceBinding if we did not answer base types.
	// NOTE: We could support looking for Base Types last in the search, however any code using
	// this feature would be extraordinarily slow.  Therefore we don't do this
	public final TypeBinding getType(char[] name) {
		// Would like to remove this test and require senders to specially handle base types
		TypeBinding binding = getBaseType(name);
		if (binding != null) return binding;
		return (ReferenceBinding) getTypeOrPackage(name, TYPE);
	}

	// Added for code assist... NOT Public API
	public final Binding getTypeOrPackage(char[][] compoundName) {
		int nameLength = compoundName.length;
		if (nameLength == 1) {
			TypeBinding binding = getBaseType(compoundName[0]);
			if (binding != null) return binding;
		}
		Binding binding = getTypeOrPackage(compoundName[0], TYPE | PACKAGE);
		if (!binding.isValidBinding()) return binding;

		int currentIndex = 1;
		boolean checkVisibility = false;
		if (binding instanceof PackageBinding) {
			PackageBinding packageBinding = (PackageBinding) binding;

			while (currentIndex < nameLength) {
				binding = packageBinding.getTypeOrPackage(compoundName[currentIndex++]);
				if (binding == null)
					return new ProblemReferenceBinding(
						CharOperation.subarray(compoundName, 0, currentIndex),
						NotFound);
				if (!binding.isValidBinding())
					return new ProblemReferenceBinding(
						CharOperation.subarray(compoundName, 0, currentIndex),
						binding.problemId());
				if (!(binding instanceof PackageBinding))
					break;
				packageBinding = (PackageBinding) binding;
			}
			if (binding instanceof PackageBinding) return binding;
			checkVisibility = true;
		}
		// binding is now a ReferenceBinding
		ReferenceBinding typeBinding = (ReferenceBinding) binding;
		if (checkVisibility) // handles the fall through case
			if (!typeBinding.canBeSeenBy(this))
				return new ProblemReferenceBinding(
					CharOperation.subarray(compoundName, 0, currentIndex),
					typeBinding,
					NotVisible);

		while (currentIndex < nameLength) {
			typeBinding = getMemberType(compoundName[currentIndex++], typeBinding);
			// checks visibility
			if (!typeBinding.isValidBinding())
				return new ProblemReferenceBinding(
					CharOperation.subarray(compoundName, 0, currentIndex),
					typeBinding.problemId());
		}
		return typeBinding;
	}

	/* Internal use only 
	*/
	final Binding getTypeOrPackage(char[] name, int mask) {
		
		Scope scope = this;
		ReferenceBinding foundType = null;
		if ((mask & TYPE) == 0) {
			Scope next = scope;
			while ((next = scope.parent) != null)
				scope = next;
		} else {
			done : while (true) { // done when a COMPILATION_UNIT_SCOPE is found
				switch (scope.kind) {
					case METHOD_SCOPE :
					case BLOCK_SCOPE :
						ReferenceBinding localType = ((BlockScope) scope).findLocalType(name); // looks in this scope only
						if (localType != null) {
							if (foundType != null && foundType != localType)
								return new ProblemReferenceBinding(name, InheritedNameHidesEnclosingName);
							return localType;
						}
						break;
					case CLASS_SCOPE :
						SourceTypeBinding sourceType = ((ClassScope) scope).referenceContext.binding;
						if (CharOperation.equals(sourceType.sourceName, name)) {
							if (foundType != null && foundType != sourceType)
								return new ProblemReferenceBinding(name, InheritedNameHidesEnclosingName);
							return sourceType;
						}

						ReferenceBinding memberType = findMemberType(name, sourceType);
						if (memberType != null) { // skip it if we did not find anything
							if (memberType.problemId() == Ambiguous) {
								if (foundType == null || foundType.problemId() == NotVisible)
									// supercedes any potential InheritedNameHidesEnclosingName problem
									return memberType;
								else
									// make the user qualify the type, likely wants the first inherited type
									return new ProblemReferenceBinding(name, InheritedNameHidesEnclosingName);
							}
							if (memberType.isValidBinding()) {
								if (sourceType == memberType.enclosingType()
									|| environment().options.complianceLevel >= CompilerOptions.JDK1_4) {
									// found a valid type in the 'immediate' scope (ie. not inherited)
									// OR in 1.4 mode (inherited shadows enclosing)
									if (foundType == null)
										return memberType;
									if (foundType.isValidBinding())
										// if a valid type was found, complain when another is found in an 'immediate' enclosing type (ie. not inherited)
										if (foundType != memberType)
											return new ProblemReferenceBinding(name, InheritedNameHidesEnclosingName);
								}
							}
							if (foundType == null || (foundType.problemId() == NotVisible && memberType.problemId() != NotVisible))
								// only remember the memberType if its the first one found or the previous one was not visible & memberType is...
								foundType = memberType;
						}
						break;
					case COMPILATION_UNIT_SCOPE :
						break done;
				}
				scope = scope.parent;
			}
			if (foundType != null && foundType.problemId() != NotVisible)
				return foundType;
		}

		// at this point the scope is a compilation unit scope
		CompilationUnitScope unitScope = (CompilationUnitScope) scope;
		// ask for the imports + name
		if ((mask & TYPE) != 0) {
			// check single type imports.
			ImportBinding[] imports = unitScope.imports;
			if (imports != null){
				// copy the list, since single type imports are removed if they cannot be resolved
				for (int i = 0, length = imports.length; i < length; i++) {
					ImportBinding typeImport = imports[i];
					if (!typeImport.onDemand)
						if (CharOperation.equals(typeImport.compoundName[typeImport.compoundName.length - 1], name))
							if (unitScope.resolveSingleTypeImport(typeImport) != null) {
								if (typeImport.reference != null) typeImport.reference.used = true;
								return typeImport.resolvedImport; // already know its visible
							}
				}
			}
			// check if the name is in the current package (answer the problem binding unless its not found in which case continue to look)
			ReferenceBinding type = findType(name, unitScope.fPackage, unitScope.fPackage); // is always visible
			if (type != null) return type;

			// check on demand imports
			boolean foundInImport = false;
			if (imports != null){
				for (int i = 0, length = imports.length; i < length; i++) {
					ImportBinding someImport = imports[i];
					if (someImport.onDemand) {
						Binding resolvedImport = someImport.resolvedImport;
						ReferenceBinding temp =
							(resolvedImport instanceof PackageBinding)
								? findType(name, (PackageBinding) resolvedImport, unitScope.fPackage)
								: findDirectMemberType(name, (ReferenceBinding) resolvedImport);
						if (temp != null && temp.isValidBinding()) {
							if (someImport.reference != null) someImport.reference.used = true;
							if (foundInImport)
								// Answer error binding -- import on demand conflict; name found in two import on demand packages.
								return new ProblemReferenceBinding(name, Ambiguous);
							type = temp;
							foundInImport = true;
						}
					}
				}
			}
			if (type != null)
				return type;
		}
		// see if the name is a package
		if ((mask & PACKAGE) != 0) {
			compilationUnitScope().recordSimpleReference(name);
			PackageBinding packageBinding = unitScope.environment.getTopLevelPackage(name);
			if (packageBinding != null)
				return packageBinding;
		}

		compilationUnitScope().recordSimpleReference(name);
		// Answer error binding -- could not find name
		if (foundType != null){
			return foundType;
		}
		return new ProblemReferenceBinding(name, NotFound);
	}

	/* Answer whether the type is defined in the same compilation unit as the receiver
	*/
	public final boolean isDefinedInSameUnit(ReferenceBinding type) {
		// find the outer most enclosing type
		ReferenceBinding enclosingType = type;
		while ((type = enclosingType.enclosingType()) != null)
			enclosingType = type;

		// find the compilation unit scope
		Scope scope, unitScope = this;
		while ((scope = unitScope.parent) != null)
			unitScope = scope;

		// test that the enclosingType is not part of the compilation unit
		SourceTypeBinding[] topLevelTypes =
			((CompilationUnitScope) unitScope).topLevelTypes;
		for (int i = topLevelTypes.length; --i >= 0;)
			if (topLevelTypes[i] == enclosingType)
				return true;
		return false;
	}

	public final boolean isJavaIoSerializable(TypeBinding tb) {
		//a first -none optimized version-...:-)....
		//please modify as needed

		return tb == getJavaIoSerializable();
	}

	public final boolean isJavaLangCloneable(TypeBinding tb) {
		//a first -none optimized version-...:-)....
		//please modify as needed

		return tb == getJavaLangCloneable();
	}

	public final boolean isJavaLangObject(TypeBinding type) {
		return type.id == T_JavaLangObject;
	}

	public final MethodScope methodScope() {
		Scope scope = this;
		do {
			if (scope instanceof MethodScope)
				return (MethodScope) scope;
			scope = scope.parent;
		} while (scope != null);
		return null;
	}

	// Internal use only
	/* All methods in visible are acceptable matches for the method in question...
	* The methods defined by the receiver type appear before those defined by its
	* superclass and so on. We want to find the one which matches best.
	*
	* Since the receiver type is a class, we know each method's declaring class is
	* either the receiver type or one of its superclasses. It is an error if the best match
	* is defined by a superclass, when a lesser match is defined by the receiver type
	* or a closer superclass.
	*/
	protected final MethodBinding mostSpecificClassMethodBinding(MethodBinding[] visible, int visibleSize) {

		MethodBinding method = null;
		MethodBinding previous = null;

		nextVisible : for (int i = 0; i < visibleSize; i++) {
			method = visible[i];
						
			if (previous != null && method.declaringClass != previous.declaringClass)
				break; // cannot answer a method farther up the hierarchy than the first method found
			previous = method;
			for (int j = 0; j < visibleSize; j++) {
				if (i == j) continue;
				MethodBinding next = visible[j];
				if (!areParametersAssignable(next.parameters, method.parameters))
					continue nextVisible;
			}
			compilationUnitScope().recordTypeReferences(method.thrownExceptions);
			return method;
		}
		return new ProblemMethodBinding(visible[0].selector, visible[0].parameters, Ambiguous);
	}

	// Internal use only
	/* All methods in visible are acceptable matches for the method in question...
	* Since the receiver type is an interface, we ignore the possibility that 2 inherited
	* but unrelated superinterfaces may define the same method in acceptable but
	* not identical ways... we just take the best match that we find since any class which
	* implements the receiver interface MUST implement all signatures for the method...
	* in which case the best match is correct.
	*
	* NOTE: This is different than javac... in the following example, the message send of
	* bar(X) in class Y is supposed to be ambiguous. But any class which implements the
	* interface I MUST implement both signatures for bar. If this class was the receiver of
	* the message send instead of the interface I, then no problem would be reported.
	*
	interface I1 {
		void bar(J j);
	}
	interface I2 {
	//	void bar(J j);
		void bar(Object o);
	}
	interface I extends I1, I2 {}
	interface J {}
	
	class X implements J {}
	
	class Y extends X {
		public void foo(I i, X x) { i.bar(x); }
	}
	*/
	protected final MethodBinding mostSpecificInterfaceMethodBinding(MethodBinding[] visible, int visibleSize) {
		MethodBinding method = null;
		nextVisible : for (int i = 0; i < visibleSize; i++) {
			method = visible[i];
			for (int j = 0; j < visibleSize; j++) {
				if (i == j) continue;
				MethodBinding next = visible[j];
				if (!areParametersAssignable(next.parameters, method.parameters))
					continue nextVisible;
			}
			compilationUnitScope().recordTypeReferences(method.thrownExceptions);
			return method;
		}
		return new ProblemMethodBinding(visible[0].selector, visible[0].parameters, Ambiguous);
	}

	public final ClassScope outerMostClassScope() {
		ClassScope lastClassScope = null;
		Scope scope = this;
		do {
			if (scope instanceof ClassScope)
				lastClassScope = (ClassScope) scope;
			scope = scope.parent;
		} while (scope != null);
		return lastClassScope; // may answer null if no class around
	}

	public final MethodScope outerMostMethodScope() {
		MethodScope lastMethodScope = null;
		Scope scope = this;
		do {
			if (scope instanceof MethodScope)
				lastMethodScope = (MethodScope) scope;
			scope = scope.parent;
		} while (scope != null);
		return lastMethodScope; // may answer null if no method around
	}

	public final CompilationUnitDeclaration referenceCompilationUnit() {
		Scope scope, unitScope = this;
		while ((scope = unitScope.parent) != null)
			unitScope = scope;
		return ((CompilationUnitScope) unitScope).referenceContext;
	}
	// start position in this scope - for ordering scopes vs. variables
	int startIndex() {
		return 0;
	}
}