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
package org.eclipse.jdt.core.jdom;

/**
 * Unchecked exception thrown when an illegal manipulation of the JDOM is 
 * performed, or when an attempt is made to access/set an attribute of a
 * JDOM node that source indexes cannot be determined for (in case the source
 * was syntactically incorrect).
 */
public class DOMException extends RuntimeException {
/**
 * Creates a new exception with no detail message.
 */
public DOMException() {}
/**
 * Creates a new exception with the given detail message.
 *
 * @param message the detail message
 */
public DOMException(String message) {
	super(message);
}
}
