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
 * reduce a qualified name reference containing the completion identifier.
 * e.g.
 *
 *	class X {
 *    Y y;
 *    void foo() {
 *      y.fred.ba[cursor]
 *    }
 *  }
 *
 *	---> class X {
 *         Y y;
 *         void foo() {
 *           <CompleteOnName:y.fred.ba>
 *         }
 *       }
 *
 * The source range of the completion node denotes the source range
 * which should be replaced by the completion.
 */

import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.lookup.*;

public class CompletionOnQualifiedNameReference extends QualifiedNameReference {
	public char[] completionIdentifier;
	public long[] sourcePositions; // positions of each token, the last one being the positions of the completion identifier
public CompletionOnQualifiedNameReference(char[][] previousIdentifiers, char[] completionIdentifier, long[] positions) {
	super(previousIdentifiers, (int) (positions[0] >>> 32), (int) positions[positions.length - 1]);
	this.completionIdentifier = completionIdentifier;
	this.sourcePositions = positions;
}
public CompletionOnQualifiedNameReference(char[][] previousIdentifiers, char[] completionIdentifier, int sourceStart, int sourceEnd) {
	super(previousIdentifiers, sourceStart, sourceEnd);
	this.completionIdentifier = completionIdentifier;
	this.sourcePositions = new long[] {((long)sourceStart << 32) + sourceEnd};
}
public TypeBinding resolveType(BlockScope scope) {
	// it can be a package, type, member type, local variable or field
	binding = scope.getBinding(tokens, this);
	if (!binding.isValidBinding()) {
		if (binding instanceof ProblemFieldBinding) {
			scope.problemReporter().invalidField(this, (FieldBinding) binding);
		} else if (binding instanceof ProblemReferenceBinding) {
			scope.problemReporter().invalidType(this, (TypeBinding) binding);
		} else {
			scope.problemReporter().unresolvableReference(this, binding);
		}
		throw new CompletionNodeFound();
	}

	throw new CompletionNodeFound(this, binding, scope);
}
public String toStringExpression() {

	StringBuffer buffer = new StringBuffer("<CompleteOnName:"); //$NON-NLS-1$
	for (int i = 0; i < tokens.length; i++) {
		buffer.append(tokens[i]);
		buffer.append("."); //$NON-NLS-1$
	}
	buffer.append(completionIdentifier).append(">"); //$NON-NLS-1$
	return buffer.toString();
}
}
