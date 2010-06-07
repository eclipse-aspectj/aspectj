/* *******************************************************************
 * Copyright (c) 2010 Contributors
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Andy Clement - SpringSource
 * ******************************************************************/
package org.aspectj.ajdt.internal.compiler.ast;

import java.lang.reflect.Modifier;
import java.util.Collections;

import org.aspectj.ajdt.internal.compiler.lookup.EclipseFactory;
import org.aspectj.ajdt.internal.compiler.lookup.EclipseTypeMunger;
import org.aspectj.ajdt.internal.compiler.lookup.InterTypeScope;
import org.aspectj.org.eclipse.jdt.internal.compiler.CompilationResult;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.CompilationUnitScope;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.aspectj.weaver.NewMemberClassTypeMunger;
import org.aspectj.weaver.ResolvedType;

/**
 * Represents an intertype member class declaration.
 * 
 * @author Andy Clement
 * @since 1.6.9
 */
public class IntertypeMemberClassDeclaration extends TypeDeclaration {

	// The target type for this inner class
	private TypeReference onType;
	private ReferenceBinding onTypeResolvedBinding;
	protected InterTypeScope interTypeScope;
	// When set to true, the scope hierarchy for the field/method declaration has been correctly modified to include an intertype
	// scope which resolves things relative to the targeted type.
	private boolean scopeSetup = false;

	public IntertypeMemberClassDeclaration(CompilationResult compilationResult) {
		super(compilationResult);
	}

	@Override
	public void resolve(ClassScope aspectScope) {
		resolveOnType(aspectScope);
		ensureScopeSetup();
		super.resolve(aspectScope);
	}

	@Override
	public void resolve() {
		super.resolve();
	}

	@Override
	public void resolve(BlockScope blockScope) {
		throw new IllegalStateException();
	}

	@Override
	public void resolve(CompilationUnitScope upperScope) {
		throw new IllegalStateException();
	}

	/**
	 * Called just before the compiler is going to start resolving elements of a declaration, this method adds an intertype scope so
	 * that elements of the type targeted by the ITD can be resolved. For example, if type variables are referred to in the ontype
	 * for the ITD, they have to be resolved against the ontype, not the aspect containing the ITD.
	 */
	public void ensureScopeSetup() {
		if (scopeSetup) {
			return; // don't do it again
		}
		ClassScope scope = this.scope;

		// TODO [inner] ton of stuff related to parameterization support

		// if (ot instanceof ParameterizedQualifiedTypeReference) { // pr132349
		// ParameterizedQualifiedTypeReference pref = (ParameterizedQualifiedTypeReference) ot;
		// if (pref.typeArguments != null && pref.typeArguments.length != 0) {
		// boolean usingNonTypeVariableInITD = false;
		// // Check if any of them are not type variables
		// for (int i = 0; i < pref.typeArguments.length; i++) {
		// TypeReference[] refs = pref.typeArguments[i];
		// for (int j = 0; refs != null && j < refs.length; j++) {
		// TypeBinding tb = refs[j].getTypeBindingPublic(scope.parent);
		// if (!tb.isTypeVariable() && !(tb instanceof ProblemReferenceBinding)) {
		// usingNonTypeVariableInITD = true;
		// }
		//
		// }
		// }
		// if (usingNonTypeVariableInITD) {
		// scope.problemReporter().signalError(sourceStart, sourceEnd,
		// "Cannot make inter-type declarations on parameterized types");
		// // to prevent disgusting cascading errors after this problem - lets null out what leads to them (pr105038)
		// this.arguments = null;
		// this.returnType = new SingleTypeReference(TypeReference.VOID, 0L);
		//
		// this.ignoreFurtherInvestigation = true;
		// ReferenceBinding closestMatch = null;
		// rb = new ProblemReferenceBinding(ot.getParameterizedTypeName(), closestMatch, 0);
		// onType = null;
		// }
		// }
		//
		// }

		// // Work out the real base type
		// if (onType instanceof ParameterizedSingleTypeReference) {
		// ParameterizedSingleTypeReference pref = (ParameterizedSingleTypeReference) ot;
		// long pos = (((long) pref.sourceStart) << 32) | pref.sourceEnd;
		// ot = new SingleTypeReference(pref.token, pos);
		// } else if (ot instanceof ParameterizedQualifiedTypeReference) {
		// ParameterizedQualifiedTypeReference pref = (ParameterizedQualifiedTypeReference) ot;
		// long pos = (((long) pref.sourceStart) << 32) | pref.sourceEnd;
		// ot = new QualifiedTypeReference(pref.tokens, new long[] { pos });// SingleTypeReference(pref.Quatoken,pos);
		// }

		// resolve it
		// if (rb == null) {
		// rb = (ReferenceBinding) ot.getTypeBindingPublic(scope.parent);
		// }

		// pr203646 - if we have ended up with the raw type, get back to the underlying generic one.
		// if (rb.isRawType() && rb.isMemberType()) {
		// // if the real target type used a type variable alias then we can do this OK, but need to switch things around, we want
		// // the generic type
		// rb = ((RawTypeBinding) rb).type;
		// }

		// if (rb instanceof TypeVariableBinding) {
		// scope.problemReporter().signalError(sourceStart, sourceEnd,
		// "Cannot make inter-type declarations on type variables, use an interface and declare parents");
		// // to prevent disgusting cascading errors after this problem - lets null out what leads to them (pr105038)
		// this.arguments = null;
		// this.returnType = new SingleTypeReference(TypeReference.VOID, 0L);
		//
		// this.ignoreFurtherInvestigation = true;
		// ReferenceBinding closestMatch = null;
		// if (((TypeVariableBinding) rb).firstBound != null) {
		// closestMatch = ((TypeVariableBinding) rb).firstBound.enclosingType();
		// }
		// rb = new ProblemReferenceBinding(rb.compoundName, closestMatch, 0);
		// }

		// if resolution failed, give up - someone else is going to report an error
		// if (rb instanceof ProblemReferenceBinding) {
		// return;
		// }

		interTypeScope = new InterTypeScope(scope.parent, onTypeResolvedBinding, Collections.emptyList());
		// FIXME asc verify the choice of lines here...
		// Two versions of this next line.
		// First one tricks the JDT variable processing code so that it won't complain if
		// you refer to a type variable from a static ITD - it *is* a problem and it *will* be caught, but later and
		// by the AJDT code so we can put out a much nicer message.
		// scope.isStatic = (typeVariableAliases != null ? false : Modifier.isStatic(declaredModifiers));
		// this is the original version in case tricking the JDT causes grief (if you reinstate this variant, you
		// will need to change the expected messages output for some of the generic ITD tests)
		// scope.isStatic = Modifier.isStatic(declaredModifiers);
		scope.parent = interTypeScope;
		scopeSetup = true;
	}

