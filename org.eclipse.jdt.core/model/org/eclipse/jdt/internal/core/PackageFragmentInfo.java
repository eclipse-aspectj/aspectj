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

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

/**
 * Element info for PackageFragments.
 */
class PackageFragmentInfo extends OpenableElementInfo {

	/**
	 * A array with all the non-java resources contained by this PackageFragment
	 */
	protected Object[] fNonJavaResources;

/**
 * Create and initialize a new instance of the receiver
 */
public PackageFragmentInfo() {
	fNonJavaResources = null;
}
/**
 * Compute the non-java resources of this package fragment.
 *
 * <p>Package fragments which are folders recognize files based on the
 * type of the fragment
 * <p>Package fragments which are in a jar only recognize .class files (
 * @see JarPackageFragment).
 */
private Object[] computeNonJavaResources(IResource resource) {
	Object[] nonJavaResources = new IResource[5];
	int nonJavaResourcesCounter = 0;
	try{
		IResource[] members = ((IContainer) resource).members();
		for (int i = 0, max = members.length; i < max; i++) {
			IResource child = members[i];
			if (child.getType() == IResource.FILE) {
				String fileName = child.getName();
				if (!Util.isValidCompilationUnitName(fileName) && !Util.isValidClassFileName(fileName)) {
					if (nonJavaResources.length == nonJavaResourcesCounter) {
						// resize
						System.arraycopy(
							nonJavaResources,
							0,
							(nonJavaResources = new IResource[nonJavaResourcesCounter * 2]),
							0,
							nonJavaResourcesCounter);
					}
					nonJavaResources[nonJavaResourcesCounter++] = child;
				}
			} else if (child.getType() == IResource.FOLDER) {
				if (!Util.isValidFolderNameForPackage(child.getName())) {
					if (nonJavaResources.length == nonJavaResourcesCounter) {
						// resize
						System.arraycopy(nonJavaResources, 0, (nonJavaResources = new IResource[nonJavaResourcesCounter * 2]), 0, nonJavaResourcesCounter);
					}
					nonJavaResources[nonJavaResourcesCounter++] = child;
				}
			}
		}
		if (nonJavaResourcesCounter == 0) {
			nonJavaResources = NO_NON_JAVA_RESOURCES;
		} else {
			if (nonJavaResources.length != nonJavaResourcesCounter) {
				System.arraycopy(nonJavaResources, 0, (nonJavaResources = new IResource[nonJavaResourcesCounter]), 0, nonJavaResourcesCounter);
			}
		}	
	} catch(CoreException e) {
		nonJavaResources = NO_NON_JAVA_RESOURCES;
		nonJavaResourcesCounter = 0;
	}
	return nonJavaResources;
}
/**
 */
boolean containsJavaResources() {
	return fChildren.length != 0;
}
/**
 * Returns an array of non-java resources contained in the receiver.
 */
Object[] getNonJavaResources(IResource underlyingResource) {
	Object[] nonJavaResources = fNonJavaResources;
	if (nonJavaResources == null) {
		nonJavaResources = computeNonJavaResources(underlyingResource);
		fNonJavaResources = nonJavaResources;
	}
	return nonJavaResources;
}
/**
 * Set the fNonJavaResources to res value
 */
synchronized void setNonJavaResources(Object[] resources) {
	fNonJavaResources = resources;
}
}
