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

import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.env.ISourceType;
import org.eclipse.jdt.internal.compiler.lookup.PackageBinding;

public interface ITypeRequestor {
	
	/**
	 * Accept the resolved binary form for the requested type.
	 */
	void accept(IBinaryType binaryType, PackageBinding packageBinding);

	/**
	 * Accept the requested type's compilation unit.
	 */
	void accept(ICompilationUnit unit);

	/**
	 * Accept the unresolved source forms for the requested type.
	 * Note that the multiple source forms can be answered, in case the target compilation unit
	 * contains multiple types. The first one is then guaranteed to be the one corresponding to the
	 * requested type.
	 */
	void accept(ISourceType[] sourceType, PackageBinding packageBinding);
}