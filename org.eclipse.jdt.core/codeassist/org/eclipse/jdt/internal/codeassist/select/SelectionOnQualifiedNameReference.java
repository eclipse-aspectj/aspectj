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
 * reduce a qualified name reference containing the assist identifier.
 * e.g.
 *
 *	class X {
 *    Y y;
 *    void foo() {
 *      y.fred.[start]ba[end]
 *    }
 *  }
 *
 *	---> class X {
 *         Y y;
 *         void foo() {
 *           <SelectOnName:y.fred.ba>
 *         }
 *       }
 *
 */

import org.eclipse.jdt.internal.compiler.ast.QualifiedNameReference;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.ProblemFieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.ProblemReasons;
import org.eclipse.jdt.internal.compiler.lookup.ProblemReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.util.CharOperation;

public class SelectionOnQualifiedNameReference extends QualifiedNameReference {
	public long[] sourcePositions; // positions of each token, the last one being the positions of the completion identifier
public SelectionOnQualifiedNameReference(char[][] previousIdentifiers, char[] selectionIdentifier, long[] positions) {
	super(
		CharOperation.arrayConcat(previousIdentifiers, selectionIdentifier),
		(int) (positions[0] >>> 32),
		(int) positions[positions.length - 1]);
	this.sourcePositions = positions;
}
public TypeBinding resolveType(BlockScope scope) {
	// it can be a package, type, member type, local variable or field
	binding = scope.getBinding(tokens, this);
	if (!binding.isValidBinding()) {
		if (binding instanceof ProblemFieldBinding) {
			// tolerate some error cases
			if (binding.problemId() == ProblemReasons.NotVisible
					|| binding.problemId() == ProblemReasons.InheritedNameHidesEnclosingName
					|| binding.problemId() == ProblemReasons.NonStaticReferenceInConstructorInvocation
					|| binding.problemId() == ProblemReasons.NonStaticReferenceInStaticContext) {
				throw new SelectionNodeFound(binding);
			}
			scope.problemReporter().invalidField(this, (FieldBinding) binding);
		} else if (binding instanceof ProblemReferenceBinding) {
			// tolerate some error cases
			if (binding.problemId() == ProblemReasons.NotVisible){
				throw new SelectionNodeFound(binding);
			}
			scope.problemReporter().invalidType(this, (TypeBinding) binding);
		} else {
			scope.problemReporter().unresolvableReference(this, binding);
		}
		throw new SelectionNodeFound();
	}
	throw new SelectionNodeFound(binding);
}
public String toStringExpression() {

	StringBuffer buffer = new StringBuffer("<SelectOnName:"); //$NON-NLS-1$
	for (int i = 0, length = tokens.length; i < length; i++) {
		buffer.append(tokens[i]);
		if (i != length - 1)
			buffer.append("."); //$NON-NLS-1$
	}
	buffer.append(">"); //$NON-NLS-1$
	return buffer.toString();
}
}
