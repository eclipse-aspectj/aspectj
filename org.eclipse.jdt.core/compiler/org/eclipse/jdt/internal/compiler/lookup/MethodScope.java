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
package org.eclipse.jdt.internal.compiler.lookup;

import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ConstructorDeclaration;
import org.eclipse.jdt.internal.compiler.ast.QualifiedNameReference;
import org.eclipse.jdt.internal.compiler.ast.SingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.flow.UnconditionalFlowInfo;
import org.eclipse.jdt.internal.compiler.impl.ReferenceContext;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;

/**
 * Particular block scope used for methods, constructors or clinits, representing
 * its outermost blockscope. Note also that such a scope will be provided to enclose
 * field initializers subscopes as well.
 */
public class MethodScope extends BlockScope {

	public ReferenceContext referenceContext;
	public boolean needToCompactLocalVariables;
	public boolean isStatic; // method modifier or initializer one

	//fields used in the TC process (no real meaning)
	public static final int NotInFieldDecl = -1; //must be a negative value 
	public boolean isConstructorCall = false; //modified on the fly by the TC
	public int fieldDeclarationIndex = NotInFieldDecl;
	//modified on the fly by the TC

	public int analysisIndex; // for setting flow-analysis id

	public boolean isPropagatingInnerClassEmulation;

	// for local variables table attributes
	public int lastIndex = 0;
	public long[] definiteInits = new long[4];
	public long[][] extraDefiniteInits = new long[4][];

	public MethodScope(
		ClassScope parent,
		ReferenceContext context,
		boolean isStatic) {

		super(METHOD_SCOPE, parent);
		locals = new LocalVariableBinding[5];
		this.referenceContext = context;
		this.isStatic = isStatic;
		this.startIndex = 0;
	}

	/* Spec : 8.4.3 & 9.4
	 */
	private void checkAndSetModifiersForConstructor(MethodBinding methodBinding) {
		
		int modifiers = methodBinding.modifiers;
		if ((modifiers & AccAlternateModifierProblem) != 0)
			problemReporter().duplicateModifierForMethod(
				methodBinding.declaringClass,
				(AbstractMethodDeclaration) referenceContext);

		if (((ConstructorDeclaration) referenceContext).isDefaultConstructor) {
			if (methodBinding.declaringClass.isPublic())
				modifiers |= AccPublic;
			else if (methodBinding.declaringClass.isProtected())
				modifiers |= AccProtected;
		}

		// after this point, tests on the 16 bits reserved.
		int realModifiers = modifiers & AccJustFlag;

		// check for abnormal modifiers
		int unexpectedModifiers =
			~(AccPublic | AccPrivate | AccProtected | AccStrictfp);
		if ((realModifiers & unexpectedModifiers) != 0)
			problemReporter().illegalModifierForMethod(
				methodBinding.declaringClass,
				(AbstractMethodDeclaration) referenceContext);
		else if (
			(((AbstractMethodDeclaration) referenceContext).modifiers & AccStrictfp) != 0)
			// must check the parse node explicitly
			problemReporter().illegalModifierForMethod(
				methodBinding.declaringClass,
				(AbstractMethodDeclaration) referenceContext);

		// check for incompatible modifiers in the visibility bits, isolate the visibility bits
		int accessorBits = realModifiers & (AccPublic | AccProtected | AccPrivate);
		if ((accessorBits & (accessorBits - 1)) != 0) {
			problemReporter().illegalVisibilityModifierCombinationForMethod(
				methodBinding.declaringClass,
				(AbstractMethodDeclaration) referenceContext);

			// need to keep the less restrictive
			if ((accessorBits & AccPublic) != 0) {
				if ((accessorBits & AccProtected) != 0)
					modifiers ^= AccProtected;
				if ((accessorBits & AccPrivate) != 0)
					modifiers ^= AccPrivate;
			}
			if ((accessorBits & AccProtected) != 0)
				if ((accessorBits & AccPrivate) != 0)
					modifiers ^= AccPrivate;
		}

		// if the receiver's declaring class is a private nested type, then make sure the receiver is not private (causes problems for inner type emulation)
		if (methodBinding.declaringClass.isPrivate())
			if ((modifiers & AccPrivate) != 0)
				modifiers ^= AccPrivate;

		methodBinding.modifiers = modifiers;
	}
	
