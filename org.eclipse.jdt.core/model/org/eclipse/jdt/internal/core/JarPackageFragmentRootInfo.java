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

/**
 * The element info for <code>JarPackageFragmentRoot</code>s.
 */
class JarPackageFragmentRootInfo extends PackageFragmentRootInfo {
	/**
	 * The SourceMapper for this JAR (or <code>null</code> if
	 * this JAR does not have source attached).
	 */
	protected SourceMapper fSourceMapper= null;
/**
 * Returns an array of non-java resources contained in the receiver.
 */
public Object[] getNonJavaResources() {
	fNonJavaResources = NO_NON_JAVA_RESOURCES;
	return fNonJavaResources;
}
/**
 * Retuns the SourceMapper for this JAR, or <code>null</code>
 * if this JAR does not have attached source.
 */
protected SourceMapper getSourceMapper() {
	return fSourceMapper;
}
/**
 * Sets the SourceMapper for this JAR.
 */
protected void setSourceMapper(SourceMapper mapper) {
	fSourceMapper= mapper;
}
}
