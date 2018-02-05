/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 * ******************************************************************/

package org.aspectj.ajdt.internal.compiler.lookup;

import org.aspectj.weaver.AjcMemberMaker;
import org.aspectj.weaver.ResolvedTypeMunger;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.InvocationSite;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.RawTypeBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.SyntheticMethodBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

public class InterTypeFieldBinding extends FieldBinding {
	public ReferenceBinding targetType;
	public SyntheticMethodBinding reader;
	public SyntheticMethodBinding writer;
	public AbstractMethodDeclaration sourceMethod;

	public InterTypeFieldBinding(EclipseFactory world, ResolvedTypeMunger munger, UnresolvedType withinType,
			AbstractMethodDeclaration sourceMethod) {
		super(world.makeFieldBinding(munger.getSignature(), munger.getTypeVariableAliases()), null);
		this.sourceMethod = sourceMethod;

		targetType = (ReferenceBinding) world.makeTypeBinding(munger.getSignature().getDeclaringType());
		this.declaringClass = (ReferenceBinding) world.makeTypeBinding(withinType);
		// We called the super() with null, we must now do the last step that will have been skipped because of this, see the
		// supers() final line:
		// OPTIMIZE dont makeFieldBinding twice, HORRIBLE
		setAnnotations(world.makeFieldBinding(munger.getSignature(), munger.getTypeVariableAliases()).getAnnotations(), false);

		reader = new SimpleSyntheticAccessMethodBinding(world.makeMethodBinding(AjcMemberMaker.interFieldGetDispatcher(munger
				.getSignature(), withinType)));

		writer = new SimpleSyntheticAccessMethodBinding(world.makeMethodBinding(AjcMemberMaker.interFieldSetDispatcher(munger
				.getSignature(), withinType)));
	}

	public boolean canBeSeenBy(TypeBinding receiverType, InvocationSite invocationSite, Scope scope) {
		scope.compilationUnitScope().recordTypeReference(declaringClass);
		// System.err.println("canBeSeenBy: " + this + ", " + isPublic());
		if (isPublic())
			return true;

		SourceTypeBinding invocationType = scope.invocationType();
		// System.out.println("receiver: " + receiverType + ", " + invocationType);
		ReferenceBinding declaringType = declaringClass;

		if (invocationType == null) // static import call
			return !isPrivate() && scope.getCurrentPackage() == receiverType.getPackage();

		// FIXME asc what about parameterized types and private ITD generic fields on interfaces?

		// Don't work with a raw type, work with the generic type
		if (declaringClass.isRawType())
			declaringType = ((RawTypeBinding) declaringClass).type;

		if (invocationType == declaringType)
			return true;

		// if (invocationType.isPrivileged) {
		// System.out.println("privileged access to: " + this);
		// return true;
		// }

		if (isProtected()) {
			throw new RuntimeException("unimplemented");
		}

		// XXX make sure this walks correctly
		if (isPrivate()) {
			// answer true if the receiverType is the declaringClass
			// AND the invocationType and the declaringClass have a common enclosingType

			// see pr149071 - it has caused me to comment out this block below - what
			// is it trying to achieve? Possibly it should be using the scope.parentScope (the class scope of
			// where the reference is being made) rather than the receiver type

			// Is the receiverType an innertype of the declaring type?
			// boolean receiverTypeIsSameOrInsideDeclaringType = receiverType == declaringType;
			// ReferenceBinding typeToCheckNext = receiverType.enclosingType();
			// while (!receiverTypeIsSameOrInsideDeclaringType && typeToCheckNext!=null) {
			// if (typeToCheckNext==declaringType) receiverTypeIsSameOrInsideDeclaringType=true;
			// }
			// if (!receiverTypeIsSameOrInsideDeclaringType) return false;

			// the code above replaces this line: (pr118698)
			// if (receiverType != declaringType) return false;

			if (invocationType != declaringType) {
				ReferenceBinding outerInvocationType = invocationType;
				ReferenceBinding temp = outerInvocationType.enclosingType();
				while (temp != null) {
					outerInvocationType = temp;
					temp = temp.enclosingType();
				}

				ReferenceBinding outerDeclaringClass = declaringType;
				temp = outerDeclaringClass.enclosingType();
				while (temp != null) {
					outerDeclaringClass = temp;
					temp = temp.enclosingType();
				}
				if (outerInvocationType != outerDeclaringClass)
					return false;
			}
			return true;
		}

		// isDefault()
		if (invocationType.fPackage == declaringClass.fPackage)
			return true;
		return false;
	}

	public SyntheticMethodBinding getAccessMethod(boolean isReadAccess) {
		if (isReadAccess)
			return reader;
		else
			return writer;
	}

	public boolean alwaysNeedsAccessMethod(boolean isReadAccess) {
		return true;
	}

	public ReferenceBinding getTargetType() {
		return targetType;
	}

	// overrides ITD'd method in FieldBinding...
	public ReferenceBinding getOwningClass() {
		return targetType;
	}

}
