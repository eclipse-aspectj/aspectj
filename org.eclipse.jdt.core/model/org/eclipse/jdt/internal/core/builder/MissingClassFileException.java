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
package org.eclipse.jdt.internal.core.builder;

/**
 * Exception thrown when the build should be aborted because a referenced
 * class file cannot be found.
 */
public class MissingClassFileException extends RuntimeException {

protected String missingClassFile;

public MissingClassFileException(String missingClassFile) {
	this.missingClassFile = missingClassFile;
}
}