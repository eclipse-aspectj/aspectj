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
package org.eclipse.jdt.internal.core;

import java.util.ArrayList;

/**
 * Element info for JarPackageFragments.  Caches the zip entry names
 * of the types (.class files) of the JarPackageFragment.  The entries
 * are used to compute the children of the JarPackageFragment.
 */
class JarPackageFragmentInfo extends PackageFragmentInfo {
	/**
	 * The names of the zip entries that are the class files associated
	 * with this package fragment info in the JAR file of the JarPackageFragmentRootInfo.
	 */
	protected ArrayList fEntryNames;
/**
 */
boolean containsJavaResources() {
	return fEntryNames != null && fEntryNames.size() != 0;
}
/**
 * Returns an array of non-java resources contained in the receiver.
 */
Object[] getNonJavaResources() {
	return fNonJavaResources;
}
/**
 * Set the names of the zip entries that are the types associated
 * with this package fragment info in the JAR file of the JarPackageFragmentRootInfo.
 */
protected void setEntryNames(ArrayList entries) {
	fEntryNames = entries;
}
}
