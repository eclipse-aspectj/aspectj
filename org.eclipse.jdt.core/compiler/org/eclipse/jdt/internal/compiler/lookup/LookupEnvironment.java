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

import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.impl.ITypeRequestor;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.eclipse.jdt.internal.compiler.util.CharOperation;
import org.eclipse.jdt.internal.compiler.util.HashtableOfPackage;
import org.eclipse.jdt.internal.compiler.util.Util;

/**
 * AspectJ - made many methods and fields more visible for extension
 * 
 * Also modified error checking on getType(char[][] compoundName) to allow
 * refering to inner types directly.
 */
public class LookupEnvironment implements BaseTypes, ProblemReasons, TypeConstants {
	
	
	
	public CompilerOptions options;
	public ProblemReporter problemReporter;
	public ITypeRequestor typeRequestor;

	PackageBinding defaultPackage;
	ImportBinding[] defaultImports;
	HashtableOfPackage knownPackages;
	static final ProblemPackageBinding theNotFoundPackage = new ProblemPackageBinding(new char[0], NotFound);
	static final ProblemReferenceBinding theNotFoundType = new ProblemReferenceBinding(new char[0], NotFound);

	private INameEnvironment nameEnvironment;
	private MethodVerifier verifier;
	private ArrayBinding[][] uniqueArrayBindings;

	protected CompilationUnitDeclaration[] units = new CompilationUnitDeclaration[4];
	protected int lastUnitIndex = -1;
	protected int lastCompletedUnitIndex = -1;

