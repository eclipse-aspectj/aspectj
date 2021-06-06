/* *******************************************************************
 * Copyright (c) 1999-2001 Xerox Corporation,
 *               2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors:
 *     Xerox/PARC     initial implementation
 * ******************************************************************/


package org.aspectj.lang;

/**
 * Thrown by the <code>aspectOf</code> special method on aspect types
 *  when there is no aspect of that type currently bound.
 */
public class NoAspectBoundException extends RuntimeException {
	Throwable cause;
	public NoAspectBoundException(String aspectName, Throwable inner) {
		super(inner == null ? aspectName :
			"Exception while initializing " +aspectName + ": " +inner);
		this.cause = inner;
	}

	public NoAspectBoundException() {
	}

	public Throwable getCause() { return cause; }

}
