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
package org.eclipse.jdt.internal.core.index;

import java.io.IOException;

/**
 * An <code>IDocument</code> represent a data source, e.g.&nbsp;a <code>File</code> (<code>FileDocument</code>), 
 * an <code>IFile</code> (<code>IFileDocument</code>), 
 * or other kinds of data sources (URL, ...). An <code>IIndexer</code> indexes an<code>IDocument</code>.
 * <br>
 * A document has a set of properties, saved in the index file (so one does not need to open the document
 * to obtain basic information as: date of creation of the document, sum up, ...). A property is a String
 * (called property) associated to a value (String). Example: "date_creation"->"02/08/2000".
 */

public interface IDocument {
	/**
	 * Returns the content of the document, in a byte array.
	 */
	byte[] getByteContent() throws IOException;
	/**
	 * Returns the content of the document, in a char array.
	 */
	char[] getCharContent() throws IOException;
	/**
	 * returns the name of the document (e.g. its path for a <code>File</code>, or its relative path
	 * in the workbench for an <code>IFile</code>).
	 */
	String getName();
	/**
	 * returns the value of the given property, or null if this document does not have
	 * such a property.
	 */
	String getProperty(String property);
	/**
	 * Returns an enumeration of the names of the properties the document has.
	 */
	java.util.Enumeration getPropertyNames();
	/**
	 * Returns the content of the document, as a String.
	 */
	public String getStringContent() throws IOException;
	/**
	 * Returns the type of the document.
	 */
	String getType();
	/**
	 * Sets the given property of the document to the given value.
	 */
	void setProperty(String attribute, String value);
}
