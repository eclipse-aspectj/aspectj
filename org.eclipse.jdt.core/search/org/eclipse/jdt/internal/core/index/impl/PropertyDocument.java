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
package org.eclipse.jdt.internal.core.index.impl;

import java.util.Enumeration;
import java.util.Hashtable;

import org.eclipse.jdt.internal.core.index.IDocument;

/**
 * The properties of a document are stored into a hashtable.
 * @see IDocument
 */

public abstract class PropertyDocument implements IDocument {
	protected Hashtable properties;
	public PropertyDocument() {
		properties= new Hashtable(5);
	}
	/**
	 * @see IDocument#getProperty
	 */
	public String getProperty(String property) {
		return (String) properties.get(property);
	}
	/**
	 * @see IDocument#getPropertyNames
	 */

	public Enumeration getPropertyNames() {
		return properties.keys();
	}
	/**
	 * @see IDocument#setProperty
	 */

	public void setProperty(String property, String value) {
		properties.put(property, value);
	}
}
