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
package org.eclipse.jdt.internal.core;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.jdom.DOMFactory;
import org.eclipse.jdt.core.jdom.IDOMInitializer;
import org.eclipse.jdt.core.jdom.IDOMNode;

/**
 * <p>This operation creates a initializer in a type.
 *
 * <p>Required Attributes:<ul>
 *  <li>Containing Type
 *  <li>The source code for the initializer. No verification of the source is
 *      performed.
 * </ul>
 */
public class CreateInitializerOperation extends CreateTypeMemberOperation {
	/**
	 * The current number of initializers in the parent type.
	 * Used to retrieve the handle of the newly created initializer.
	 */
	protected int fNumberOfInitializers= 1;
/**
 * When executed, this operation will create an initializer with the given name
 * in the given type with the specified source.
 *
 * <p>By default the new initializer is positioned after the last existing initializer
 * declaration, or as the first member in the type if there are no
 * initializers.
 */
public CreateInitializerOperation(IType parentElement, String source) {
	super(parentElement, source, false);
}
/**
 * @see CreateTypeMemberOperation#generateElementDOM
 */
protected IDOMNode generateElementDOM() throws JavaModelException {
	IDOMInitializer domInitializer = (new DOMFactory()).createInitializer(fSource);
	if (domInitializer == null) {
		IDOMNode node = generateSyntaxIncorrectDOM();
		if (!(node instanceof IDOMInitializer)) {
			return null;
		}
		domInitializer = (IDOMInitializer) node;
	}
	return domInitializer;
}
/**
 * @see CreateElementInCUOperation#generateResultHandle
 */
protected IJavaElement generateResultHandle() {
	try {
		//update the children to be current
		getType().getCompilationUnit().close();
		if (fAnchorElement == null) {
			return getType().getInitializer(fNumberOfInitializers);
		} else {
			IJavaElement[] children = getType().getChildren();
			int count = 0;
			for (int i = 0; i < children.length; i++) {
				IJavaElement child = children[i];
				if (child.equals(fAnchorElement)) {
					if (child .getElementType() == IJavaElement.INITIALIZER && fInsertionPolicy == CreateElementInCUOperation.INSERT_AFTER) {
						count++;
					}
					return getType().getInitializer(count);
				} else
					if (child.getElementType() == IJavaElement.INITIALIZER) {
						count++;
					}
			}
		}
	} catch (JavaModelException jme) {
	}
	return null;
}
/**
 * @see CreateElementInCUOperation#getMainTaskName()
 */
public String getMainTaskName(){
	return Util.bind("operation.createInitializerProgress"); //$NON-NLS-1$
}
/**
 * By default the new initializer is positioned after the last existing initializer
 * declaration, or as the first member in the type if there are no
 * initializers.
 */
protected void initializeDefaultPosition() {
	IType parentElement = getType();
	try {
		IJavaElement[] elements = parentElement.getInitializers();
		if (elements != null && elements.length > 0) {
			fNumberOfInitializers= elements.length;
			createAfter(elements[elements.length - 1]);
		} else {
			elements = parentElement.getChildren();
			if (elements != null && elements.length > 0) {
				createBefore(elements[0]);
			}
		}
	} catch (JavaModelException e) {
	}
}
}
