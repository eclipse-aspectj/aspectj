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

public class QualifiedAllocationExpression extends AllocationExpression {
	
	//qualification may be on both side
	public Expression enclosingInstance;
	public AnonymousLocalTypeDeclaration anonymousType;

	public QualifiedAllocationExpression() {
	}

	public QualifiedAllocationExpression(AnonymousLocalTypeDeclaration anonymousType) {
		this.anonymousType = anonymousType;
	}

	public FlowInfo analyseCode(
		BlockScope currentScope,
		FlowContext flowContext,
		FlowInfo flowInfo) {

		// variation on allocation, where can be specified an enclosing instance and an anonymous type

		// analyse the enclosing instance
		if (enclosingInstance != null) {
			flowInfo = enclosingInstance.analyseCode(currentScope, flowContext, flowInfo);
		}
		// process arguments
		if (arguments != null) {
			for (int i = 0, count = arguments.length; i < count; i++) {
				flowInfo = arguments[i].analyseCode(currentScope, flowContext, flowInfo);
			}
		}

		// analyse the anonymous nested type
		if (anonymousType != null) {
			flowInfo = anonymousType.analyseCode(currentScope, flowContext, flowInfo);
		}

		// record some dependency information for exception types
		ReferenceBinding[] thrownExceptions;
		if (((thrownExceptions = binding.thrownExceptions).length) != 0) {
			// check exception handling
			flowContext.checkExceptionHandlers(
				thrownExceptions,
				this,
				flowInfo,
				currentScope);
		}
		manageEnclosingInstanceAccessIfNecessary(currentScope);
		manageSyntheticAccessIfNecessary(currentScope);
		return flowInfo;
	}

	public Expression enclosingInstance() {

		return enclosingInstance;
	}

	public void generateCode(
		BlockScope currentScope,
		CodeStream codeStream,
		boolean valueRequired) {

		int pc = codeStream.position;
		ReferenceBinding allocatedType = binding.declaringClass;
		if (allocatedType.isLocalType()) {
			LocalTypeBinding localType = (LocalTypeBinding) allocatedType;
			localType.constantPoolName(
				codeStream.classFile.outerMostEnclosingClassFile().computeConstantPoolName(
					localType));
		}
		codeStream.new_(allocatedType);
		if (valueRequired) {
			codeStream.dup();
		}
		// better highlight for allocation: display the type individually
		codeStream.recordPositionsFrom(pc, type.sourceStart);

		// handling innerclass instance allocation
		if (allocatedType.isNestedType()) {
			// make sure its name is computed before arguments, since may be necessary for argument emulation
			codeStream.generateSyntheticArgumentValues(
				currentScope,
				allocatedType,
				enclosingInstance(),
				this);
		}
		// generate the arguments for constructor
		if (arguments != null) {
			for (int i = 0, count = arguments.length; i < count; i++) {
				arguments[i].generateCode(currentScope, codeStream, true);
			}
		}
		// invoke constructor
		if (syntheticAccessor == null) {
			codeStream.invokespecial(binding);
		} else {
			// synthetic accessor got some extra arguments appended to its signature, which need values
			for (int i = 0,
				max = syntheticAccessor.parameters.length - binding.parameters.length;
				i < max;
				i++) {
				codeStream.aconst_null();
			}
			codeStream.invokespecial(syntheticAccessor);
		}
		codeStream.recordPositionsFrom(pc, this.sourceStart);
		if (anonymousType != null) {
			anonymousType.generateCode(currentScope, codeStream);
		}
	}
	
	public boolean isSuperAccess() {

		// necessary to lookup super constructor of anonymous type
		return anonymousType != null;
	}
	
	/* Inner emulation consists in either recording a dependency 
	 * link only, or performing one level of propagation.
	 *
	 * Dependency mechanism is used whenever dealing with source target
	 * types, since by the time we reach them, we might not yet know their
	 * exact need.
	 */
	public void manageEnclosingInstanceAccessIfNecessary(BlockScope currentScope) {

		ReferenceBinding allocatedType;

		// perform some emulation work in case there is some and we are inside a local type only
		if ((allocatedType = binding.declaringClass).isNestedType()
			&& currentScope.enclosingSourceType().isLocalType()) {

			if (allocatedType.isLocalType()) {
				((LocalTypeBinding) allocatedType).addInnerEmulationDependent(
					currentScope,
					enclosingInstance != null,
					false);
				// request cascade of accesses
			} else {
				// locally propagate, since we already now the desired shape for sure
				currentScope.propagateInnerEmulation(
					allocatedType,
					enclosingInstance != null,
					false);
				// request cascade of accesses
			}
		}
	}

