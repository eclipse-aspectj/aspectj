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
package org.eclipse.jdt.internal.codeassist.complete;

/**
 * Thrown whenever cursor location is not inside a consistent token
 * i.e. inside a string, number, unicode, comments etc...
 */
public class InvalidCursorLocation extends RuntimeException {

	public String irritant;

	/* Possible irritants */
	public static final String NO_COMPLETION_INSIDE_UNICODE = "No Completion Inside Unicode"; //$NON-NLS-1$
	public static final String NO_COMPLETION_INSIDE_COMMENT = "No Completion Inside Comment";      //$NON-NLS-1$
	public static final String NO_COMPLETION_INSIDE_STRING = "No Completion Inside String";        //$NON-NLS-1$
	public static final String NO_COMPLETION_INSIDE_NUMBER = "No Completion Inside Number";        //$NON-NLS-1$
    
public InvalidCursorLocation(String irritant){
	this.irritant = irritant;
}
}
