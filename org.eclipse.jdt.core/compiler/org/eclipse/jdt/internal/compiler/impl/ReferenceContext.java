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
package org.eclipse.jdt.internal.compiler.impl;
/*
 * Implementors are valid compilation contexts from which we can
 * escape in case of error:
 *	i.e. method | type | compilation unit
 */

import org.eclipse.jdt.internal.compiler.CompilationResult;

public interface ReferenceContext {
	void abort(int abortLevel);
	CompilationResult compilationResult();
	void tagAsHavingErrors();
	boolean hasErrors();
}
