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
package org.eclipse.jdt.core.util;

/**
 * Description of attribute names as described in the JVM specifications.
 * 
 * This interface is not intended to be implemented by clients.
 * 
 * @since 2.0
 */
public interface IAttributeNamesConstants {
	char[] SYNTHETIC = new char[] {'S', 'y', 'n', 't', 'h', 'e', 't', 'i', 'c'};
	char[] CONSTANT_VALUE = new char[] {'C', 'o', 'n', 's', 't', 'a', 'n', 't', 'V', 'a', 'l', 'u', 'e'};
	char[] LINE_NUMBER= new char[] {'L', 'i', 'n', 'e', 'N', 'u', 'm', 'b', 'e', 'r', 'T', 'a', 'b', 'l', 'e'};
	char[] LOCAL_VARIABLE = new char[] {'L', 'o', 'c', 'a', 'l', 'V', 'a', 'r', 'i', 'a', 'b', 'l', 'e', 'T', 'a', 'b', 'l', 'e'};
	char[] INNER_CLASSES = new char[] {'I', 'n', 'n', 'e', 'r', 'C', 'l', 'a', 's', 's', 'e', 's'};
	char[] CODE = new char[] {'C', 'o', 'd', 'e'};
	char[] EXCEPTIONS = new char[] {'E', 'x', 'c', 'e', 'p', 't', 'i', 'o', 'n', 's'};
	char[] SOURCE = new char[] {'S', 'o', 'u', 'r', 'c', 'e', 'F', 'i', 'l', 'e'};
	char[] DEPRECATED = new char[] {'D', 'e', 'p', 'r', 'e', 'c', 'a', 't', 'e', 'd'};
}
