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

public class ExplicitConstructorCall
	extends Statement
	implements InvocationSite {
		
	public Expression[] arguments;
	public Expression qualification;
	public MethodBinding binding;

	public int accessMode;

	public final static int ImplicitSuper = 1;
	public final static int Super = 2;
	public final static int This = 3;

	public VariableBinding[][] implicitArguments;
	boolean discardEnclosingInstance;

	MethodBinding syntheticAccessor;

	public ExplicitConstructorCall(int accessMode) {
		this.accessMode = accessMode;
	}

	public FlowInfo analyseCode(
		BlockScope currentScope,
		FlowContext flowContext,
		FlowInfo flowInfo) {

		// must verify that exceptions potentially thrown by this expression are caught in the method.

		try {
			((MethodScope) currentScope).isConstructorCall = true;

			// process enclosing instance
			if (qualification != null) {
				flowInfo =
					qualification
						.analyseCode(currentScope, flowContext, flowInfo)
						.unconditionalInits();
			}
			// process arguments
			if (arguments != null) {
				for (int i = 0, max = arguments.length; i < max; i++) {
					flowInfo =
						arguments[i]
							.analyseCode(currentScope, flowContext, flowInfo)
							.unconditionalInits();
				}
			}

			ReferenceBinding[] thrownExceptions;
			if ((thrownExceptions = binding.thrownExceptions) != NoExceptions) {
				// check exceptions
				flowContext.checkExceptionHandlers(
					thrownExceptions,
					(accessMode == ImplicitSuper)
						? (AstNode) currentScope.methodScope().referenceContext
						: (AstNode) this,
					flowInfo,
					currentScope);
			}
			manageEnclosingInstanceAccessIfNecessary(currentScope);
			manageSyntheticAccessIfNecessary(currentScope);
			return flowInfo;
		} finally {
			((MethodScope) currentScope).isConstructorCall = false;
		}
	}

	/**
	 * Constructor call code generation
	 *
	 * @param currentScope org.eclipse.jdt.internal.compiler.lookup.BlockScope
	 * @param codeStream org.eclipse.jdt.internal.compiler.codegen.CodeStream
	 */
	public void generateCode(BlockScope currentScope, CodeStream codeStream) {

		if ((bits & IsReachableMASK) == 0) {
			return;
		}
		try {
			((MethodScope) currentScope).isConstructorCall = true;

			int pc = codeStream.position;
			codeStream.aload_0();

			// handling innerclass constructor invocation
			ReferenceBinding targetType;
			if ((targetType = binding.declaringClass).isNestedType()) {
				codeStream.generateSyntheticArgumentValues(
					currentScope,
					targetType,
					discardEnclosingInstance ? null : qualification,
					this);
			}
			// regular code gen
			if (arguments != null) {
				for (int i = 0, max = arguments.length; i < max; i++) {
					arguments[i].generateCode(currentScope, codeStream, true);
				}
			}
			if (syntheticAccessor != null) {
				// synthetic accessor got some extra arguments appended to its signature, which need values
				for (int i = 0,
					max = syntheticAccessor.parameters.length - binding.parameters.length;
					i < max;
					i++) {
					codeStream.aconst_null();
				}
				codeStream.invokespecial(syntheticAccessor);
			} else {
				codeStream.invokespecial(binding);
			}
			codeStream.recordPositionsFrom(pc, this.sourceStart);
		} finally {
			((MethodScope) currentScope).isConstructorCall = false;
		}
	}

	public boolean isImplicitSuper() {
		//return true if I'm of these compiler added statement super();

		return (accessMode == ImplicitSuper);
	}

	public boolean isSuperAccess() {

		return accessMode != This;
	}

	public boolean isTypeAccess() {

		return true;
	}

	/* Inner emulation consists in either recording a dependency 
	 * link only, or performing one level of propagation.
	 *
	 * Dependency mechanism is used whenever dealing with source target
	 * types, since by the time we reach them, we might not yet know their
	 * exact need.
	 */
	void manageEnclosingInstanceAccessIfNecessary(BlockScope currentScope) {
		ReferenceBinding superType;

		// perform some emulation work in case there is some and we are inside a local type only
		if ((superType = binding.declaringClass).isNestedType()
			&& currentScope.enclosingSourceType().isLocalType()) {

			if (superType.isLocalType()) {
				((LocalTypeBinding) superType).addInnerEmulationDependent(
					currentScope,
					qualification != null,
					true);
				// request direct access
			} else {
				// locally propagate, since we already now the desired shape for sure
				currentScope.propagateInnerEmulation(superType, qualification != null, true);
				// request direct access

			}
		}
	}

	public void manageSyntheticAccessIfNecessary(BlockScope currentScope) {
		if (binding.alwaysNeedsAccessMethod()) {
			syntheticAccessor = binding.getAccessMethod(true);
			return;
		}
		
		// perform some emulation work in case there is some and we are inside a local type only
		if (binding.isPrivate() && (accessMode != This)) {

			if (currentScope
				.environment()
				.options
				.isPrivateConstructorAccessChangingVisibility) {
				binding.tagForClearingPrivateModifier();
				// constructor will not be dumped as private, no emulation required thus
			} else {
				syntheticAccessor =
					((SourceTypeBinding) binding.declaringClass).addSyntheticMethod(binding);
				currentScope.problemReporter().needToEmulateMethodAccess(binding, this);
			}
		}
	}

	public void resolve(BlockScope scope) {
		// the return type should be void for a constructor.
		// the test is made into getConstructor

		// mark the fact that we are in a constructor call.....
		// unmark at all returns
		try {
			((MethodScope) scope).isConstructorCall = true;
			ReferenceBinding receiverType = scope.enclosingSourceType();
			//System.err.println("rT: " + receiverType + " scope " + scope);
			if (accessMode != This)
				receiverType = receiverType.superclass();

			if (receiverType == null) {
				return;
			}

			// qualification should be from the type of the enclosingType
			if (qualification != null) {
				if (accessMode != Super) {
					scope.problemReporter().unnecessaryEnclosingInstanceSpecification(
						qualification,
						receiverType);
				}
				ReferenceBinding enclosingType = receiverType.enclosingType();
				if (enclosingType == null) {
					scope.problemReporter().unnecessaryEnclosingInstanceSpecification(
						qualification,
						receiverType);
					discardEnclosingInstance = true;
				} else {
					TypeBinding qTb = qualification.resolveTypeExpecting(scope, enclosingType);
					qualification.implicitWidening(qTb, qTb);
				}
			}

			// arguments buffering for the method lookup
			TypeBinding[] argTypes = NoParameters;
			if (arguments != null) {
				boolean argHasError = false; // typeChecks all arguments
				int length = arguments.length;
				argTypes = new TypeBinding[length];
				for (int i = 0; i < length; i++)
					if ((argTypes[i] = arguments[i].resolveType(scope)) == null)
						argHasError = true;
				if (argHasError)
					return;
			}
			if ((binding = scope.getConstructor(receiverType, argTypes, this))
				.isValidBinding()) {
				if (isMethodUseDeprecated(binding, scope))
					scope.problemReporter().deprecatedMethod(binding, this);

				// see for user-implicit widening conversion 
				if (arguments != null) {
					int length = arguments.length;
					TypeBinding[] paramTypes = binding.parameters;
					for (int i = 0; i < length; i++)
						arguments[i].implicitWidening(paramTypes[i], argTypes[i]);
				}
			} else {
				if (binding.declaringClass == null)
					binding.declaringClass = receiverType;
				scope.problemReporter().invalidConstructor(this, binding);
			}
		} finally {
			((MethodScope) scope).isConstructorCall = false;
		}
	}

	public void setActualReceiverType(ReferenceBinding receiverType) {
		// ignored
	}

	public void setDepth(int depth) {
		// ignore for here
	}

	public void setFieldIndex(int depth) {
		// ignore for here
	}

	public String toString(int tab) {

		String s = tabString(tab);
		if (qualification != null)
			s = s + qualification.toStringExpression() + "."; //$NON-NLS-1$
		if (accessMode == This) {
			s = s + "this("; //$NON-NLS-1$
		} else {
			s = s + "super("; //$NON-NLS-1$
		}
		if (arguments != null)
			for (int i = 0; i < arguments.length; i++) {
				s = s + arguments[i].toStringExpression();
				if (i != arguments.length - 1)
					s = s + ", "; //$NON-NLS-1$
			}
		s = s + ")"; //$NON-NLS-1$
		return s;
	}

	public void traverse(IAbstractSyntaxTreeVisitor visitor, BlockScope scope) {

		if (visitor.visit(this, scope)) {
			if (qualification != null) {
				qualification.traverse(visitor, scope);
			}
			if (arguments != null) {
				int argumentLength = arguments.length;
				for (int i = 0; i < argumentLength; i++)
					arguments[i].traverse(visitor, scope);
			}
		}
		visitor.endVisit(this, scope);
	}
}