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
 * reduce an access to the literal 'class' containing the cursor.
 * e.g.
 *
 *	class X {
 *    void foo() {
 *      String[].[cursor]
 *    }
 *  }
 *
 *	---> class X {
 *         void foo() {
 *           <CompleteOnClassLiteralAccess:String[].>
 *         }
 *       }
 *
 * The source range of the completion node denotes the source range
 * which should be replaced by the completion.
 */

import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.lookup.*;

public class CompletionOnClassLiteralAccess extends ClassLiteralAccess {
	public char[] completionIdentifier;
	public int classStart;
	
public CompletionOnClassLiteralAccess(long pos, TypeReference t) {
	super((int)pos, t);
	this.classStart = (int) (pos >>> 32);
}
public TypeBinding resolveType(BlockScope scope) {
	if (super.resolveType(scope) == null)
		throw new CompletionNodeFound();
	else
		throw new CompletionNodeFound(this, targetType, scope);
}
public String toStringExpression() {
	StringBuffer result = new StringBuffer("<CompleteOnClassLiteralAccess:"); //$NON-NLS-1$
	result.append(type.toString());
	result.append("."); //$NON-NLS-1$
	result.append(completionIdentifier);
	result.append(">"); //$NON-NLS-1$
	return result.toString();
}
}
