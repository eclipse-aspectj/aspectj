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
package org.eclipse.jdt.internal.compiler;

public class DefaultErrorHandlingPolicies {
 
/*
 * Accumulate all problems, then exit without proceeding.
 *
 * Typically, the #proceedWithProblems(Problem[]) should
 * show the problems.
 *
 */
public static IErrorHandlingPolicy exitAfterAllProblems() {
	return new IErrorHandlingPolicy() {
		public boolean stopOnFirstError() {
			return false;
		}
		public boolean proceedOnErrors(){
			return false;
		}
	};
}
/*
 * Exit without proceeding on the first problem wich appears
 * to be an error.
 *
 */
public static IErrorHandlingPolicy exitOnFirstError() {
	return new IErrorHandlingPolicy() {
		public boolean stopOnFirstError() {
			return true;
		}
		public boolean proceedOnErrors(){
			return false;
		}
	};
}
/*
 * Proceed on the first error met.
 *
 */
public static IErrorHandlingPolicy proceedOnFirstError() {
	return new IErrorHandlingPolicy() {
		public boolean stopOnFirstError() {
			return true;
		}
		public boolean proceedOnErrors(){
			return true;
		}
	};
}
/*
 * Accumulate all problems, then proceed with them.
 *
 */
public static IErrorHandlingPolicy proceedWithAllProblems() {
	return new IErrorHandlingPolicy() {
		public boolean stopOnFirstError() {
			return false;
		}
		public boolean proceedOnErrors(){
			return true;
		}
	};
}
}
