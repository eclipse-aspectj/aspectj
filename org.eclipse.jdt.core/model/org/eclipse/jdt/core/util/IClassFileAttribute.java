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
 * This class represents a generic class file attribute. It is intended to be extended
 * for any new attribute.
 * 
 * @since 2.0
 */
public interface IClassFileAttribute {
	
	/**
	 * Answer back the attribute name index in the constant pool as specified
	 * in the JVM specifications.
	 * 
	 * @return the attribute name index in the constant pool
	 */
	int getAttributeNameIndex();

	/**
	 * Answer back the attribute name as specified
	 * in the JVM specifications.
	 * 
	 * @return the attribute name
	 */
	char[] getAttributeName();

	/**
	 * Answer back the attribute length as specified
	 * in the JVM specifications.
	 * 
	 * @return the attribute length
	 */
	long getAttributeLength();
}
