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
 * reduce a type reference containing the completion identifier as part
 * of a qualified name.
 * e.g.
 *
 *	class X extends java.lang.[start]Object[end]
 *
 *	---> class X extends <SelectOnType:java.lang.Object>
 *
 */
 
import org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.ProblemReasons;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.util.CharOperation;

public class SelectionOnQualifiedTypeReference extends QualifiedTypeReference {
public SelectionOnQualifiedTypeReference(char[][] previousIdentifiers, char[] selectionIdentifier, long[] positions) {
	super(
		CharOperation.arrayConcat(previousIdentifiers, selectionIdentifier),
		positions);
}
public void aboutToResolve(Scope scope) {
	getTypeBinding(scope.parent); // step up from the ClassScope
}
public TypeBinding getTypeBinding(Scope scope) {
	// it can be a package, type or member type
	Binding binding = scope.getTypeOrPackage(tokens);
	if (!binding.isValidBinding()) {
			// tolerate some error cases
			if (binding.problemId() == ProblemReasons.NotVisible){
				throw new SelectionNodeFound(binding);
			}
		scope.problemReporter().invalidType(this, (TypeBinding) binding);
		throw new SelectionNodeFound();
	}

	throw new SelectionNodeFound(binding);
}
public String toStringExpression(int tab) {

	StringBuffer buffer = new StringBuffer();
	buffer.append("<SelectOnType:"); //$NON-NLS-1$
	for (int i = 0, length = tokens.length; i < length; i++) {
		buffer.append(tokens[i]);
		if (i != length - 1)
			buffer.append("."); //$NON-NLS-1$
	}
	buffer.append(">"); //$NON-NLS-1$
	return buffer.toString();
}
}
