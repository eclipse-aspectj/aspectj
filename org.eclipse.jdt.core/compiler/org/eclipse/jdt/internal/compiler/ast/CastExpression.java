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
import org.eclipse.jdt.internal.compiler.impl.*;
import org.eclipse.jdt.internal.compiler.codegen.*;
import org.eclipse.jdt.internal.compiler.flow.*;
import org.eclipse.jdt.internal.compiler.lookup.*;

public class CastExpression extends Expression {

	public Expression expression;
	public Expression type;
	public boolean needRuntimeCheckcast;
	public TypeBinding castTb;

	//expression.implicitConversion holds the cast for baseType casting 
	public CastExpression(Expression e, Expression t) {
		expression = e;
		type = t;

		//due to the fact an expression may start with ( and that a cast also start with (
		//the field is an expression....it can be a TypeReference OR a NameReference Or
		//an expression <--this last one is invalid.......

		// :-( .............

		//if (type instanceof TypeReference )
		//	flag = IsTypeReference ;
		//else
		//	if (type instanceof NameReference)
		//		flag = IsNameReference ;
		//	else
		//		flag = IsExpression ;

	}

	public FlowInfo analyseCode(
		BlockScope currentScope,
		FlowContext flowContext,
		FlowInfo flowInfo) {

		return expression
			.analyseCode(currentScope, flowContext, flowInfo)
			.unconditionalInits();
	}

	public final void areTypesCastCompatible(
		BlockScope scope,
		TypeBinding castTb,
		TypeBinding expressionTb) {

		// see specifications p.68
		// handle errors and process constant when needed

		// if either one of the type is null ==>
		// some error has been already reported some where ==>
		// we then do not report an obvious-cascade-error.

		needRuntimeCheckcast = false;
		if (castTb == null || expressionTb == null)
			return;
		if (castTb.isBaseType()) {
			if (expressionTb.isBaseType()) {
				if (expressionTb == castTb) {
					constant = expression.constant; //use the same constant
					return;
				}
				if (scope.areTypesCompatible(expressionTb, castTb)
					|| BaseTypeBinding.isNarrowing(castTb.id, expressionTb.id)) {
					expression.implicitConversion = (castTb.id << 4) + expressionTb.id;
					if (expression.constant != Constant.NotAConstant)
						constant = expression.constant.castTo(expression.implicitConversion);
					return;
				}
			}
			scope.problemReporter().typeCastError(this, castTb, expressionTb);
			return;
		}

		//-----------cast to something which is NOT a base type--------------------------	
		if (expressionTb == NullBinding)
			return; //null is compatible with every thing

		if (expressionTb.isBaseType()) {
			scope.problemReporter().typeCastError(this, castTb, expressionTb);
			return;
		}

		if (expressionTb.isArrayType()) {
			if (castTb.isArrayType()) {
				//------- (castTb.isArray) expressionTb.isArray -----------
				TypeBinding expressionEltTb = ((ArrayBinding) expressionTb).elementsType(scope);
				if (expressionEltTb.isBaseType()) {
					// <---stop the recursion------- 
					if (((ArrayBinding) castTb).elementsType(scope) == expressionEltTb)
						needRuntimeCheckcast = true;
					else
						scope.problemReporter().typeCastError(this, castTb, expressionTb);
					return;
				}
				// recursively on the elements...
				areTypesCastCompatible(
					scope,
					((ArrayBinding) castTb).elementsType(scope),
					expressionEltTb);
				return;
			} else if (
				castTb.isClass()) {
				//------(castTb.isClass) expressionTb.isArray ---------------	
				if (scope.isJavaLangObject(castTb))
					return;
			} else { //------- (castTb.isInterface) expressionTb.isArray -----------
				if (scope.isJavaLangCloneable(castTb) || scope.isJavaIoSerializable(castTb)) {
					needRuntimeCheckcast = true;
					return;
				}
			}
			scope.problemReporter().typeCastError(this, castTb, expressionTb);
			return;
		}

		if (expressionTb.isClass()) {
			if (castTb.isArrayType()) {
				// ---- (castTb.isArray) expressionTb.isClass -------
				if (scope.isJavaLangObject(expressionTb)) { // potential runtime error
					needRuntimeCheckcast = true;
					return;
				}
			} else if (
				castTb.isClass()) { // ----- (castTb.isClass) expressionTb.isClass ------
				if (scope.areTypesCompatible(expressionTb, castTb)) // no runtime error
					return;
				if (scope.areTypesCompatible(castTb, expressionTb)) {
					// potential runtime  error
					needRuntimeCheckcast = true;
					return;
				}
			} else { // ----- (castTb.isInterface) expressionTb.isClass -------  
				if (((ReferenceBinding) expressionTb).isFinal()) {
					// no subclass for expressionTb, thus compile-time check is valid
					if (scope.areTypesCompatible(expressionTb, castTb))
						return;
				} else { // a subclass may implement the interface ==> no check at compile time
					needRuntimeCheckcast = true;
					return;
				}
			}
			scope.problemReporter().typeCastError(this, castTb, expressionTb);
			return;
		}

		//	if (expressionTb.isInterface()) { cannot be anything else
		if (castTb.isArrayType()) {
			// ----- (castTb.isArray) expressionTb.isInterface ------
			if (scope.isJavaLangCloneable(expressionTb)
				|| scope.isJavaIoSerializable(expressionTb)) // potential runtime error
				needRuntimeCheckcast = true;
			else
				scope.problemReporter().typeCastError(this, castTb, expressionTb);
			return;
		} else if (
			castTb.isClass()) { // ----- (castTb.isClass) expressionTb.isInterface --------
			if (scope.isJavaLangObject(castTb)) // no runtime error
				return;
			if (((ReferenceBinding) castTb).isFinal()) {
				// no subclass for castTb, thus compile-time check is valid
				if (!scope.areTypesCompatible(castTb, expressionTb)) {
					// potential runtime error
					scope.problemReporter().typeCastError(this, castTb, expressionTb);
					return;
				}
			}
		} else { // ----- (castTb.isInterface) expressionTb.isInterface -------
			if (castTb != expressionTb
				&& (Scope.compareTypes(castTb, expressionTb) == NotRelated)) {
				MethodBinding[] castTbMethods = ((ReferenceBinding) castTb).methods();
				MethodBinding[] expressionTbMethods =
					((ReferenceBinding) expressionTb).methods();
				int exprMethodsLength = expressionTbMethods.length;
				for (int i = 0, castMethodsLength = castTbMethods.length;
					i < castMethodsLength;
					i++)
					for (int j = 0; j < exprMethodsLength; j++)
						if (castTbMethods[i].returnType != expressionTbMethods[j].returnType)
							if (castTbMethods[i].selector == expressionTbMethods[j].selector)
								if (castTbMethods[i].areParametersEqual(expressionTbMethods[j]))
									scope.problemReporter().typeCastError(this, castTb, expressionTb);
			}
		}
		needRuntimeCheckcast = true;
		return;
	}

