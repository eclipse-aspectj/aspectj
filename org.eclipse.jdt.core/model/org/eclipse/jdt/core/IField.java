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
package org.eclipse.jdt.core;

/**
 * Represents a field declared in a type.
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 */
public interface IField extends IMember {
/**
 * Returns the constant value associated with this field
 * or <code>null</code> if this field has none.
 * Returns either a subclass of <code>Number</code>, or a <code>String</code>,
 * depending on the type of the field.
 * For example, if the field is of type <code>short</code>, this returns
 * a <code>Short</code>.
 *
 * @return  the constant value associated with this field or <code>null</code> if this field has none.
 * @exception JavaModelException if this element does not exist or if an
 *      exception occurs while accessing its corresponding resource
 */
public Object getConstant() throws JavaModelException;
/**
 * Returns the simple name of this field.
 * @return the simple name of this field.
 */
String getElementName();
/**
 * Returns the type signature of this field.
 *
 * @see Signature
 * @return the type signature of this field.
 * @exception JavaModelException if this element does not exist or if an
 *      exception occurs while accessing its corresponding resource
 */
String getTypeSignature() throws JavaModelException;
}
