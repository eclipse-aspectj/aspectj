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

import org.eclipse.jdt.internal.compiler.ast.ConstructorDeclaration;
import org.eclipse.jdt.internal.compiler.env.IBinaryField;
import org.eclipse.jdt.internal.compiler.env.IBinaryMethod;
import org.eclipse.jdt.internal.compiler.env.IBinaryNestedType;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.compiler.problem.AbortCompilation;
import org.eclipse.jdt.internal.compiler.util.CharOperation;

/*
Not all fields defined by this type are initialized when it is created.
Some are initialized only when needed.

Accessors have been provided for some public fields so all TypeBindings have the same API...
but access public fields directly whenever possible.
Non-public fields have accessors which should be used everywhere you expect the field to be initialized.

null is NOT a valid value for a non-public field... it just means the field is not initialized.
*/

public final class BinaryTypeBinding extends ReferenceBinding {
	// all of these fields are ONLY guaranteed to be initialized if accessed using their public accessor method
	private ReferenceBinding superclass;
	private ReferenceBinding enclosingType;
	private ReferenceBinding[] superInterfaces;
	private FieldBinding[] fields;
	private MethodBinding[] methods;
	private ReferenceBinding[] memberTypes;

	// For the link with the principle structure
	private LookupEnvironment environment;
public BinaryTypeBinding(PackageBinding packageBinding, IBinaryType binaryType, LookupEnvironment environment) {
	this.compoundName = CharOperation.splitOn('/', binaryType.getName());
	computeId();

	this.tagBits |= IsBinaryBinding;
	this.environment = environment;
	this.fPackage = packageBinding;
	this.	fileName = binaryType.getFileName();

	// source name must be one name without "$".
	char[] possibleSourceName = this.compoundName[this.compoundName.length - 1];
	int start = CharOperation.lastIndexOf('$', possibleSourceName) + 1;
	if (start == 0) {
		this.sourceName = possibleSourceName;
	} else {
		this.sourceName = new char[possibleSourceName.length - start];
		System.arraycopy(possibleSourceName, start, this.sourceName, 0, this.sourceName.length);
	}

	this.modifiers = binaryType.getModifiers();
	if (binaryType.isInterface())
		this.modifiers |= AccInterface;
}

public FieldBinding[] availableFields() {
	FieldBinding[] availableFields = new FieldBinding[fields.length];
	int count = 0;
	
	for (int i = 0; i < fields.length;i++) {
		try {
			availableFields[count] = resolveTypeFor(fields[i]);
			count++;
		} catch (AbortCompilation a){
		}
	}
	
	System.arraycopy(availableFields, 0, availableFields = new FieldBinding[count], 0, count);
	return availableFields;
}

public MethodBinding[] availableMethods() {
	if ((modifiers & AccUnresolved) == 0)
		return methods;
		
	MethodBinding[] availableMethods = new MethodBinding[methods.length];
	int count = 0;
	
	for (int i = 0; i < methods.length;i++) {
		try {
			availableMethods[count] = resolveTypesFor(methods[i]);
			count++;
		} catch (AbortCompilation a){
		}
	}
	System.arraycopy(availableMethods, 0, availableMethods = new MethodBinding[count], 0, count);
	return availableMethods;
}

void cachePartsFrom(IBinaryType binaryType, boolean needFieldsAndMethods) {
	char[] superclassName = binaryType.getSuperclassName();
	if (superclassName != null)
		// attempt to find the superclass if it exists in the cache (otherwise - resolve it when requested)
		this.superclass = environment.getTypeFromConstantPoolName(superclassName, 0, -1);

	char[] enclosingTypeName = binaryType.getEnclosingTypeName();
	if (enclosingTypeName != null) {
		// attempt to find the enclosing type if it exists in the cache (otherwise - resolve it when requested)
		this.enclosingType = environment.getTypeFromConstantPoolName(enclosingTypeName, 0, -1);
		this.tagBits |= MemberTypeMask;   // must be a member type not a top-level or local type
		if (this.enclosingType().isStrictfp())
			this.modifiers |= AccStrictfp;
		if (this.enclosingType().isDeprecated())
			this.modifiers |= AccDeprecatedImplicitly;
	}

	this.memberTypes = NoMemberTypes;
	IBinaryNestedType[] memberTypeStructures = binaryType.getMemberTypes();
	if (memberTypeStructures != null) {
		int size = memberTypeStructures.length;
		if (size > 0) {
			this.memberTypes = new ReferenceBinding[size];
			for (int i = 0; i < size; i++)
				// attempt to find each member type if it exists in the cache (otherwise - resolve it when requested)
				this.memberTypes[i] = environment.getTypeFromConstantPoolName(memberTypeStructures[i].getName(), 0, -1);
		}
	}

	this.superInterfaces = NoSuperInterfaces;
	char[][] interfaceNames = binaryType.getInterfaceNames();
	if (interfaceNames != null) {
		int size = interfaceNames.length;
		if (size > 0) {
			this.superInterfaces = new ReferenceBinding[size];
			for (int i = 0; i < size; i++)
				// attempt to find each superinterface if it exists in the cache (otherwise - resolve it when requested)
				this.superInterfaces[i] = environment.getTypeFromConstantPoolName(interfaceNames[i], 0, -1);
		}
	}
	if (needFieldsAndMethods){
		createFields(binaryType.getFields());
		createMethods(binaryType.getMethods());
	}
}
private void createFields(IBinaryField[] iFields) {
	this.fields = NoFields;
	if (iFields != null) {
		int size = iFields.length;
		if (size > 0) {
			this.fields = new FieldBinding[size];
			for (int i = 0; i < size; i++) {
				IBinaryField field = iFields[i];
				this.fields[i] =
					new FieldBinding(
						field.getName(),
						environment.getTypeFromSignature(field.getTypeName(), 0, -1),
						field.getModifiers(),
						this,
						field.getConstant());
			}
		}
	}
}
private MethodBinding createMethod(IBinaryMethod method) {
	int modifiers = method.getModifiers() | AccUnresolved;

	ReferenceBinding[] exceptions = NoExceptions;
	char[][] exceptionTypes = method.getExceptionTypeNames();
	if (exceptionTypes != null) {
		int size = exceptionTypes.length;
		if (size > 0) {
			exceptions = new ReferenceBinding[size];
			for (int i = 0; i < size; i++)
				exceptions[i] = environment.getTypeFromConstantPoolName(exceptionTypes[i], 0, -1);
		}
	}

	TypeBinding[] parameters = NoParameters;
	char[] signature = method.getMethodDescriptor();   // of the form (I[Ljava/jang/String;)V
	int numOfParams = 0;
	char nextChar;
	int index = 0;   // first character is always '(' so skip it
	while ((nextChar = signature[++index]) != ')') {
		if (nextChar != '[') {
			numOfParams++;
			if (nextChar == 'L')
				while ((nextChar = signature[++index]) != ';');
		}
	}

	// Ignore synthetic argument for member types.
	int startIndex = (method.isConstructor() && isMemberType() && !isStatic()) ? 1 : 0;
	int size = numOfParams - startIndex;
	if (size > 0) {
		parameters = new TypeBinding[size];
		index = 1;
		int end = 0;   // first character is always '(' so skip it
		for (int i = 0; i < numOfParams; i++) {
			while ((nextChar = signature[++end]) == '[');
			if (nextChar == 'L')
				while ((nextChar = signature[++end]) != ';');

			if (i >= startIndex)   // skip the synthetic arg if necessary
				parameters[i - startIndex] = environment.getTypeFromSignature(signature, index, end);
			index = end + 1;
		}
	}

	MethodBinding binding = null;
	if (method.isConstructor())
		binding = new MethodBinding(modifiers, parameters, exceptions, this);
	else
		binding = new MethodBinding(
			modifiers,
			method.getSelector(),
			environment.getTypeFromSignature(signature, index + 1, -1),   // index is currently pointing at the ')'
			parameters,
			exceptions,
			this);
	return binding;
}
private void createMethods(IBinaryMethod[] iMethods) {
	int total = 0;
	int clinitIndex = -1;
	if (iMethods != null) {
		total = iMethods.length;
		for (int i = total; --i >= 0;) {
			char[] methodName = iMethods[i].getSelector();
			if (methodName[0] == '<' && methodName.length == 8) { // Can only match <clinit>
				total--;
				clinitIndex = i;
				break;
			}
		}
	}
	if (total == 0) {
		this.methods = NoMethods;
		return;
	}

	this.methods = new MethodBinding[total];
	int next = 0;
	for (int i = 0, length = iMethods.length; i < length; i++)
		if (i != clinitIndex)
			this.methods[next++] = createMethod(iMethods[i]);
	modifiers |= AccUnresolved; // until methods() is sent
}
/* Answer the receiver's enclosing type... null if the receiver is a top level type.
*
* NOTE: enclosingType of a binary type is resolved when needed
*/

public ReferenceBinding enclosingType() {
	if (enclosingType == null)
		return null;
	if (enclosingType instanceof UnresolvedReferenceBinding)
		enclosingType = ((UnresolvedReferenceBinding) enclosingType).resolve(environment);
	return enclosingType;
}
// NOTE: the type of each field of a binary type is resolved when needed

public FieldBinding[] fields() {
	for (int i = fields.length; --i >= 0;)
		resolveTypeFor(fields[i]);
	return fields;
}
// NOTE: the return type, arg & exception types of each method of a binary type are resolved when needed

public MethodBinding getExactConstructor(TypeBinding[] argumentTypes) {
	int argCount = argumentTypes.length;
	nextMethod : for (int m = methods.length; --m >= 0;) {
		MethodBinding method = methods[m];
		if (method.selector == ConstructorDeclaration.ConstantPoolName && method.parameters.length == argCount) {
			resolveTypesFor(method);
			TypeBinding[] toMatch = method.parameters;
			for (int p = 0; p < argCount; p++)
				if (toMatch[p] != argumentTypes[p])
					continue nextMethod;
			return method;
		}
	}
	return null;
}
// NOTE: the return type, arg & exception types of each method of a binary type are resolved when needed
// searches up the hierarchy as long as no potential (but not exact) match was found.

public MethodBinding getExactMethod(char[] selector, TypeBinding[] argumentTypes) {
	int argCount = argumentTypes.length;
	int selectorLength = selector.length;
	boolean foundNothing = true;
	nextMethod : for (int m = methods.length; --m >= 0;) {
		MethodBinding method = methods[m];
		if (method.selector.length == selectorLength && CharOperation.prefixEquals(method.selector, selector)) {
			foundNothing = false; // inner type lookups must know that a method with this name exists
			if (method.parameters.length == argCount) {
				resolveTypesFor(method);
				TypeBinding[] toMatch = method.parameters;
				for (int p = 0; p < argCount; p++)
					if (toMatch[p] != argumentTypes[p])
						continue nextMethod;
				return method;
			}
		}
	}

	if (foundNothing) {
		if (isInterface()) {
			 if (superInterfaces.length == 1)
				return superInterfaces[0].getExactMethod(selector, argumentTypes);
		} else if (superclass != null) {
			return superclass.getExactMethod(selector, argumentTypes);
		}
	}
	return null;
}
// NOTE: the type of a field of a binary type is resolved when needed

public FieldBinding getField(char[] fieldName) {
	int fieldLength = fieldName.length;
	for (int f = fields.length; --f >= 0;) {
		char[] name = fields[f].name;
		if (name.length == fieldLength && CharOperation.prefixEquals(name, fieldName))
			return resolveTypeFor(fields[f]);
	}
	return null;
}
// NOTE: the return type, arg & exception types of each method of a binary type are resolved when needed

public MethodBinding[] getMethods(char[] selector) {
	int count = 0;
	int lastIndex = -1;
	int selectorLength = selector.length;
	for (int m = 0, length = methods.length; m < length; m++) {
		MethodBinding method = methods[m];
		if (method.selector.length == selectorLength && CharOperation.prefixEquals(method.selector, selector)) {
			resolveTypesFor(method);
			count++;
			lastIndex = m;
		}
	}
	if (count == 1)
		return new MethodBinding[] {methods[lastIndex]};
	if (count > 0) {
		MethodBinding[] result = new MethodBinding[count];
		count = 0;
		for (int m = 0; m <= lastIndex; m++) {
			MethodBinding method = methods[m];
			if (method.selector.length == selectorLength && CharOperation.prefixEquals(method.selector, selector))
				result[count++] = method;
		}
		return result;
	}
	return NoMethods;
}
// NOTE: member types of binary types are resolved when needed

public ReferenceBinding[] memberTypes() {
	for (int i = memberTypes.length; --i >= 0;)
		if (memberTypes[i] instanceof UnresolvedReferenceBinding)
			memberTypes[i] = ((UnresolvedReferenceBinding) memberTypes[i]).resolve(environment);
	return memberTypes;
}
// NOTE: the return type, arg & exception types of each method of a binary type are resolved when needed

public MethodBinding[] methods() {
	if ((modifiers & AccUnresolved) == 0)
		return methods;

	for (int i = methods.length; --i >= 0;)
		resolveTypesFor(methods[i]);
	modifiers ^= AccUnresolved;
	return methods;
}
private TypeBinding resolveType(TypeBinding type) {
	if (type instanceof UnresolvedReferenceBinding)
		return ((UnresolvedReferenceBinding) type).resolve(environment);
	if (type instanceof ArrayBinding) {
		ArrayBinding array = (ArrayBinding) type;
		if (array.leafComponentType instanceof UnresolvedReferenceBinding)
			array.leafComponentType = ((UnresolvedReferenceBinding) array.leafComponentType).resolve(environment);
	}
	return type;
}
private FieldBinding resolveTypeFor(FieldBinding field) {
	field.type = resolveType(field.type);
	return field;
}
private MethodBinding resolveTypesFor(MethodBinding method) {
	if ((method.modifiers & AccUnresolved) == 0)
		return method;

	if (!method.isConstructor())
		method.returnType = resolveType(method.returnType);
	for (int i = method.parameters.length; --i >= 0;)
		method.parameters[i] = resolveType(method.parameters[i]);
	for (int i = method.thrownExceptions.length; --i >= 0;)
		if (method.thrownExceptions[i] instanceof UnresolvedReferenceBinding)
			method.thrownExceptions[i] = ((UnresolvedReferenceBinding) method.thrownExceptions[i]).resolve(environment);
	method.modifiers ^= AccUnresolved;
	return method;
}
/* Answer the receiver's superclass... null if the receiver is Object or an interface.
*
* NOTE: superclass of a binary type is resolved when needed
*/

public ReferenceBinding superclass() {
	if (superclass == null)
		return null;
	if (superclass instanceof UnresolvedReferenceBinding)
		superclass = ((UnresolvedReferenceBinding) superclass).resolve(environment);
	return superclass;
}
// NOTE: superInterfaces of binary types are resolved when needed

public ReferenceBinding[] superInterfaces() {
	for (int i = superInterfaces.length; --i >= 0;)
		if (superInterfaces[i] instanceof UnresolvedReferenceBinding)
			superInterfaces[i] = ((UnresolvedReferenceBinding) superInterfaces[i]).resolve(environment);
	return superInterfaces;
}
public String toString() {
	String s = ""; //$NON-NLS-1$

	if (isDeprecated()) s += "deprecated "; //$NON-NLS-1$
	if (isPublic()) s += "public "; //$NON-NLS-1$
	if (isProtected()) s += "protected "; //$NON-NLS-1$
	if (isPrivate()) s += "private "; //$NON-NLS-1$
	if (isAbstract() && isClass()) s += "abstract "; //$NON-NLS-1$
	if (isStatic() && isNestedType()) s += "static "; //$NON-NLS-1$
	if (isFinal()) s += "final "; //$NON-NLS-1$

	s += isInterface() ? "interface " : "class "; //$NON-NLS-1$ //$NON-NLS-2$
	s += (compoundName != null) ? CharOperation.toString(compoundName) : "UNNAMED TYPE"; //$NON-NLS-1$

	s += "\n\textends "; //$NON-NLS-1$
	s += (superclass != null) ? superclass.debugName() : "NULL TYPE"; //$NON-NLS-1$

	if (superInterfaces != null) {
		if (superInterfaces != NoSuperInterfaces) {
			s += "\n\timplements : "; //$NON-NLS-1$
			for (int i = 0, length = superInterfaces.length; i < length; i++) {
				if (i  > 0)
					s += ", "; //$NON-NLS-1$
				s += (superInterfaces[i] != null) ? superInterfaces[i].debugName() : "NULL TYPE"; //$NON-NLS-1$
			}
		}
	} else {
		s += "NULL SUPERINTERFACES"; //$NON-NLS-1$
	}

	if (enclosingType != null) {
		s += "\n\tenclosing type : "; //$NON-NLS-1$
		s += enclosingType.debugName();
	}

	if (fields != null) {
		if (fields != NoFields) {
			s += "\n/*   fields   */"; //$NON-NLS-1$
			for (int i = 0, length = fields.length; i < length; i++)
				s += (fields[i] != null) ? "\n" + fields[i].toString() : "\nNULL FIELD"; //$NON-NLS-1$ //$NON-NLS-2$
		}
	} else {
		s += "NULL FIELDS"; //$NON-NLS-1$
	}

	if (methods != null) {
		if (methods != NoMethods) {
			s += "\n/*   methods   */"; //$NON-NLS-1$
			for (int i = 0, length = methods.length; i < length; i++)
				s += (methods[i] != null) ? "\n" + methods[i].toString() : "\nNULL METHOD"; //$NON-NLS-1$ //$NON-NLS-2$
		}
	} else {
		s += "NULL METHODS"; //$NON-NLS-1$
	}

	if (memberTypes != null) {
		if (memberTypes != NoMemberTypes) {
			s += "\n/*   members   */"; //$NON-NLS-1$
			for (int i = 0, length = memberTypes.length; i < length; i++)
				s += (memberTypes[i] != null) ? "\n" + memberTypes[i].toString() : "\nNULL TYPE"; //$NON-NLS-1$ //$NON-NLS-2$
		}
	} else {
		s += "NULL MEMBER TYPES"; //$NON-NLS-1$
	}

	s += "\n\n\n"; //$NON-NLS-1$
	return s;
}
}
