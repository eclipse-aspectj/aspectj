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

public class UnresolvedReferenceBinding extends ReferenceBinding {
	ReferenceBinding resolvedType;
UnresolvedReferenceBinding(char[][] compoundName, PackageBinding packageBinding) {
	this.compoundName = compoundName;
	this.fPackage = packageBinding;
}
String debugName() {
	return toString();
}
ReferenceBinding resolve(LookupEnvironment environment) {
	if (resolvedType != null) return resolvedType;

	ReferenceBinding environmentType = fPackage.getType0(compoundName[compoundName.length - 1]);
	if (environmentType == this)
		environmentType = environment.askForType(compoundName);
	if (environmentType != null && environmentType != this) { // could not resolve any better, error was already reported against it
		resolvedType = environmentType;
		environment.updateArrayCache(this, environmentType);
		return environmentType; // when found, it replaces the unresolved type in the cache
	}

	environment.problemReporter.isClassPathCorrect(compoundName, null);
	return null; // will not get here since the above error aborts the compilation
}
public String toString() {
	return "Unresolved type " + ((compoundName != null) ? CharOperation.toString(compoundName) : "UNNAMED"); //$NON-NLS-1$ //$NON-NLS-2$
}
}
