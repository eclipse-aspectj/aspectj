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

public abstract class TypeReference extends Expression {
	public TypeBinding binding;
public TypeReference() {
		super () ;
		}
// allows us to trap completion & selection nodes

public void aboutToResolve(Scope scope) {}
/*
 * Answer a base type reference (can be an array of base type).
 */
public static final TypeReference baseTypeReference(int baseType, int dim) {
	
	if (dim == 0) {
		switch (baseType) {
			case (T_void) :
				return new SingleTypeReference(VoidBinding.simpleName, 0);
			case (T_boolean) :
				return new SingleTypeReference(BooleanBinding.simpleName, 0);
			case (T_char) :
				return new SingleTypeReference(CharBinding.simpleName, 0);
			case (T_float) :
				return new SingleTypeReference(FloatBinding.simpleName, 0);
			case (T_double) :
				return new SingleTypeReference(DoubleBinding.simpleName, 0);
			case (T_byte) :
				return new SingleTypeReference(ByteBinding.simpleName, 0);
			case (T_short) :
				return new SingleTypeReference(ShortBinding.simpleName, 0);
			case (T_int) :
				return new SingleTypeReference(IntBinding.simpleName, 0);
			default : //T_long	
				return new SingleTypeReference(LongBinding.simpleName, 0);
		}
	}
	switch (baseType) {
		case (T_void) :
			return new ArrayTypeReference(VoidBinding.simpleName, dim, 0);
		case (T_boolean) :
			return new ArrayTypeReference(BooleanBinding.simpleName, dim, 0);
		case (T_char) :
			return new ArrayTypeReference(CharBinding.simpleName, dim, 0);
		case (T_float) :
			return new ArrayTypeReference(FloatBinding.simpleName, dim, 0);
		case (T_double) :
			return new ArrayTypeReference(DoubleBinding.simpleName, dim, 0);
		case (T_byte) :
			return new ArrayTypeReference(ByteBinding.simpleName, dim, 0);
		case (T_short) :
			return new ArrayTypeReference(ShortBinding.simpleName, dim, 0);
		case (T_int) :
			return new ArrayTypeReference(IntBinding.simpleName, dim, 0);
		default : //T_long	
			return new ArrayTypeReference(LongBinding.simpleName, dim, 0);
	}
}
public abstract TypeReference copyDims(int dim);
public int dimensions() {
	return 0;
}
public abstract TypeBinding getTypeBinding(Scope scope);
/**
 * @return char[][]
 */
public abstract char [][] getTypeName() ;
public boolean isTypeReference() {
	return true;
}
public TypeBinding resolveType(BlockScope scope) {
	// handle the error here
	constant = NotAConstant;
	if (binding != null) { // is a shared type reference which was already resolved
		if (!binding.isValidBinding())
			return null; // already reported error
	} else {
		binding = getTypeBinding(scope);
		if (!binding.isValidBinding()) {
			scope.problemReporter().invalidType(this, binding);
			return null;
		}
		if (isTypeUseDeprecated(binding, scope))
			scope.problemReporter().deprecatedType(binding, this);
	}
	return binding;
}
public abstract void traverse(IAbstractSyntaxTreeVisitor visitor, ClassScope classScope);
}
