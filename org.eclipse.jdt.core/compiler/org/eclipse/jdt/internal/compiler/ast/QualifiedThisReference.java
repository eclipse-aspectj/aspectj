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
import org.eclipse.jdt.internal.compiler.codegen.*;
import org.eclipse.jdt.internal.compiler.flow.*;
import org.eclipse.jdt.internal.compiler.lookup.*;

public class QualifiedThisReference extends ThisReference {
	
	public TypeReference qualification;
	ReferenceBinding currentCompatibleType;

	public QualifiedThisReference(TypeReference name, int pos, int sourceEnd) {

		qualification = name;
		this.sourceEnd = sourceEnd;
		this.sourceStart = name.sourceStart;
	}

	public FlowInfo analyseCode(
		BlockScope currentScope,
		FlowContext flowContext,
		FlowInfo flowInfo) {

		manageEnclosingInstanceAccessIfNecessary(currentScope);
		return flowInfo;
	}

	public FlowInfo analyseCode(
		BlockScope currentScope,
		FlowContext flowContext,
		FlowInfo flowInfo,
		boolean valueRequired) {

		if (valueRequired) {
			manageEnclosingInstanceAccessIfNecessary(currentScope);
		}
		return flowInfo;
	}

	protected boolean checkAccess(
		MethodScope methodScope,
		TypeBinding targetType) {

		// this/super cannot be used in constructor call
		if (methodScope.isConstructorCall) {
			methodScope.problemReporter().fieldsOrThisBeforeConstructorInvocation(this);
			return false;
		}

		// static may not refer to this/super
		if (methodScope.isStatic) {
			methodScope.problemReporter().incorrectEnclosingInstanceReference(
				this,
				targetType);
			return false;
		}
		return true;
	}

	/**
	 * Code generation for QualifiedThisReference
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
		if (valueRequired) {
			if ((bits & DepthMASK) != 0) {
				Object[] emulationPath =
					currentScope.getExactEmulationPath(currentCompatibleType);
				if (emulationPath == null) {
					// internal error, per construction we should have found it
					currentScope.problemReporter().needImplementation();
				} else {
					codeStream.generateOuterAccess(emulationPath, this, currentScope);
				}
			} else {
				// nothing particular after all
				codeStream.aload_0();
			}
		}
		codeStream.recordPositionsFrom(pc, this.sourceStart);
	}

	void manageEnclosingInstanceAccessIfNecessary(BlockScope currentScope) {

		currentScope.emulateOuterAccess(
			(SourceTypeBinding) currentCompatibleType,
			false);
		// request cascade of accesses
	}

	public TypeBinding resolveType(BlockScope scope) {

		constant = NotAConstant;
		TypeBinding qualificationTb = qualification.resolveType(scope);
		if (qualificationTb == null)
			return null;

		// the qualification MUST exactly match some enclosing type name
		// Its possible to qualify 'this' by the name of the current class
		int depth = 0;
		currentCompatibleType = scope.referenceType().binding;
		while (currentCompatibleType != null
			&& currentCompatibleType != qualificationTb) {
			depth++;
			currentCompatibleType =
				currentCompatibleType.isStatic() ? null : currentCompatibleType.enclosingType();
		}
		bits &= ~DepthMASK; // flush previous depth if any			
		bits |= (depth & 0xFF) << DepthSHIFT; // encoded depth into 8 bits

		if (currentCompatibleType == null) {
			scope.problemReporter().incorrectEnclosingInstanceReference(
				this,
				qualificationTb);
			return null;
		}

		// Ensure one cannot write code like: B() { super(B.this); }
		if (depth == 0) {
			if (!checkAccess(scope.methodScope(), qualificationTb))
				return null;
		} else {
			// Could also be targeting an enclosing instance inside a super constructor invocation
			//	class X {
			//		public X(int i) {
			//			this(new Object() { Object obj = X.this; });
			//		}
			//	}

			MethodScope methodScope = scope.methodScope();
			while (methodScope != null) {
				if (methodScope.enclosingSourceType() == currentCompatibleType) {
					if (!this.checkAccess(methodScope, qualificationTb))
						return null;
					break;
				}
				methodScope = methodScope.parent.methodScope();
			}
		}
		return qualificationTb;
	}

	public String toStringExpression() {

		return qualification.toString(0) + ".this"; //$NON-NLS-1$
	}

	public void traverse(
		IAbstractSyntaxTreeVisitor visitor,
		BlockScope blockScope) {

		if (visitor.visit(this, blockScope)) {
			qualification.traverse(visitor, blockScope);
		}
		visitor.endVisit(this, blockScope);
	}
}