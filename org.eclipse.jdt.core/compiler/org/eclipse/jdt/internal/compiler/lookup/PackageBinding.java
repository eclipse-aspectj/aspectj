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

import org.eclipse.jdt.internal.compiler.util.CharOperation;
import org.eclipse.jdt.internal.compiler.util.HashtableOfPackage;
import org.eclipse.jdt.internal.compiler.util.HashtableOfType;

public class PackageBinding extends Binding implements TypeConstants {
	public char[][] compoundName;
	PackageBinding parent;
	LookupEnvironment environment;
	HashtableOfType knownTypes;
	HashtableOfPackage knownPackages;
protected PackageBinding() {
}
public PackageBinding(char[][] compoundName, PackageBinding parent, LookupEnvironment environment) {
	this.compoundName = compoundName;
	this.parent = parent;
	this.environment = environment;
	this.knownTypes = null; // initialized if used... class counts can be very large 300-600
	this.knownPackages = new HashtableOfPackage(3); // sub-package counts are typically 0-3
}
public PackageBinding(char[] topLevelPackageName, LookupEnvironment environment) {
	this(new char[][] {topLevelPackageName}, null, environment);
}
/* Create the default package.
*/

public PackageBinding(LookupEnvironment environment) {
	this(NoCharChar, null, environment);
}
private void addNotFoundPackage(char[] simpleName) {
	knownPackages.put(simpleName, environment.theNotFoundPackage);
}
private void addNotFoundType(char[] simpleName) {
	if (knownTypes == null)
		knownTypes = new HashtableOfType(25);
	knownTypes.put(simpleName, environment.theNotFoundType);
}
void addPackage(PackageBinding element) {
	knownPackages.put(element.compoundName[element.compoundName.length - 1], element);
}
void addType(ReferenceBinding element) {
	if (knownTypes == null)
		knownTypes = new HashtableOfType(25);
	knownTypes.put(element.compoundName[element.compoundName.length - 1], element);
}
/* API
* Answer the receiver's binding type from Binding.BindingID.
*/

public final int bindingType() {
	return PACKAGE;
}
private PackageBinding findPackage(char[] name) {
	if (!environment.isPackage(this.compoundName, name))
		return null;

	char[][] compoundName = CharOperation.arrayConcat(this.compoundName, name);
	PackageBinding newPackageBinding = new PackageBinding(compoundName, this, environment);
	addPackage(newPackageBinding);
	return newPackageBinding;
}
/* Answer the subpackage named name; ask the oracle for the package if its not in the cache.
* Answer null if it could not be resolved.
*
* NOTE: This should only be used when we know there is NOT a type with the same name.
*/

PackageBinding getPackage(char[] name) {
	PackageBinding binding = getPackage0(name);
	if (binding != null) {
		if (binding == environment.theNotFoundPackage)
			return null;
		else
			return binding;
	}
	if ((binding = findPackage(name)) != null)
		return binding;

	// not found so remember a problem package binding in the cache for future lookups
	addNotFoundPackage(name);
	return null;
}
/* Answer the subpackage named name if it exists in the cache.
* Answer theNotFoundPackage if it could not be resolved the first time
* it was looked up, otherwise answer null.
*
* NOTE: Senders must convert theNotFoundPackage into a real problem
* package if its to returned.
*/

PackageBinding getPackage0(char[] name) {
	return knownPackages.get(name);
}
/* Answer the type named name; ask the oracle for the type if its not in the cache.
* Answer a NotVisible problem type if the type is not visible from the invocationPackage.
* Answer null if it could not be resolved.
*
* NOTE: This should only be used by source types/scopes which know there is NOT a
* package with the same name.
*/

ReferenceBinding getType(char[] name) {
	ReferenceBinding binding = getType0(name);
	if (binding == null) {
		if ((binding = environment.askForType(this, name)) == null) {
			// not found so remember a problem type binding in the cache for future lookups
			addNotFoundType(name);
			return null;
		}
	}

	if (binding == environment.theNotFoundType)
		return null;
	if (binding instanceof UnresolvedReferenceBinding)
		binding = ((UnresolvedReferenceBinding) binding).resolve(environment);
	if (binding.isNestedType())
		return new ProblemReferenceBinding(name, InternalNameProvided);
	return binding;
}
/* Answer the type named name if it exists in the cache.
* Answer theNotFoundType if it could not be resolved the first time
* it was looked up, otherwise answer null.
*
* NOTE: Senders must convert theNotFoundType into a real problem
* reference type if its to returned.
*/

ReferenceBinding getType0(char[] name) {
	if (knownTypes == null)
		return null;
	return knownTypes.get(name);
}
/* Answer the package or type named name; ask the oracle if it is not in the cache.
* Answer null if it could not be resolved.
*
* When collisions exist between a type name & a package name, answer the package.
* Treat the type as if it does not exist... a problem was already reported when the type was defined.
*
* NOTE: no visibility checks are performed.
* THIS SHOULD ONLY BE USED BY SOURCE TYPES/SCOPES.
*/

public Binding getTypeOrPackage(char[] name) {
	PackageBinding packageBinding = getPackage0(name);
	if (packageBinding != null && packageBinding != environment.theNotFoundPackage)
		return packageBinding;

	ReferenceBinding typeBinding = getType0(name);
	if (typeBinding != null && typeBinding != environment.theNotFoundType) {
		if (typeBinding instanceof UnresolvedReferenceBinding)
			typeBinding = ((UnresolvedReferenceBinding) typeBinding).resolve(environment);
		if (typeBinding.isNestedType())
			return new ProblemReferenceBinding(name, InternalNameProvided);
		return typeBinding;
	}

	if (typeBinding == null && packageBinding == null) {
		// find the package
		if ((packageBinding = findPackage(name)) != null)
			return packageBinding;

		// if no package was found, find the type named name relative to the receiver
		if ((typeBinding = environment.askForType(this, name)) != null) {
			if (typeBinding.isNestedType())
				return new ProblemReferenceBinding(name, InternalNameProvided);
			return typeBinding;
		}

		// Since name could not be found, add problem bindings
		// to the collections so it will be reported as an error next time.
		addNotFoundPackage(name);
		addNotFoundType(name);
	} else {
		if (packageBinding == environment.theNotFoundPackage)
			packageBinding = null;
		if (typeBinding == environment.theNotFoundType)
			typeBinding = null;
	}

	if (packageBinding != null)
		return packageBinding;
	else
		return typeBinding;
}
public char[] readableName() /*java.lang*/ {
	return CharOperation.concatWith(compoundName, '.');
}
public String toString() {
	if (compoundName == NoCharChar)
		return "The Default Package"; //$NON-NLS-1$
	else
		return "package " + ((compoundName != null) ? CharOperation.toString(compoundName) : "UNNAMED"); //$NON-NLS-1$ //$NON-NLS-2$
}
}