	// indicate in which step on the compilation we are.
	// step 1 : build the reference binding
	// step 2 : conect the hierarchy (connect bindings)
	// step 3 : build fields and method bindings.
	protected int stepCompleted;
	final protected static int BUILD_TYPE_HIERARCHY = 1;
	final protected static int CHECK_AND_SET_IMPORTS = 2;
	final protected static int CONNECT_TYPE_HIERARCHY = 3;
	final protected static int BUILD_FIELDS_AND_METHODS = 4;
public LookupEnvironment(ITypeRequestor typeRequestor, CompilerOptions options, ProblemReporter problemReporter, INameEnvironment nameEnvironment) {
	this.typeRequestor = typeRequestor;
	this.options = options;
	this.problemReporter = problemReporter;
	this.defaultPackage = new PackageBinding(this); // assume the default package always exists
	this.defaultImports = null;
	this.nameEnvironment = nameEnvironment;
	this.knownPackages = new HashtableOfPackage();
	this.uniqueArrayBindings = new ArrayBinding[5][];
	this.uniqueArrayBindings[0] = new ArrayBinding[50]; // start off the most common 1 dimension array @ 50
}
/* Ask the oracle for a type which corresponds to the compoundName.
* Answer null if the name cannot be found.
*/

public ReferenceBinding askForType(char[][] compoundName) {
	NameEnvironmentAnswer answer = nameEnvironment.findType(compoundName);
	if (answer == null)
		return null;

	if (answer.isBinaryType())
		// the type was found as a .class file
		typeRequestor.accept(answer.getBinaryType(), computePackageFrom(compoundName));
	else if (answer.isCompilationUnit())
		// the type was found as a .java file, try to build it then search the cache
		typeRequestor.accept(answer.getCompilationUnit());
	else if (answer.isSourceType())
		// the type was found as a source model
		typeRequestor.accept(answer.getSourceTypes(), computePackageFrom(compoundName));

	return getCachedType(compoundName);
}
/* Ask the oracle for a type named name in the packageBinding.
* Answer null if the name cannot be found.
*/

ReferenceBinding askForType(PackageBinding packageBinding, char[] name) {
	if (packageBinding == null) {
		if (defaultPackage == null)
			return null;
		packageBinding = defaultPackage;
	}
	NameEnvironmentAnswer answer = nameEnvironment.findType(name, packageBinding.compoundName);
	if (answer == null)
		return null;

	if (answer.isBinaryType())
		// the type was found as a .class file
		typeRequestor.accept(answer.getBinaryType(), packageBinding);
	else if (answer.isCompilationUnit())
		// the type was found as a .java file, try to build it then search the cache
		typeRequestor.accept(answer.getCompilationUnit());
	else if (answer.isSourceType())
		// the type was found as a source model
		typeRequestor.accept(answer.getSourceTypes(), packageBinding);

	return packageBinding.getType0(name);
}
/* Create the initial type bindings for the compilation unit.
*
* See completeTypeBindings() for a description of the remaining steps
*
* NOTE: This method can be called multiple times as additional source files are needed
*/

public void buildTypeBindings(CompilationUnitDeclaration unit) {
	CompilationUnitScope scope = new CompilationUnitScope(unit, this);
	scope.buildTypeBindings();

	int unitsLength = units.length;
	if (++lastUnitIndex >= unitsLength)
		System.arraycopy(units, 0, units = new CompilationUnitDeclaration[2 * unitsLength], 0, unitsLength);
	units[lastUnitIndex] = unit;
}
/* Cache the binary type since we know it is needed during this compile.
*
* Answer the created BinaryTypeBinding or null if the type is already in the cache.
*/

public BinaryTypeBinding cacheBinaryType(IBinaryType binaryType) {
	return cacheBinaryType(binaryType, true);
}
/* Cache the binary type since we know it is needed during this compile.
*
* Answer the created BinaryTypeBinding or null if the type is already in the cache.
*/

public BinaryTypeBinding cacheBinaryType(IBinaryType binaryType, boolean needFieldsAndMethods) {
	char[][] compoundName = CharOperation.splitOn('/', binaryType.getName());
	ReferenceBinding existingType = getCachedType(compoundName);

	if (existingType == null || existingType instanceof UnresolvedReferenceBinding)
		// only add the binary type if its not already in the cache
		return createBinaryTypeFrom(binaryType, computePackageFrom(compoundName), needFieldsAndMethods);
	return null; // the type already exists & can be retrieved from the cache
}
/*
* 1. Connect the type hierarchy for the type bindings created for parsedUnits.
* 2. Create the field bindings
* 3. Create the method bindings
*/

/* We know each known compilationUnit is free of errors at this point...
*
* Each step will create additional bindings unless a problem is detected, in which
* case either the faulty import/superinterface/field/method will be skipped or a
* suitable replacement will be substituted (such as Object for a missing superclass)
*/

public void completeTypeBindings() {
	stepCompleted = BUILD_TYPE_HIERARCHY;
	
	for (int i = lastCompletedUnitIndex + 1; i <= lastUnitIndex; i++) {
		units[i].scope.checkAndSetImports();
	}
	stepCompleted = CHECK_AND_SET_IMPORTS;

	for (int i = lastCompletedUnitIndex + 1; i <= lastUnitIndex; i++) {
		units[i].scope.connectTypeHierarchy();
	}
	stepCompleted = CONNECT_TYPE_HIERARCHY;

	for (int i = lastCompletedUnitIndex + 1; i <= lastUnitIndex; i++) {
		units[i].scope.buildFieldsAndMethods();
		units[i] = null; // release unnecessary reference to the parsed unit
	}
	
	stepCompleted = BUILD_FIELDS_AND_METHODS;
	lastCompletedUnitIndex = lastUnitIndex;
}
/*
* 1. Connect the type hierarchy for the type bindings created for parsedUnits.
* 2. Create the field bindings
* 3. Create the method bindings
*/

/*
* Each step will create additional bindings unless a problem is detected, in which
* case either the faulty import/superinterface/field/method will be skipped or a
* suitable replacement will be substituted (such as Object for a missing superclass)
*/

public void completeTypeBindings(CompilationUnitDeclaration parsedUnit) {
	if (stepCompleted == BUILD_FIELDS_AND_METHODS) {
		// This can only happen because the original set of units are completely built and
		// are now being processed, so we want to treat all the additional units as a group
		// until they too are completely processed.
		completeTypeBindings();
	} else {
		if (parsedUnit.scope == null) return; // parsing errors were too severe

		if (stepCompleted >= CHECK_AND_SET_IMPORTS)
			parsedUnit.scope.checkAndSetImports();

		if (stepCompleted >= CONNECT_TYPE_HIERARCHY)
			parsedUnit.scope.connectTypeHierarchy();
	}
}
/*
* Used by other compiler tools which do not start by calling completeTypeBindings().
*
* 1. Connect the type hierarchy for the type bindings created for parsedUnits.
* 2. Create the field bindings
* 3. Create the method bindings
*/

public void completeTypeBindings(CompilationUnitDeclaration parsedUnit, boolean buildFieldsAndMethods) {
	if (parsedUnit.scope == null) return; // parsing errors were too severe

	parsedUnit.scope.checkAndSetImports();
	parsedUnit.scope.connectTypeHierarchy();

	if (buildFieldsAndMethods)
		parsedUnit.scope.buildFieldsAndMethods();
}
private PackageBinding computePackageFrom(char[][] constantPoolName) {
	if (constantPoolName.length == 1)
		return defaultPackage;

	PackageBinding packageBinding = getPackage0(constantPoolName[0]);
	if (packageBinding == null || packageBinding == theNotFoundPackage) {
		packageBinding = new PackageBinding(constantPoolName[0], this);
		knownPackages.put(constantPoolName[0], packageBinding);
	}

	for (int i = 1, length = constantPoolName.length - 1; i < length; i++) {
		PackageBinding parent = packageBinding;
		if ((packageBinding = parent.getPackage0(constantPoolName[i])) == null || packageBinding == theNotFoundPackage) {
			packageBinding = new PackageBinding(CharOperation.subarray(constantPoolName, 0, i + 1), parent, this);
			parent.addPackage(packageBinding);
		}
	}
	return packageBinding;
}
/* Used to guarantee array type identity.
*/

public ArrayBinding createArrayType(TypeBinding type, int dimensionCount) {
	// find the array binding cache for this dimension
	int dimIndex = dimensionCount - 1;
	int length = uniqueArrayBindings.length;
	ArrayBinding[] arrayBindings;
	if (dimIndex < length) {
		if ((arrayBindings = uniqueArrayBindings[dimIndex]) == null)
			uniqueArrayBindings[dimIndex] = arrayBindings = new ArrayBinding[10];
	} else {
		System.arraycopy(
			uniqueArrayBindings, 0, 
			uniqueArrayBindings = new ArrayBinding[dimensionCount][], 0, 
			length); 
		uniqueArrayBindings[dimIndex] = arrayBindings = new ArrayBinding[10];
	}

	// find the cached array binding for this leaf component type (if any)
	int index = -1;
	length = arrayBindings.length;
	while (++index < length) {
		ArrayBinding currentBinding = arrayBindings[index];
		if (currentBinding == null) // no matching array, but space left
			return arrayBindings[index] = new ArrayBinding(type, dimensionCount);
		if (currentBinding.leafComponentType == type)
			return currentBinding;
	}

	// no matching array, no space left
	System.arraycopy(
		arrayBindings, 0,
		(arrayBindings = new ArrayBinding[length * 2]), 0,
		length); 
	uniqueArrayBindings[dimIndex] = arrayBindings;
	return arrayBindings[length] = new ArrayBinding(type, dimensionCount);
}
public BinaryTypeBinding createBinaryTypeFrom(IBinaryType binaryType, PackageBinding packageBinding) {
	return createBinaryTypeFrom(binaryType, packageBinding, true);
}
public BinaryTypeBinding createBinaryTypeFrom(IBinaryType binaryType, PackageBinding packageBinding, boolean needFieldsAndMethods) {
	BinaryTypeBinding binaryBinding = new BinaryTypeBinding(packageBinding, binaryType, this);

	// resolve any array bindings which reference the unresolvedType
	ReferenceBinding cachedType = packageBinding.getType0(binaryBinding.compoundName[binaryBinding.compoundName.length - 1]);
	if (cachedType != null) {
		if (cachedType.isBinaryBinding()) // sanity check before the cast... at this point the cache should ONLY contain unresolved types
			return (BinaryTypeBinding) cachedType;

		UnresolvedReferenceBinding unresolvedType = (UnresolvedReferenceBinding) cachedType;
		unresolvedType.resolvedType = binaryBinding;
		updateArrayCache(unresolvedType, binaryBinding);
	}

	packageBinding.addType(binaryBinding);
	binaryBinding.cachePartsFrom(binaryType, needFieldsAndMethods);
	return binaryBinding;
}
/* Used to create packages from the package statement.
*/

PackageBinding createPackage(char[][] compoundName) {
	PackageBinding packageBinding = getPackage0(compoundName[0]);
	if (packageBinding == null || packageBinding == theNotFoundPackage) {
		packageBinding = new PackageBinding(compoundName[0], this);
		knownPackages.put(compoundName[0], packageBinding);
	}

	for (int i = 1, length = compoundName.length; i < length; i++) {
		// check to see if it collides with a known type...
		// this case can only happen if the package does not exist as a directory in the file system
		// otherwise when the source type was defined, the correct error would have been reported
		// unless its an unresolved type which is referenced from an inconsistent class file
		ReferenceBinding type = packageBinding.getType0(compoundName[i]);
		if (type != null && type != theNotFoundType && !(type instanceof UnresolvedReferenceBinding))
			return null;

		PackageBinding parent = packageBinding;
		if ((packageBinding = parent.getPackage0(compoundName[i])) == null || packageBinding == theNotFoundPackage) {
			// if the package is unknown, check to see if a type exists which would collide with the new package
			// catches the case of a package statement of: package java.lang.Object;
			// since the package can be added after a set of source files have already been compiled, we need
			// whenever a package statement is encountered
			if (nameEnvironment.findType(compoundName[i], parent.compoundName) != null)
				return null;

			packageBinding = new PackageBinding(CharOperation.subarray(compoundName, 0, i + 1), parent, this);
			parent.addPackage(packageBinding);
		}
	}
	return packageBinding;
}
/* Answer the type for the compoundName if it exists in the cache.
* Answer theNotFoundType if it could not be resolved the first time
* it was looked up, otherwise answer null.
*
* NOTE: Do not use for nested types... the answer is NOT the same for a.b.C or a.b.C.D.E
* assuming C is a type in both cases. In the a.b.C.D.E case, null is the answer.
*/

public ReferenceBinding getCachedType(char[][] compoundName) {
	if (compoundName.length == 1) {
		if (defaultPackage == null)
			return null;
		return defaultPackage.getType0(compoundName[0]);
	}

	PackageBinding packageBinding = getPackage0(compoundName[0]);
	if (packageBinding == null || packageBinding == theNotFoundPackage)
		return null;

	for (int i = 1, packageLength = compoundName.length - 1; i < packageLength; i++)
		if ((packageBinding = packageBinding.getPackage0(compoundName[i])) == null || packageBinding == theNotFoundPackage)
			return null;
	return packageBinding.getType0(compoundName[compoundName.length - 1]);
}
/* Answer the top level package named name if it exists in the cache.
* Answer theNotFoundPackage if it could not be resolved the first time
* it was looked up, otherwise answer null.
*
* NOTE: Senders must convert theNotFoundPackage into a real problem
* package if its to returned.
*/

PackageBinding getPackage0(char[] name) {
	return knownPackages.get(name);
}
/* Answer the top level package named name.
* Ask the oracle for the package if its not in the cache.
* Answer null if the package cannot be found.
*/

PackageBinding getTopLevelPackage(char[] name) {
	PackageBinding packageBinding = getPackage0(name);
	if (packageBinding != null) {
		if (packageBinding == theNotFoundPackage)
			return null;
		else
			return packageBinding;
	}

	if (nameEnvironment.isPackage(null, name)) {
		knownPackages.put(name, packageBinding = new PackageBinding(name, this));
		return packageBinding;
	}

	knownPackages.put(name, theNotFoundPackage); // saves asking the oracle next time
	return null;
}
/* Answer the type corresponding to the compoundName.
* Ask the oracle for the type if its not in the cache.
* Answer null if the type cannot be found... likely a fatal error.
*/

public ReferenceBinding getType(char[][] compoundName) {
	ReferenceBinding referenceBinding;

	if (compoundName.length == 1) {
		if (defaultPackage == null)
			return null;

		if ((referenceBinding = defaultPackage.getType0(compoundName[0])) == null) {
			PackageBinding packageBinding = getPackage0(compoundName[0]);
			if (packageBinding != null && packageBinding != theNotFoundPackage)
				return null; // collides with a known package... should not call this method in such a case
			referenceBinding = askForType(defaultPackage, compoundName[0]);
		}
	} else {
		PackageBinding packageBinding = getPackage0(compoundName[0]);
		if (packageBinding == theNotFoundPackage)
			return null;

		if (packageBinding != null) {
			for (int i = 1, packageLength = compoundName.length - 1; i < packageLength; i++) {
				if ((packageBinding = packageBinding.getPackage0(compoundName[i])) == null)
					break;
				if (packageBinding == theNotFoundPackage)
					return null;
			}
		}

		if (packageBinding == null)
			referenceBinding = askForType(compoundName);
		else if ((referenceBinding = packageBinding.getType0(compoundName[compoundName.length - 1])) == null)
			referenceBinding = askForType(packageBinding, compoundName[compoundName.length - 1]);
	}

	if (referenceBinding == null || referenceBinding == theNotFoundType)
		return null;
	if (referenceBinding instanceof UnresolvedReferenceBinding)
		referenceBinding = ((UnresolvedReferenceBinding) referenceBinding).resolve(this);

	// compoundName refers to a nested type incorrectly (i.e. package1.A$B)
	//XXX how else are we supposed to refer to nested types???
//	if (referenceBinding.isNestedType())
//		return new ProblemReferenceBinding(compoundName, InternalNameProvided);
//	else
		return referenceBinding;
}
/* Answer the type corresponding to the name from the binary file.
* Does not ask the oracle for the type if its not found in the cache... instead an
* unresolved type is returned which must be resolved before used.
*
* NOTE: Does NOT answer base types nor array types!
*
* NOTE: Aborts compilation if the class file cannot be found.
*/

ReferenceBinding getTypeFromConstantPoolName(char[] signature, int start, int end) {
	if (end == -1)
		end = signature.length - 1;

	char[][] compoundName = CharOperation.splitOn('/', signature, start, end);
	ReferenceBinding binding = getCachedType(compoundName);
	if (binding == null) {
		PackageBinding packageBinding = computePackageFrom(compoundName);
		binding = new UnresolvedReferenceBinding(compoundName, packageBinding);
		packageBinding.addType(binding);
	} else if (binding == theNotFoundType) {
		problemReporter.isClassPathCorrect(compoundName, null);
		return null; // will not get here since the above error aborts the compilation
	}
	return binding;
}
/* Answer the type corresponding to the signature from the binary file.
* Does not ask the oracle for the type if its not found in the cache... instead an
* unresolved type is returned which must be resolved before used.
*
* NOTE: Does answer base types & array types.
*
* NOTE: Aborts compilation if the class file cannot be found.
*/

TypeBinding getTypeFromSignature(char[] signature, int start, int end) {
	int dimension = 0;
	while (signature[start] == '[') {
		start++;
		dimension++;
	}
	if (end == -1)
		end = signature.length - 1;

	// Just switch on signature[start] - the L case is the else
	TypeBinding binding = null;
	if (start == end) {
		switch (signature[start]) {
			case 'I' :
				binding = IntBinding;
				break;
			case 'Z' :
				binding = BooleanBinding;
				break;
			case 'V' :
				binding = VoidBinding;
				break;
			case 'C' :
				binding = CharBinding;
				break;
			case 'D' :
				binding = DoubleBinding;
				break;
			case 'B' :
				binding = ByteBinding;
				break;
			case 'F' :
				binding = FloatBinding;
				break;
			case 'J' :
				binding = LongBinding;
				break;
			case 'S' :
				binding = ShortBinding;
				break;
			default :
				throw new Error(Util.bind("error.undefinedBaseType",String.valueOf(signature[start]))); //$NON-NLS-1$
		}
	} else {
		binding = getTypeFromConstantPoolName(signature, start + 1, end - 1);
	}

	if (dimension == 0)
		return binding;
	else
		return createArrayType(binding, dimension);
}
/* Ask the oracle if a package exists named name in the package named compoundName.
*/

boolean isPackage(char[][] compoundName, char[] name) {
	if (compoundName == null || compoundName.length == 0)
		return nameEnvironment.isPackage(null, name);
	else
		return nameEnvironment.isPackage(compoundName, name);
}
// The method verifier is lazily initialized to guarantee the receiver, the compiler & the oracle are ready.

public MethodVerifier methodVerifier() {
	if (verifier == null)
		verifier = new MethodVerifier(this);
	return verifier;
}
public void reset() {
	this.defaultPackage = new PackageBinding(this); // assume the default package always exists
	this.defaultImports = null;
	this.knownPackages = new HashtableOfPackage();

	this.verifier = null;
	for (int i = this.uniqueArrayBindings.length; --i >= 0;)
		this.uniqueArrayBindings[i] = null;
	this.uniqueArrayBindings[0] = new ArrayBinding[50]; // start off the most common 1 dimension array @ 50

	for (int i = this.units.length; --i >= 0;)
		this.units[i] = null;
	this.lastUnitIndex = -1;
	this.lastCompletedUnitIndex = -1;
	
	// name environment has a longer life cycle, and must be reset in
	// the code which created it.
}
void updateArrayCache(UnresolvedReferenceBinding unresolvedType, ReferenceBinding resolvedType) {
	nextDimension : for (int i = 0, length = uniqueArrayBindings.length; i < length; i++) {
		ArrayBinding[] arrayBindings = uniqueArrayBindings[i];
		if (arrayBindings != null) {
			for (int j = 0, max = arrayBindings.length; j < max; j++) {
				ArrayBinding currentBinding = arrayBindings[j];
				if (currentBinding == null)
					continue nextDimension;
				if (currentBinding.leafComponentType == unresolvedType) {
					currentBinding.leafComponentType = resolvedType;
					continue nextDimension;
				}
			}
		}
	}
}
}
