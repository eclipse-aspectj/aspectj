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
package org.eclipse.jdt.internal.codeassist.complete;

/*
 * Completion node build by the parser in any case it was intending to
 * reduce a explicit constructor call containing the cursor.
 * e.g.
 *
 *	class X {
 *    X() {
 *      this(1, 2, [cursor]
 *    }
 *  }
 *
 *	---> class X {
 *         X() {
 *           <CompleteOnExplicitConstructorCall:this(1, 2)>
 *         }
 *       }
 *
 * The source range is always of length 0.
 * The arguments of the constructor call are all the arguments defined
 * before the cursor.
 */

import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.lookup.*;

public class CompletionOnExplicitConstructorCall extends ExplicitConstructorCall {
public CompletionOnExplicitConstructorCall(int accessMode) {
	super(accessMode);
}
public void resolve(BlockScope scope) {
	ReferenceBinding receiverType = scope.enclosingSourceType();

	if (accessMode != This && receiverType != null) {
		if (receiverType.isHierarchyInconsistent())
			throw new CompletionNodeFound();
		receiverType = receiverType.superclass();
	}
	if (receiverType == null)
		throw new CompletionNodeFound();
	else
		throw new CompletionNodeFound(this, receiverType, scope);
}
public String toString(int tab) {
	String s = tabString(tab);
	s += "<CompleteOnExplicitConstructorCall:"; //$NON-NLS-1$
	if (qualification != null)
		s = s + qualification.toStringExpression() + "."; //$NON-NLS-1$
	if (accessMode == This) {
		s = s + "this("; //$NON-NLS-1$
	} else {
		s = s + "super("; //$NON-NLS-1$
	}
	if (arguments != null) {
		for (int i = 0; i < arguments.length; i++) {
			s += arguments[i].toStringExpression();
			if (i != arguments.length - 1) {
				s += ", "; //$NON-NLS-1$
			}
		};
	}
	s += ")>"; //$NON-NLS-1$
	return s;
}
}