	/**
	 * Cast expression code generation
	 *
	 * @param currentScope org.eclipse.jdt.internal.compiler.lookup.BlockScope
	 * @param codeStream org.eclipse.jdt.internal.compiler.codegen.CodeStream
	 * @param valueRequired boolean
	 */
	public void generateCode(
		BlockScope currentScope,
		CodeStream codeStream,
		boolean valueRequired) {

		int pc = codeStream.position;
		if (constant != NotAConstant) {
			if (valueRequired
				|| needRuntimeCheckcast) { // Added for: 1F1W9IG: IVJCOM:WINNT - Compiler omits casting check
				codeStream.generateConstant(constant, implicitConversion);
				if (needRuntimeCheckcast) {
					codeStream.checkcast(castTb);
					if (!valueRequired)
						codeStream.pop();
				}
			}
			codeStream.recordPositionsFrom(pc, this.sourceStart);
			return;
		}
		expression.generateCode(
			currentScope,
			codeStream,
			valueRequired || needRuntimeCheckcast);
		if (needRuntimeCheckcast) {
			codeStream.checkcast(castTb);
			if (!valueRequired)
				codeStream.pop();
		} else {
			if (valueRequired)
				codeStream.generateImplicitConversion(implicitConversion);
		}
		codeStream.recordPositionsFrom(pc, this.sourceStart);
	}

	public TypeBinding resolveType(BlockScope scope) {
		// compute a new constant if the cast is effective

		// due to the fact an expression may start with ( and that a cast can also start with (
		// the field is an expression....it can be a TypeReference OR a NameReference Or
		// any kind of Expression <-- this last one is invalid.......

		constant = Constant.NotAConstant;
		implicitConversion = T_undefined;
		TypeBinding expressionTb = expression.resolveType(scope);
		if (expressionTb == null)
			return null;

		if ((type instanceof TypeReference) || (type instanceof NameReference)) {
			if ((castTb = type.resolveType(scope)) == null)
				return null;
			areTypesCastCompatible(scope, castTb, expressionTb);
			return castTb;
		} else { // expression as a cast !!!!!!!! 
			scope.problemReporter().invalidTypeReference(type);
			return null;
		}
	}

	public String toStringExpression() {

		return "(" + type.toString(0) + ") " + //$NON-NLS-2$ //$NON-NLS-1$
		expression.toStringExpression();
	}

	public void traverse(
		IAbstractSyntaxTreeVisitor visitor,
		BlockScope blockScope) {

		if (visitor.visit(this, blockScope)) {
			type.traverse(visitor, blockScope);
			expression.traverse(visitor, blockScope);
		}
		visitor.endVisit(this, blockScope);
	}
}