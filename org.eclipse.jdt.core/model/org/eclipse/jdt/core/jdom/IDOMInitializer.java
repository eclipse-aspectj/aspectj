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
package org.eclipse.jdt.core.jdom;

/**
 * Represents an initializer. The corresponding syntactic
 * units are InstanceInitializer (JLS2 8.6) and StaticDeclaration (JLS2 8.7).
 * An initializer has no children and its parent is a type.
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 */
public interface IDOMInitializer extends IDOMMember {
/**
 * Returns the body of this initializer. The syntax for a body corresponds to
 * InstanceInitializer (JLS2 8.6) and StaticDeclaration (JLS2 8.7).
 *
 * @return an initializer body, including braces, or <code>null</code> if
 *   no body is present
 */
public String getBody();
/**
 * The <code>IDOMInitializer</code> refinement of this <code>IDOMNode</code>
 * method returns <code>null</code>. An initializer does not have a name.
 */
public String getName();
/**
 * Sets the body of this initializer. The syntax for a body corresponds to
 * InstanceInitializer (JLS2 8.6) and StaticDeclaration (JLS2 8.7). No formatting
 * or syntax checking is performed on the body. Braces <b>must</b> be included.
 *
 * @param body an initializer body, including braces, or <code>null</code> 
 *   indicating no body
 */
public void setBody(String body);
/**
 * The <code>IDOMInitializer</code> refinement of this <code>IDOMNode</code>
 * method does nothing.
 */
public void setName(String name);
}