	public TypeBinding resolveType(BlockScope scope) {

		if (anonymousType == null && enclosingInstance == null)
			return super.resolveType(scope);
		// added for code assist... is not possible with 'normal' code

		// Propagate the type checking to the arguments, and checks if the constructor is defined.

		// ClassInstanceCreationExpression ::= Primary '.' 'new' SimpleName '(' ArgumentListopt ')' ClassBodyopt
		// ClassInstanceCreationExpression ::= Name '.' 'new' SimpleName '(' ArgumentListopt ')' ClassBodyopt
		// ==> by construction, when there is an enclosing instance the typename may NOT be qualified
		// ==> therefore by construction the type is always a SingleTypeReferenceType instead of being either 
		// sometime a SingleTypeReference and sometime a QualifedTypeReference

		constant = NotAConstant;
		TypeBinding enclosingInstTb = null;
		TypeBinding recType;
		if (anonymousType == null) {
			//----------------no anonymous class------------------------	
			if ((enclosingInstTb = enclosingInstance.resolveType(scope)) == null)
				return null;
			if (enclosingInstTb.isBaseType() | enclosingInstTb.isArrayType()) {
				scope.problemReporter().illegalPrimitiveOrArrayTypeForEnclosingInstance(
					enclosingInstTb,
					enclosingInstance);
				return null;
			}
			recType =
				((SingleTypeReference) type).resolveTypeEnclosing(
					scope,
					(ReferenceBinding) enclosingInstTb);
			// will check for null after args are resolved
			TypeBinding[] argumentTypes = NoParameters;
			if (arguments != null) {
				boolean argHasError = false;
				int length = arguments.length;
				argumentTypes = new TypeBinding[length];
				for (int i = 0; i < length; i++)
					if ((argumentTypes[i] = arguments[i].resolveType(scope)) == null)
						argHasError = true;
				if (argHasError)
					return recType;
			}
			if (recType == null)
				return null;
			if (!recType.canBeInstantiated()) {
				scope.problemReporter().cannotInstantiate(type, recType);
				return recType;
			}
			if ((binding =
				scope.getConstructor((ReferenceBinding) recType, argumentTypes, this))
				.isValidBinding()) {
				if (isMethodUseDeprecated(binding, scope))
					scope.problemReporter().deprecatedMethod(binding, this);

				if (arguments != null)
					for (int i = 0; i < arguments.length; i++)
						arguments[i].implicitWidening(binding.parameters[i], argumentTypes[i]);
			} else {
				if (binding.declaringClass == null)
					binding.declaringClass = (ReferenceBinding) recType;
				scope.problemReporter().invalidConstructor(this, binding);
				return recType;
			}

			// The enclosing instance must be compatible with the innermost enclosing type
			ReferenceBinding expectedType = binding.declaringClass.enclosingType();
			if (scope.areTypesCompatible(enclosingInstTb, expectedType))
				return recType;
			scope.problemReporter().typeMismatchErrorActualTypeExpectedType(
				enclosingInstance,
				enclosingInstTb,
				expectedType);
			return recType;
		}

		//--------------there is an anonymous type declaration-----------------
		if (enclosingInstance != null) {
			if ((enclosingInstTb = enclosingInstance.resolveType(scope)) == null)
				return null;
			if (enclosingInstTb.isBaseType() | enclosingInstTb.isArrayType()) {
				scope.problemReporter().illegalPrimitiveOrArrayTypeForEnclosingInstance(
					enclosingInstTb,
					enclosingInstance);
				return null;
			}
		}
		// due to syntax-construction, recType is a ReferenceBinding		
		recType =
			(enclosingInstance == null)
				? type.resolveType(scope)
				: ((SingleTypeReference) type).resolveTypeEnclosing(
					scope,
					(ReferenceBinding) enclosingInstTb);
		if (recType == null)
			return null;
		if (((ReferenceBinding) recType).isFinal()) {
			scope.problemReporter().anonymousClassCannotExtendFinalClass(type, recType);
			return null;
		}
		TypeBinding[] argumentTypes = NoParameters;
		if (arguments != null) {
			int length = arguments.length;
			argumentTypes = new TypeBinding[length];
			for (int i = 0; i < length; i++)
				if ((argumentTypes[i] = arguments[i].resolveType(scope)) == null)
					return null;
		}

		// an anonymous class inherits from java.lang.Object when declared "after" an interface
		ReferenceBinding superBinding =
			recType.isInterface() ? scope.getJavaLangObject() : (ReferenceBinding) recType;
		MethodBinding inheritedBinding =
			scope.getConstructor(superBinding, argumentTypes, this);
		if (!inheritedBinding.isValidBinding()) {
			if (inheritedBinding.declaringClass == null)
				inheritedBinding.declaringClass = superBinding;
			scope.problemReporter().invalidConstructor(this, inheritedBinding);
			return null;
		}
		if (enclosingInstance != null) {
			if (!scope
				.areTypesCompatible(
					enclosingInstTb,
					inheritedBinding.declaringClass.enclosingType())) {
				scope.problemReporter().typeMismatchErrorActualTypeExpectedType(
					enclosingInstance,
					enclosingInstTb,
					inheritedBinding.declaringClass.enclosingType());
				return null;
			}
		}

		// this promotion has to be done somewhere: here or inside the constructor of the
		// anonymous class. We do it here while the constructor of the inner is then easier.
		if (arguments != null)
			for (int i = 0; i < arguments.length; i++)
				arguments[i].implicitWidening(inheritedBinding.parameters[i], argumentTypes[i]);

		// Update the anonymous inner class : superclass, interface  
		scope.addAnonymousType(anonymousType, (ReferenceBinding) recType);
		anonymousType.resolve(scope);
		binding = anonymousType.createsInternalConstructorWithBinding(inheritedBinding);
		return anonymousType.binding; // 1.2 change
	}

	public String toStringExpression(int tab) {

		String s = ""; //$NON-NLS-1$
		if (enclosingInstance != null)
			s += enclosingInstance.toString() + "."; //$NON-NLS-1$
		s += super.toStringExpression(tab);
		if (anonymousType != null) {
			s += anonymousType.toString(tab);
		} //allows to restart just after the } one line under ....
		return s;
	}

	public void traverse(IAbstractSyntaxTreeVisitor visitor, BlockScope scope) {

		if (visitor.visit(this, scope)) {
			if (enclosingInstance != null)
				enclosingInstance.traverse(visitor, scope);
			type.traverse(visitor, scope);
			if (arguments != null) {
				int argumentsLength = arguments.length;
				for (int i = 0; i < argumentsLength; i++)
					arguments[i].traverse(visitor, scope);
			}
			if (anonymousType != null)
				anonymousType.traverse(visitor, scope);
		}
		visitor.endVisit(this, scope);
	}
}