	/* Spec : 8.4.3 & 9.4
	 */
	private void checkAndSetModifiersForMethod(MethodBinding methodBinding) {
		
		int modifiers = methodBinding.modifiers;
		if ((modifiers & AccAlternateModifierProblem) != 0)
			problemReporter().duplicateModifierForMethod(
				methodBinding.declaringClass,
				(AbstractMethodDeclaration) referenceContext);

		// after this point, tests on the 16 bits reserved.
		int realModifiers = modifiers & AccJustFlag;

		// set the requested modifiers for a method in an interface
		if (methodBinding.declaringClass.isInterface()) {
			if ((realModifiers & ~(AccPublic | AccAbstract)) != 0)
				problemReporter().illegalModifierForInterfaceMethod(
					methodBinding.declaringClass,
					(AbstractMethodDeclaration) referenceContext);
			return;
		}

		// check for abnormal modifiers
		int unexpectedModifiers =
			~(
				AccPublic
					| AccPrivate
					| AccProtected
					| AccAbstract
					| AccStatic
					| AccFinal
					| AccSynchronized
					| AccNative
					| AccStrictfp);
		if ((realModifiers & unexpectedModifiers) != 0)
			problemReporter().illegalModifierForMethod(
				methodBinding.declaringClass,
				(AbstractMethodDeclaration) referenceContext);

		// check for incompatible modifiers in the visibility bits, isolate the visibility bits
		int accessorBits = realModifiers & (AccPublic | AccProtected | AccPrivate);
		if ((accessorBits & (accessorBits - 1)) != 0) {
			problemReporter().illegalVisibilityModifierCombinationForMethod(
				methodBinding.declaringClass,
				(AbstractMethodDeclaration) referenceContext);

			// need to keep the less restrictive
			if ((accessorBits & AccPublic) != 0) {
				if ((accessorBits & AccProtected) != 0)
					modifiers ^= AccProtected;
				if ((accessorBits & AccPrivate) != 0)
					modifiers ^= AccPrivate;
			}
			if ((accessorBits & AccProtected) != 0)
				if ((accessorBits & AccPrivate) != 0)
					modifiers ^= AccPrivate;
		}

		// check for modifiers incompatible with abstract modifier
		if ((modifiers & AccAbstract) != 0) {
			int incompatibleWithAbstract =
				AccPrivate | AccStatic | AccFinal | AccSynchronized | AccNative | AccStrictfp;
			if ((modifiers & incompatibleWithAbstract) != 0)
				problemReporter().illegalAbstractModifierCombinationForMethod(
					methodBinding.declaringClass,
					(AbstractMethodDeclaration) referenceContext);
			if (!methodBinding.declaringClass.isAbstract())
				problemReporter().abstractMethodInAbstractClass(
					(SourceTypeBinding) methodBinding.declaringClass,
					(AbstractMethodDeclaration) referenceContext);
		}

		/* DISABLED for backward compatibility with javac (if enabled should also mark private methods as final)
		// methods from a final class are final : 8.4.3.3 
		if (methodBinding.declaringClass.isFinal())
			modifiers |= AccFinal;
		*/
		// native methods cannot also be tagged as strictfp
		if ((modifiers & AccNative) != 0 && (modifiers & AccStrictfp) != 0)
			problemReporter().nativeMethodsCannotBeStrictfp(
				methodBinding.declaringClass,
				(AbstractMethodDeclaration) referenceContext);

		// static members are only authorized in a static member or top level type
		if (((realModifiers & AccStatic) != 0)
			&& methodBinding.declaringClass.isNestedType()
			&& !methodBinding.declaringClass.isStatic())
			problemReporter().unexpectedStaticModifierForMethod(
				methodBinding.declaringClass,
				(AbstractMethodDeclaration) referenceContext);

		methodBinding.modifiers = modifiers;
	}
	
	/* Error management:
	 * 		keep null for all the errors that prevent the method to be created
	 * 		otherwise return a correct method binding (but without the element
	 *		that caused the problem) : ie : Incorrect thrown exception
	 */
	MethodBinding createMethod(AbstractMethodDeclaration method) {

		// is necessary to ensure error reporting
		this.referenceContext = method;
		method.scope = this;
		SourceTypeBinding declaringClass = referenceType().binding;
		int modifiers = method.modifiers | AccUnresolved;
		if (method.isConstructor()) {
			method.binding = new MethodBinding(modifiers, null, null, declaringClass);
			checkAndSetModifiersForConstructor(method.binding);
		} else {
			if (declaringClass.isInterface())
				modifiers |= AccPublic | AccAbstract;
			method.binding =
				new MethodBinding(modifiers, method.selector, null, null, null, declaringClass);
			checkAndSetModifiersForMethod(method.binding);
		}

		this.isStatic = method.binding.isStatic();
		return method.binding;
	}

