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
package org.eclipse.jdt.internal.core.util;

/**
 * An adapter which implements the methods for handling
 * reference information from the parser.
 */
public abstract class ReferenceInfoAdapter {
/**
 * Does nothing.
 */
public void acceptConstructorReference(char[] typeName, int argCount, int sourcePosition) {}
/**
 * Does nothing.
 */
public void acceptFieldReference(char[] fieldName, int sourcePosition) {}
/**
 * Does nothing.
 */
public void acceptMethodReference(char[] methodName, int argCount, int sourcePosition) {}
/**
 * Does nothing.
 */
public void acceptTypeReference(char[][] typeName, int sourceStart, int sourceEnd) {}
/**
 * Does nothing.
 */
public void acceptTypeReference(char[] typeName, int sourcePosition) {}
/**
 * Does nothing.
 */
public void acceptUnknownReference(char[][] name, int sourceStart, int sourceEnd) {}
/**
 * Does nothing.
 */
public void acceptUnknownReference(char[] name, int sourcePosition) {}
}
