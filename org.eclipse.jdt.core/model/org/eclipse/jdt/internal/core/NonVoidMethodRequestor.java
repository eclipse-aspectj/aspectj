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
package org.eclipse.jdt.internal.core;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.internal.codeassist.ISearchRequestor;

/**
 *	This class modifies the <code>SearchableEnvironmentRequestor</code>'s 
 *	functionality by only accepting methods with return types that are not void.
 */
public class NonVoidMethodRequestor extends SearchableEnvironmentRequestor {
/**
 * NonVoidMethodRequestor constructor comment.
 * @param requestor org.eclipse.jdt.internal.codeassist.ISearchRequestor
 */
public NonVoidMethodRequestor(ISearchRequestor requestor) {
	super(requestor);
}
public void acceptMethod(IMethod method) {
	try {
		if (!Signature.getReturnType(method.getSignature()).equals("V")) { //$NON-NLS-1$
			super.acceptMethod(method);
		}
	} catch (JavaModelException npe) {
	}
}
}
