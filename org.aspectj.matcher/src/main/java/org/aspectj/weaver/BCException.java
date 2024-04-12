/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors:
 *     PARC     initial implementation
 * ******************************************************************/

package org.aspectj.weaver;

import org.aspectj.bridge.context.CompilationAndWeavingContext;

/**
 * Exception to use inside the bcweaver.
 */
public class BCException extends RuntimeException {
	public BCException() {
    super();
  }

	public BCException(String message) {
		super(message + CompilationAndWeavingContext.getCurrentContext(true));
	}

	public BCException(String message, Throwable cause) {
		super(message + CompilationAndWeavingContext.getCurrentContext(true), cause);
	}
}
