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
	private EclipseWorld world;

	public EclipseTypeMunger(ResolvedTypeMunger munger, ResolvedTypeX aspectType,
								AbstractMethodDeclaration sourceMethod)
	{
		super(munger, aspectType);
		this.sourceMethod = sourceMethod;
		this.world = (EclipseWorld)aspectType.getWorld();
	}

	public String toString() {
		return "(EclipseTypeMunger " + getMunger() + ")";
	}
	
	private boolean match(SourceTypeBinding sourceType) {
		if (targetBinding == null) {
			TypeX targetTypeX = munger.getSignature().getDeclaringType();
			targetBinding = (ReferenceBinding)world.makeTypeBinding(targetTypeX);
		}
		//??? assumes instance uniqueness for ReferenceBindings
		return targetBinding == sourceType;
		
	}
	
	/**
	 * Modifies signatures of a TypeBinding through its ClassScope,
	 * i.e. adds Method|FieldBindings, plays with inheritance, ...
	 */
	public boolean munge(SourceTypeBinding sourceType) {
		if (!match(sourceType)) return false;
		
		if (munger.getKind() == ResolvedTypeMunger.Field) {
			mungeNewField(sourceType, (NewFieldTypeMunger)munger);
		} else if (munger.getKind() == ResolvedTypeMunger.Method) {
			mungeNewMethod(sourceType, (NewMethodTypeMunger)munger);
		} else if (munger.getKind() == ResolvedTypeMunger.Constructor) {
			mungeNewConstructor(sourceType, (NewConstructorTypeMunger)munger);
		} else {
			throw new RuntimeException("unimplemented");
		}
		return true;
	}
	

	private void mungeNewMethod(SourceTypeBinding sourceType, NewMethodTypeMunger munger) {
//		if (shouldTreatAsPublic()) {
//			MethodBinding binding = world.makeMethodBinding(munger.getSignature());
//			findOrCreateInterTypeMemberFinder(classScope).addInterTypeMethod(binding);
//			//classScope.referenceContext.binding.addMethod(binding);
//		} else {
			InterTypeMethodBinding binding =
				new InterTypeMethodBinding(world, munger.getSignature(), aspectType, sourceMethod);
			findOrCreateInterTypeMemberFinder(sourceType).addInterTypeMethod(binding);
//		}

	}
	private void mungeNewConstructor(SourceTypeBinding sourceType, NewConstructorTypeMunger munger) {		
		if (shouldTreatAsPublic()) {
			MethodBinding binding = world.makeMethodBinding(munger.getSignature());
			findOrCreateInterTypeMemberFinder(sourceType).addInterTypeMethod(binding);
			//classScope.referenceContext.binding.addMethod(binding);
		} else {
			InterTypeMethodBinding binding =
				new InterTypeMethodBinding(world, munger.getSignature(), aspectType, sourceMethod);
			findOrCreateInterTypeMemberFinder(sourceType).addInterTypeMethod(binding);
		}

	}

	private void mungeNewField(SourceTypeBinding sourceType, NewFieldTypeMunger munger) {		
		if (shouldTreatAsPublic() && !targetBinding.isInterface()) {
			FieldBinding binding = world.makeFieldBinding(munger.getSignature());
			findOrCreateInterTypeMemberFinder(sourceType).addInterTypeField(binding);
			//classScope.referenceContext.binding.addField(binding);
		} else {
			InterTypeFieldBinding binding =
				new InterTypeFieldBinding(world, munger.getSignature(), aspectType, sourceMethod);
			findOrCreateInterTypeMemberFinder(sourceType).addInterTypeField(binding);
		}
	}
	
	
	private boolean shouldTreatAsPublic() {
		//??? this is where we could fairly easily choose to treat package-protected
		//??? introductions like public ones when the target type and the aspect
		//??? are in the same package
		return Modifier.isPublic(munger.getSignature().getModifiers());
	}
	
	
	private InterTypeMemberFinder findOrCreateInterTypeMemberFinder(SourceTypeBinding sourceType) {
		InterTypeMemberFinder finder = 
			(InterTypeMemberFinder)sourceType.memberFinder;
		if (finder == null) {
			finder = new InterTypeMemberFinder();
			sourceType.memberFinder = finder;
			finder.sourceTypeBinding = sourceType;
		}
		return finder;
	}
	
	public ISourceLocation getSourceLocation() {
		return new EclipseSourceLocation(sourceMethod.compilationResult, 
					sourceMethod.sourceStart, sourceMethod.sourceEnd);
	}

}
