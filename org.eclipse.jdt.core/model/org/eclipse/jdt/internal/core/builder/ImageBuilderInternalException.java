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
package org.eclipse.jdt.internal.core.builder;

import org.eclipse.core.runtime.CoreException;

/**
 * Exception thrown when there is an internal error in the image builder.
 * May wrapper another exception.
 */
public class ImageBuilderInternalException extends RuntimeException {

protected CoreException coreException;

public ImageBuilderInternalException(CoreException e) {
	this.coreException = e;
}

public CoreException getThrowable() {
	return coreException;
}

public void printStackTrace() {
	if (coreException != null) {
		System.err.println(this);
		System.err.println("Stack trace of embedded core exception:"); //$NON-NLS-1$
		coreException.printStackTrace();
	} else {
		super.printStackTrace();
	}
}
}