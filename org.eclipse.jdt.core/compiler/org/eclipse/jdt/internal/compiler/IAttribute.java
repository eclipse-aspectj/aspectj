/*******************************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC) 
 * and others.  All rights reserved. 
 * This program and the accompanying materials are made available under
 * the terms of the Common Public License v1.0 which accompanies this
 * distribution, available at http://www.eclipse.org/legal/cpl-v1.0.html
 * 
 * Contributors:
 *     PARC       - initial API and implementation
 ******************************************************************************/

package org.eclipse.jdt.internal.compiler;

/**
 * Represents an Attribute for a Java .class file.
 */
public interface IAttribute {

	/**
	 * Returns the name of the attribute.
	 */
	char[] getNameChars();

	/**
	 * @param nameIndex the index into this class's constant pool for this
	 *         attribute's name.
	 * 
	 * @return all of the bytes to represent this attribute in the .class file.
	 */
	byte[] getAllBytes(short nameIndex);

}