	public void setOnType(TypeReference onType) {
		this.onType = onType;
	}

	private void resolveOnType(ClassScope cuScope) {
		if (onType == null || onTypeResolvedBinding != null) {
			return;
		} // error reported elsewhere.

		onTypeResolvedBinding = (ReferenceBinding) onType.getTypeBindingPublic(cuScope);
		if (!onTypeResolvedBinding.isValidBinding()) {
			cuScope.problemReporter().invalidType(onType, onTypeResolvedBinding);
			ignoreFurtherInvestigation = true;
		}
	}

	public EclipseTypeMunger build(ClassScope classScope) {
		EclipseFactory world = EclipseFactory.fromScopeLookupEnvironment(classScope);
		resolveOnType(classScope);
		ensureScopeSetup();

		if (ignoreFurtherInvestigation) {
			return null;
		}

		if (onTypeResolvedBinding.isInterface() || onTypeResolvedBinding.isEnum() || onTypeResolvedBinding.isAnnotationType()) {
			scope.problemReporter().signalError(
					sourceStart,
					sourceEnd,
					"Cannot declare new member type on '" + onType.toString()
							+ "'. New member types can only be specified on classes (compiler limitation)");
			return null;
		}

		if (!Modifier.isStatic(modifiers)) {
			scope.problemReporter().signalError(sourceStart, sourceEnd,
					"Intertype declared member types can only be static (compiler limitation)");
			return null;
		}

		ResolvedType declaringType = world.fromBinding(onTypeResolvedBinding).resolve(world.getWorld());
		if (declaringType.isRawType() || declaringType.isParameterizedType()) {
			declaringType = declaringType.getGenericType();
		}

		if (interTypeScope == null) {
			return null; // We encountered a problem building the scope, don't continue - error already reported
		}

		// TODO [inner] use the interTypeScope.getRecoveryAliases
		// TODO [inner] should mark it in the aspect as unreachable - it is not to be considered part of the aspect
		NewMemberClassTypeMunger newMemberClassTypeMunger = new NewMemberClassTypeMunger(declaringType, new String(this.name));
		ResolvedType aspectType = world.fromEclipse(classScope.referenceContext.binding);
		return new EclipseTypeMunger(world, newMemberClassTypeMunger, aspectType, null);
	}
}
