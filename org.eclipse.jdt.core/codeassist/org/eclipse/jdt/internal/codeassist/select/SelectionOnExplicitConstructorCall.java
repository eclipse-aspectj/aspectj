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
package org.eclipse.jdt.internal.codeassist.select;

/*
 * Selection node build by the parser in any case it was intending to
 * reduce an explicit constructor call containing the cursor.
 * e.g.
 *
 *	class X {
 *    void foo() {
 *      Y.[start]super[end](1, 2)
 *    }
 *  }
 *
 *	---> class X {
 *         void foo() {
 *           <SelectOnExplicitConstructorCall:Y.super(1, 2)>
 *         }
 *       }
 *
 */

import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.lookup.*;

public class SelectionOnExplicitConstructorCall extends ExplicitConstructorCall {
public SelectionOnExplicitConstructorCall(int accessMode) {
	super(accessMode);
}
public void resolve(BlockScope scope) {
	super.resolve(scope);

	// tolerate some error cases
	if (binding == null || 
			!(binding.isValidBinding() ||
				binding.problemId() == ProblemReasons.NotVisible))
		throw new SelectionNodeFound();
	else
		throw new SelectionNodeFound(binding);
}
public String toString(int tab) {
	String s = tabString(tab);
	s += "<SelectOnExplicitConstructorCall:"; //$NON-NLS-1$
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
