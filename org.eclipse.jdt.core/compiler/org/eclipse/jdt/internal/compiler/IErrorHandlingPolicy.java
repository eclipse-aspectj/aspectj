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

/*
 * Handler policy is responsible to answer the 2 following
 * questions:
 * 1. should the handler stop on first problem which appears
 *	to be a real error (i.e. not a warning),
 * 2. should it proceed once it has gathered all problems
 *
 * The intent is that one can supply its own policy to implement 
 * some interactive error handling strategy where some UI would 
 * display problems and ask user if he wants to proceed or not.
 */

public interface IErrorHandlingPolicy {
	boolean proceedOnErrors();
	boolean stopOnFirstError();
}
