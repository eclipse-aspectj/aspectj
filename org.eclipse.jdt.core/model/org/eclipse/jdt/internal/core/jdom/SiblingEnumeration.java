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

import java.util.Enumeration;

import org.eclipse.jdt.core.jdom.IDOMNode;

/**
 * SiblingEnumeration provides an enumeration on a linked list
 * of sibling DOM nodes.
 *
 * @see java.util.Enumeration
 */

/* package */ class SiblingEnumeration implements Enumeration {

	/**
	 * The current location in the linked list
	 * of DOM nodes.
	 */
	protected IDOMNode fCurrentElement;
/**
 * Creates an enumeration of silbings starting at the given node.
 * If the given node is <code>null</code> the enumeration is empty.
 */
SiblingEnumeration(IDOMNode child) {
	fCurrentElement= child;
}
/**
 * @see java.util.Enumeration#hasMoreElements()
 */
public boolean hasMoreElements() {
	return fCurrentElement != null;
}
/**
 * @see java.util.Enumeration#nextElement()
 */
public Object nextElement() {
	IDOMNode curr=  fCurrentElement;
	if (curr != null) {
		fCurrentElement= fCurrentElement.getNextNode();
	}
	return curr;
}
}
