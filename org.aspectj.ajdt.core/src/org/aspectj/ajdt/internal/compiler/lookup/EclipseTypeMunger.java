/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.ajdt.internal.compiler.lookup;

import java.lang.reflect.Modifier;

import org.aspectj.bridge.*;
import org.aspectj.bridge.ISourceLocation;
import org.aspectj.weaver.*;
import org.aspectj.weaver.ConcreteTypeMunger;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;


public class EclipseTypeMunger extends ConcreteTypeMunger {
	protected ReferenceBinding targetBinding = null;
	private AbstractMethodDeclaration sourceMethod;

	public EclipseTypeMunger(ResolvedTypeMunger munger, ResolvedTypeX aspectType,
								AbstractMethodDeclaration sourceMethod)
	{
		super(munger, aspectType);
		this.sourceMethod = sourceMethod;
	}

	public String toString() {
		return "(EclipseTypeMunger " + getMunger() + ")";
	}
	
	private boolean match(ClassScope scope) {
		if (targetBinding == null) {
			TypeX targetTypeX = munger.getSignature().getDeclaringType();
			targetBinding =
				(ReferenceBinding)EclipseWorld.fromScopeLookupEnvironment(scope).makeTypeBinding(targetTypeX);
		}
		//??? assumes instance uniqueness for ReferenceBindings
		return targetBinding == scope.referenceContext.binding;
		
	}
	
	/**
	 * Modifies signatures of a TypeBinding through its ClassScope,
	 * i.e. adds Method|FieldBindings, plays with inheritance, ...
	 */
	public boolean munge(ClassScope classScope) {
		if (!match(classScope)) return false;
		
		if (munger.getKind() == ResolvedTypeMunger.Field) {
			mungeNewField(classScope, (NewFieldTypeMunger)munger);
		} else if (munger.getKind() == ResolvedTypeMunger.Method) {
			mungeNewMethod(classScope, (NewMethodTypeMunger)munger);
		} else if (munger.getKind() == ResolvedTypeMunger.Constructor) {
			mungeNewConstructor(classScope, (NewConstructorTypeMunger)munger);
		} else {
			throw new RuntimeException("unimplemented");
		}
		return true;
	}
	

	private void mungeNewMethod(ClassScope classScope, NewMethodTypeMunger munger) {
		EclipseWorld world = EclipseWorld.fromScopeLookupEnvironment(classScope);
		
//		if (shouldTreatAsPublic()) {
//			MethodBinding binding = world.makeMethodBinding(munger.getSignature());
//			findOrCreateInterTypeMemberFinder(classScope).addInterTypeMethod(binding);
//			//classScope.referenceContext.binding.addMethod(binding);
//		} else {
			InterTypeMethodBinding binding =
				new InterTypeMethodBinding(world, munger.getSignature(), aspectType, sourceMethod);
			findOrCreateInterTypeMemberFinder(classScope).addInterTypeMethod(binding);
//		}

	}
	private void mungeNewConstructor(ClassScope classScope, NewConstructorTypeMunger munger) {
		EclipseWorld world = EclipseWorld.fromScopeLookupEnvironment(classScope);
		
		if (shouldTreatAsPublic()) {
			MethodBinding binding = world.makeMethodBinding(munger.getSignature());
			findOrCreateInterTypeMemberFinder(classScope).addInterTypeMethod(binding);
			//classScope.referenceContext.binding.addMethod(binding);
		} else {
			InterTypeMethodBinding binding =
				new InterTypeMethodBinding(world, munger.getSignature(), aspectType, sourceMethod);
			findOrCreateInterTypeMemberFinder(classScope).addInterTypeMethod(binding);
		}

	}

	private void mungeNewField(ClassScope classScope, NewFieldTypeMunger munger) {
		EclipseWorld world = EclipseWorld.fromScopeLookupEnvironment(classScope);
		
		if (shouldTreatAsPublic() && !targetBinding.isInterface()) {
			FieldBinding binding = world.makeFieldBinding(munger.getSignature());
			findOrCreateInterTypeMemberFinder(classScope).addInterTypeField(binding);
			//classScope.referenceContext.binding.addField(binding);
		} else {
			InterTypeFieldBinding binding =
				new InterTypeFieldBinding(world, munger.getSignature(), aspectType, sourceMethod);
			findOrCreateInterTypeMemberFinder(classScope).addInterTypeField(binding);
		}
	}
	
	
	private boolean shouldTreatAsPublic() {
		//??? this is where we could fairly easily choose to treat package-protected
		//??? introductions like public ones when the target type and the aspect
		//??? are in the same package
		return Modifier.isPublic(munger.getSignature().getModifiers());
	}
	
	
	private InterTypeMemberFinder findOrCreateInterTypeMemberFinder(ClassScope classScope) {
		InterTypeMemberFinder finder = 
			(InterTypeMemberFinder)classScope.referenceContext.binding.memberFinder;
		if (finder == null) {
			finder = new InterTypeMemberFinder();
			classScope.referenceContext.binding.memberFinder = finder;
			finder.sourceTypeBinding = classScope.referenceContext.binding;
		}
		return finder;
	}
	
	public ISourceLocation getSourceLocation() {
		return new EclipseSourceLocation(sourceMethod.compilationResult, 
					sourceMethod.sourceStart, sourceMethod.sourceEnd);
	}

}
