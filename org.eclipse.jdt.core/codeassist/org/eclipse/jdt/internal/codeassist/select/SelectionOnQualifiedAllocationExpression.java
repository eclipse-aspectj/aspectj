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
 * reduce an allocation expression containing the cursor.
 * If the allocation expression is not qualified, the enclosingInstance field
 * is null.
 * e.g.
 *
 *	class X {
 *    void foo() {
 *      new [start]Bar[end](1, 2)
 *    }
 *  }
 *
 *	---> class X {
 *         void foo() {
 *           <SelectOnAllocationExpression:new Bar(1, 2)>
 *         }
 *       }
 *
 */

import org.eclipse.jdt.internal.compiler.ast.AnonymousLocalTypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ConstructorDeclaration;
import org.eclipse.jdt.internal.compiler.ast.QualifiedAllocationExpression;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ProblemReasons;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
 
public class SelectionOnQualifiedAllocationExpression extends QualifiedAllocationExpression {
public SelectionOnQualifiedAllocationExpression() {
}
public SelectionOnQualifiedAllocationExpression(AnonymousLocalTypeDeclaration anonymous) {
	anonymousType = anonymous ;
}
public TypeBinding resolveType(BlockScope scope) {
	super.resolveType(scope);

	// tolerate some error cases
	if (binding == null || 
			!(binding.isValidBinding() || 
				binding.problemId() == ProblemReasons.NotVisible))
		throw new SelectionNodeFound();
	if (anonymousType == null)
		throw new SelectionNodeFound(binding);

	// if selecting a type for an anonymous type creation, we have to
	// find its target super constructor (if extending a class) or its target 
	// super interface (if extending an interface)
	if (anonymousType.binding.superInterfaces == NoSuperInterfaces) {
		// find the constructor binding inside the super constructor call
		ConstructorDeclaration constructor = (ConstructorDeclaration) anonymousType.declarationOf(binding);
		throw new SelectionNodeFound(constructor.constructorCall.binding);
	} else {
		// open on the only superinterface
		throw new SelectionNodeFound(anonymousType.binding.superInterfaces[0]);
	}
}
public String toStringExpression(int tab) {
	return 
		((this.enclosingInstance == null) ? 
			"<SelectOnAllocationExpression:" :  //$NON-NLS-1$
			"<SelectOnQualifiedAllocationExpression:") +  //$NON-NLS-1$
		super.toStringExpression(tab) + ">"; //$NON-NLS-1$
}
}
