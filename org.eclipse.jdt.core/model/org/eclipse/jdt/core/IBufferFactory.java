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
 * A factory that creates <code>IBuffer</code>s for openables.
 * <p>
 * This interface may be implemented by clients.
 * </p>
 * @since 2.0
 */
public interface IBufferFactory {

	/**
	 * Creates a buffer for the given owner.
	 * The new buffer will be initialized with the contents of the owner 
	 * if and only if it was not already initialized by the factory (a buffer is uninitialized if 
	 * its content is <code>null</code>).
	 * 
	 * @param owner the owner of the buffer
	 * @see IBuffer
	 */
	IBuffer createBuffer(IOpenable owner);
}

