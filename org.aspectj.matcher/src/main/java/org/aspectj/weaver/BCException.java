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

import java.io.PrintStream;
import java.io.PrintWriter;

import org.aspectj.bridge.context.CompilationAndWeavingContext;

/**
 * Exception to use inside the bcweaver.
 */
@SuppressWarnings("serial")
public class BCException extends RuntimeException {
	Throwable thrown;

	public BCException() {
		super();
	}

	public BCException(String s) {
		super(s + "\n" + CompilationAndWeavingContext.getCurrentContext());
	}

	public BCException(String s, Throwable thrown) {
		this(s);
		this.thrown = thrown;
	}

	// TODO: Is it really necessary to re-invent stack trace printing here? Can these methods simply go away?
	// The only doubtful "benefit" is that the causing exception's stack trace is printed fully instead of shortened.
	// But OTOH, the JVM just omits the lines which were present in the original stack trace above already, so there is
	// really no extra information here.

	public void printStackTrace() {
		printStackTrace(System.err);
	}

	public void printStackTrace(PrintStream s) {
		printStackTrace(new PrintWriter(s));
	}

	public void printStackTrace(PrintWriter s) {
		super.printStackTrace(s);
		if (null != thrown) {
			s.print("Caused by: ");
			thrown.printStackTrace(s);
		}
		// Flush PrintWriter in case the JVM exits before the stack trace was printed. Otherwise, when e.g. calling
		// UnresolvedType.signatureToName from a main method or a test directly and a BCException is thrown, nothing but
		//   Exception in thread "main"
		// would be printed without flushing the PrintWriter, because the JVM exits immediately.
		s.flush();
	}

}
