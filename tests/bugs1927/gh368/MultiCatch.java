/*******************************************************************************
 * Copyright (c) 2026 Contributors
 * All rights reserved. This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *******************************************************************************/
/** Multi-catch clauses whose catch types have only Throwable in common, so that they share one handler. */
public class MultiCatch {
	public static void main(String[] args) {
		for (int which = 0; which < 2; which++) {
			exceptionFirst(which);
			linkageErrorFirst(which);
			unusedParameter(which);
		}
		for (int which = 0; which < 3; which++) {
			threeTypes(which);
		}
	}

	/** which: 0 throws an Exception, 1 throws a LinkageError */
	static void exceptionFirst(int which) {
		try {
			if (which == 0) {
				throw new IllegalStateException("exception");
			}
			throw new LinkageError("linkage error");
		} catch (Exception | LinkageError e) {
			System.out.println("exceptionFirst caught " + e.getMessage());
		}
	}

	static void linkageErrorFirst(int which) {
		try {
			if (which == 0) {
				throw new IllegalStateException("exception");
			}
			throw new LinkageError("linkage error");
		} catch (LinkageError | Exception e) {
			System.out.println("linkageErrorFirst caught " + e.getMessage());
		}
	}

	/** The compiler starts the handler with a pop and not a store when the catch parameter is not used. */
	static void unusedParameter(int which) {
		try {
			if (which == 0) {
				throw new IllegalStateException("exception");
			}
			throw new LinkageError("linkage error");
		} catch (Exception | LinkageError e) {
			System.out.println("unusedParameter caught " + which);
		}
	}

	/** which: 0 throws an Exception, 1 a LinkageError, 2 an AssertionError */
	static void threeTypes(int which) {
		try {
			if (which == 0) {
				throw new IllegalStateException("exception");
			} else if (which == 1) {
				throw new LinkageError("linkage error");
			}
			throw new AssertionError("assertion error");
		} catch (Exception | LinkageError | AssertionError e) {
			System.out.println("threeTypes caught " + e.getMessage());
		}
	}
}
