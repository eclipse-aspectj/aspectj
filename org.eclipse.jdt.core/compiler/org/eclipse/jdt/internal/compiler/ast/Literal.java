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

import org.eclipse.jdt.internal.compiler.impl.*;
import org.eclipse.jdt.internal.compiler.lookup.*;

public abstract class Literal extends Expression {
	

public Literal(int s,int e) {
	sourceStart = s ;
	sourceEnd= e;
}
public abstract void computeConstant() ;
	//ON ERROR constant STAYS NULL
public abstract TypeBinding literalType(BlockScope scope);
public TypeBinding resolveType(BlockScope scope) {
	// compute the real value, which must range its type's range

	computeConstant();
	if (constant == null) {
		scope.problemReporter().constantOutOfRange(this);
		constant = Constant.NotAConstant;
		return null;
	}
	return literalType(scope);
}
public abstract char[] source() ;
}