	/* Overridden to detect the error case inside an explicit constructor call:
	
	class X {
		int i;
		X myX;
		X(X x) {
			this(i, myX.i, x.i); // same for super calls... only the first 2 field accesses are errors
		}
	}
	*/
	public FieldBinding findField(
		TypeBinding receiverType,
		char[] fieldName,
		InvocationSite invocationSite) {

		FieldBinding field = super.findField(receiverType, fieldName, invocationSite);
		if (field == null)
			return null;
		if (!field.isValidBinding())
			return field; // answer the error field
		if (field.isStatic())
			return field; // static fields are always accessible

		if (!isConstructorCall || receiverType != enclosingSourceType())
			return field;

		if (invocationSite instanceof SingleNameReference)
			return new ProblemFieldBinding(
				field.declaringClass,
				fieldName,
				NonStaticReferenceInConstructorInvocation);
		if (invocationSite instanceof QualifiedNameReference) {
			// look to see if the field is the first binding
			QualifiedNameReference name = (QualifiedNameReference) invocationSite;
			if (name.binding == null)
				// only true when the field is the fieldbinding at the beginning of name's tokens
				return new ProblemFieldBinding(
					field.declaringClass,
					fieldName,
					NonStaticReferenceInConstructorInvocation);
		}
		return field;
	}

	public boolean isInsideInitializer() {

		return (referenceContext instanceof TypeDeclaration);
	}

	public boolean isInsideInitializerOrConstructor() {

		return (referenceContext instanceof TypeDeclaration)
			|| (referenceContext instanceof ConstructorDeclaration);
	}

	/* Answer the problem reporter to use for raising new problems.
	 *
	 * Note that as a side-effect, this updates the current reference context
	 * (unit, type or method) in case the problem handler decides it is necessary
	 * to abort.
	 */
	public ProblemReporter problemReporter() {

		MethodScope outerMethodScope;
		if ((outerMethodScope = outerMostMethodScope()) == this) {
			ProblemReporter problemReporter = referenceCompilationUnit().problemReporter;
			problemReporter.referenceContext = referenceContext;
			return problemReporter;
		} else {
			return outerMethodScope.problemReporter();
		}
	}

	public final int recordInitializationStates(FlowInfo flowInfo) {

		if ((flowInfo == FlowInfo.DeadEnd) || (flowInfo.isFakeReachable())) {
			return -1;
		}
		UnconditionalFlowInfo unconditionalFlowInfo = flowInfo.unconditionalInits();
		long[] extraInits = unconditionalFlowInfo.extraDefiniteInits;
		long inits = unconditionalFlowInfo.definiteInits;
		checkNextEntry : for (int i = lastIndex; --i >= 0;) {
			if (definiteInits[i] == inits) {
				long[] otherInits = extraDefiniteInits[i];
				if ((extraInits != null) && (otherInits != null)) {
					if (extraInits.length == otherInits.length) {
						int j, max;
						for (j = 0, max = extraInits.length; j < max; j++) {
							if (extraInits[j] != otherInits[j]) {
								continue checkNextEntry;
							}
						}
						return i;
					}
				} else {
					if ((extraInits == null) && (otherInits == null)) {
						return i;
					}
				}
			}
		}

		// add a new entry
		if (definiteInits.length == lastIndex) {
			// need a resize
			System.arraycopy(
				definiteInits,
				0,
				(definiteInits = new long[lastIndex + 20]),
				0,
				lastIndex);
			System.arraycopy(
				extraDefiniteInits,
				0,
				(extraDefiniteInits = new long[lastIndex + 20][]),
				0,
				lastIndex);
		}
		definiteInits[lastIndex] = inits;
		if (extraInits != null) {
			extraDefiniteInits[lastIndex] = new long[extraInits.length];
			System.arraycopy(
				extraInits,
				0,
				extraDefiniteInits[lastIndex],
				0,
				extraInits.length);
		}
		return lastIndex++;
	}

	/* Answer the reference type of this scope.
	*
	* i.e. the nearest enclosing type of this scope.
	*/
	public TypeDeclaration referenceType() {

		return (TypeDeclaration) ((ClassScope) parent).referenceContext;
	}

	String basicToString(int tab) {

		String newLine = "\n"; //$NON-NLS-1$
		for (int i = tab; --i >= 0;)
			newLine += "\t"; //$NON-NLS-1$

		String s = newLine + "--- Method Scope ---"; //$NON-NLS-1$
		newLine += "\t"; //$NON-NLS-1$
		s += newLine + "locals:"; //$NON-NLS-1$
		for (int i = 0; i < localIndex; i++)
			s += newLine + "\t" + locals[i].toString(); //$NON-NLS-1$
		s += newLine + "startIndex = " + startIndex; //$NON-NLS-1$
		s += newLine + "isConstructorCall = " + isConstructorCall; //$NON-NLS-1$
		s += newLine + "fieldDeclarationIndex = " + fieldDeclarationIndex; //$NON-NLS-1$
		return s;
	}

}