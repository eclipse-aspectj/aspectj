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
 * reduce a type reference containing the selection identifier as a single
 * name reference.
 * e.g.
 *
 *	class X extends [start]Object[end]
 *
 *	---> class X extends <SelectOnType:Object>
 *
 */
 
import org.eclipse.jdt.internal.compiler.ast.SingleTypeReference;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ProblemReasons;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

public class SelectionOnSingleTypeReference extends SingleTypeReference {
public SelectionOnSingleTypeReference(char[] source, long pos) {
	super(source, pos);
}
public void aboutToResolve(Scope scope) {
	getTypeBinding(scope.parent); // step up from the ClassScope
}
public TypeBinding getTypeBinding(Scope scope) {
	// it can be a package, type or member type
	Binding binding = scope.getTypeOrPackage(new char[][] {token});
	if (!binding.isValidBinding()) {
		scope.problemReporter().invalidType(this, (TypeBinding) binding);
		throw new SelectionNodeFound();
	}

	throw new SelectionNodeFound(binding);
}
public TypeBinding resolveTypeEnclosing(BlockScope scope, ReferenceBinding enclosingType) {
	super.resolveTypeEnclosing(scope, enclosingType);

		// tolerate some error cases
		if (binding == null || 
				!(binding.isValidBinding() || 
					binding.problemId() == ProblemReasons.NotVisible))
		throw new SelectionNodeFound();
	else
		throw new SelectionNodeFound(binding);
}
public String toStringExpression(int tab){

	return "<SelectOnType:" + new String(token) + ">" ; //$NON-NLS-2$ //$NON-NLS-1$
}
}
