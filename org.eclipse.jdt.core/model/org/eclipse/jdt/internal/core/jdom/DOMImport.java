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
package org.eclipse.jdt.internal.core.jdom;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.jdom.IDOMImport;
import org.eclipse.jdt.core.jdom.IDOMNode;
import org.eclipse.jdt.internal.compiler.util.Util;
import org.eclipse.jdt.internal.core.util.CharArrayBuffer;

/**
 * DOMImport provides an implementation of IDOMImport.
 *
 * @see IDOMImport
 * @see DOMNode
 */
class DOMImport extends DOMNode implements IDOMImport {
	/**
	 * Indicates if this import is an on demand type import
	 */
	protected boolean fOnDemand;
/**
 * Creates a new empty IMPORT node.
 */
DOMImport() {
	fName = "java.lang.*"; //$NON-NLS-1$
	setMask(MASK_DETAILED_SOURCE_INDEXES, true);
}
/**
 * Creates a new detailed IMPORT document fragment on the given range of the document.
 *
 * @param document - the document containing this node's original contents
 * @param sourceRange - a two element array of integers describing the
 *		entire inclusive source range of this node within its document.
 * 		Contents start on and include the character at the first position.
 *		Contents end on and include the character at the last position.
 *		An array of -1's indicates this node's contents do not exist
 *		in the document.
 * @param name - the identifier portion of the name of this node, or
 *		<code>null</code> if this node does not have a name
 * @param nameRange - a two element array of integers describing the
 *		entire inclusive source range of this node's name within its document,
 *		including any array qualifiers that might immediately follow the name
 *		or -1's if this node does not have a name.
 * @param onDemand - indicates if this import is an on demand style import
 */
DOMImport(char[] document, int[] sourceRange, String name, int[] nameRange, boolean onDemand) {
	super(document, sourceRange, name, nameRange);
	fOnDemand = onDemand;
	setMask(MASK_DETAILED_SOURCE_INDEXES, true);
}
/**
 * Creates a new simple IMPORT document fragment on the given range of the document.
 *
 * @param document - the document containing this node's original contents
 * @param sourceRange - a two element array of integers describing the
 *		entire inclusive source range of this node within its document.
 * 		Contents start on and include the character at the first position.
 *		Contents end on and include the character at the last position.
 *		An array of -1's indicates this node's contents do not exist
 *		in the document.
 * @param name - the identifier portion of the name of this node, or
 *		<code>null</code> if this node does not have a name
 * @param onDemand - indicates if this import is an on demand style import
 */
DOMImport(char[] document, int[] sourceRange, String name, boolean onDemand) {
	this(document, sourceRange, name, new int[] {-1, -1}, onDemand);
	fOnDemand = onDemand;
	setMask(MASK_DETAILED_SOURCE_INDEXES, false);
}
/**
 * @see DOMNode#appendFragmentedContents(CharArrayBuffer)
 */
protected void appendFragmentedContents(CharArrayBuffer buffer) {
	if (fNameRange[0] < 0) {
		buffer
			.append("import ") //$NON-NLS-1$
			.append(fName)
			.append(';')
			.append(Util.LINE_SEPARATOR);
	} else {
		buffer.append(fDocument, fSourceRange[0], fNameRange[0] - fSourceRange[0]);
		//buffer.append(fDocument, fNameRange[0], fNameRange[1] - fNameRange[0] + 1);
		buffer.append(fName);
		buffer.append(fDocument, fNameRange[1] + 1, fSourceRange[1] - fNameRange[1]);
	}
}
/** 
 * @see IDOMNode#getContents()
 */
public String getContents() {
	if (fName == null) {
		return null;
	} else {
		return super.getContents();
	}
}
/**
 * @see DOMNode#getDetailedNode()
 */
protected DOMNode getDetailedNode() {
	return (DOMNode)getFactory().createImport(getContents());
}
/**
 * @see IDOMNode#getJavaElement
 */
public IJavaElement getJavaElement(IJavaElement parent) throws IllegalArgumentException {
	if (parent.getElementType() == IJavaElement.COMPILATION_UNIT) {
		return ((ICompilationUnit)parent).getImport(getName());
	} else {
		throw new IllegalArgumentException(Util.bind("element.illegalParent")); //$NON-NLS-1$
	}
}
/**
 * @see IDOMNode#getNodeType()
 */
public int getNodeType() {
	return IDOMNode.IMPORT;
}
/**
 * @see IDOMImport#isOnDemand()
 */
public boolean isOnDemand() {
	return fOnDemand;	
}
/**
 * @see DOMNode
 */
protected DOMNode newDOMNode() {
	return new DOMImport();
}
/**
 * @see IDOMNode#setName(char[])
 */
public void setName(String name) {
	if (name == null) {
		throw new IllegalArgumentException(Util.bind("element.nullName")); //$NON-NLS-1$
	}
	becomeDetailed();
	super.setName(name);
	fOnDemand = name.endsWith(".*"); //$NON-NLS-1$
}
/**
 * @see IDOMNode#toString()
 */
public String toString() {
	return "IMPORT: " + getName(); //$NON-NLS-1$
}
}
