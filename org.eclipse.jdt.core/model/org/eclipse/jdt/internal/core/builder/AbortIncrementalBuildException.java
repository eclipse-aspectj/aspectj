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
package org.eclipse.jdt.internal.core.builder;

/**
 * Exception thrown when an incremental builder cannot find a .class file.
 * Its possible the type can no longer be found because it was renamed inside its existing
 * source file.
 */
public class AbortIncrementalBuildException extends RuntimeException {

public String qualifiedTypeName;  // exposed for AspectJ

public AbortIncrementalBuildException(String qualifiedTypeName) {
	this.qualifiedTypeName = qualifiedTypeName;
}
}