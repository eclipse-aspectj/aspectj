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
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor;
import org.eclipse.jdt.internal.compiler.lookup.*;

public class ArrayQualifiedTypeReference extends QualifiedTypeReference {
	int dimensions;
public ArrayQualifiedTypeReference(char[][] sources , int dim, long[] poss) {
	super( sources , poss);
	dimensions = dim ;
}
public ArrayQualifiedTypeReference(char[][] sources , TypeBinding tb, int dim, long[] poss) {
	super( sources , tb, poss);
	dimensions = dim ;
}
public int dimensions() {
	return dimensions;
}
public TypeBinding getTypeBinding(Scope scope) {
	if (binding != null)
		return binding;
	return scope.createArray(scope.getType(tokens), dimensions);
}
public String toStringExpression(int tab){
	/* slow speed */

	String s = super.toStringExpression(tab)  ;
	if (dimensions == 1 ) return s + "[]" ; //$NON-NLS-1$
	for (int i=1 ; i <= dimensions ; i++)
		s = s + "[]" ; //$NON-NLS-1$
	return s ;
}
public void traverse(IAbstractSyntaxTreeVisitor visitor, BlockScope scope) {
	visitor.visit(this, scope);
	visitor.endVisit(this, scope);
}
public void traverse(IAbstractSyntaxTreeVisitor visitor, ClassScope scope) {
	visitor.visit(this, scope);
	visitor.endVisit(this, scope);
}
}
