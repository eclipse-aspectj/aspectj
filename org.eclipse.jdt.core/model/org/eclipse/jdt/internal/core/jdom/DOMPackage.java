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
import org.eclipse.jdt.core.jdom.IDOMNode;
import org.eclipse.jdt.core.jdom.IDOMPackage;
import org.eclipse.jdt.internal.compiler.util.Util;
import org.eclipse.jdt.internal.core.util.CharArrayBuffer;

/**
 * DOMPackage provides an implementation of IDOMPackage.
 *
 * @see IDOMPackage
 * @see DOMNode
 */
class DOMPackage extends DOMNode implements IDOMPackage {

/**
 * Creates an empty PACKAGE node.
 */
DOMPackage() {
	setMask(MASK_DETAILED_SOURCE_INDEXES, true);
}
/**
 * Creates a new simple PACKAGE document fragment on the given range of the document.
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
 */
DOMPackage(char[] document, int[] sourceRange, String name) {
	super(document, sourceRange, name, new int[] {-1, -1});
	setMask(MASK_DETAILED_SOURCE_INDEXES, false);
}
/**
 * Creates a new detailed PACKAGE document fragment on the given range of the document.
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
 */
DOMPackage(char[] document, int[] sourceRange, String name, int[] nameRange) {
	super(document, sourceRange, name, nameRange);
	setMask(MASK_DETAILED_SOURCE_INDEXES, true);
}
/**
 * @see DOMNode#appendFragmentedContents(CharArrayBuffer)
 */
protected void appendFragmentedContents(CharArrayBuffer buffer) {
	if (fNameRange[0] < 0) {
		buffer
			.append("package ") //$NON-NLS-1$
			.append(fName)
			.append(';')
			.append(Util.LINE_SEPARATOR)
			.append(Util.LINE_SEPARATOR);
	} else {
		buffer
			.append(fDocument, fSourceRange[0], fNameRange[0] - fSourceRange[0])
			.append(fName)
			.append(fDocument, fNameRange[1] + 1, fSourceRange[1] - fNameRange[1]);
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
	return (DOMNode)getFactory().createPackage(getContents());
}
/**
 * @see IDOMNode#getJavaElement
 */
public IJavaElement getJavaElement(IJavaElement parent) throws IllegalArgumentException {
	if (parent.getElementType() == IJavaElement.COMPILATION_UNIT) {
		return ((ICompilationUnit)parent).getPackageDeclaration(getName());
	} else {
		throw new IllegalArgumentException(Util.bind("element.illegalParent")); //$NON-NLS-1$
	}
}
/**
 * @see IDOMNode#getNodeType()
 */
public int getNodeType() {
	return IDOMNode.PACKAGE;
}
/**
 * @see DOMNode
 */
protected DOMNode newDOMNode() {
	return new DOMPackage();
}
/**
 * @see IDOMNode#setName
 */
public void setName(String name) {
	becomeDetailed();
	super.setName(name);
}
/**
 * @see IDOMNode#toString()
 */
public String toString() {
	return "PACKAGE: " + getName(); //$NON-NLS-1$
}
}
