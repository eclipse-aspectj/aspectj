/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.ajdt.internal.compiler.lookup;

import java.lang.reflect.Modifier;

import org.aspectj.bridge.ISourceLocation;
import org.aspectj.weaver.ConcreteTypeMunger;
import org.aspectj.weaver.NewConstructorTypeMunger;
import org.aspectj.weaver.NewFieldTypeMunger;
import org.aspectj.weaver.NewMethodTypeMunger;
import org.aspectj.weaver.ResolvedTypeMunger;
import org.aspectj.weaver.ResolvedTypeX;
//import org.aspectj.weaver.TypeX;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
//import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;


public class EclipseTypeMunger extends ConcreteTypeMunger {
	private ResolvedTypeX targetTypeX;
	//protected ReferenceBinding targetBinding = null;
	private AbstractMethodDeclaration sourceMethod;
	private EclipseFactory world;
	private ISourceLocation sourceLocation;
	

	public EclipseTypeMunger(EclipseFactory world, ResolvedTypeMunger munger, ResolvedTypeX aspectType,
								AbstractMethodDeclaration sourceMethod)
	{
		super(munger, aspectType);
		this.world = world;
		this.sourceMethod = sourceMethod;
		if (sourceMethod != null) {
			this.sourceLocation =
				new EclipseSourceLocation(sourceMethod.compilationResult, 
						sourceMethod.sourceStart, sourceMethod.sourceEnd);
			// Piece of magic that tells type mungers where they came from.
			// Won't be persisted unless ResolvedTypeMunger.persistSourceLocation is true.
			munger.setSourceLocation(sourceLocation);
		}
		targetTypeX = munger.getSignature().getDeclaringType().resolve(world.getWorld());
		//targetBinding = (ReferenceBinding)world.makeTypeBinding(targetTypeX);
	}
	
	public static boolean supportsKind(ResolvedTypeMunger.Kind kind) {
		return kind == ResolvedTypeMunger.Field
			|| kind == ResolvedTypeMunger.Method
			|| kind == ResolvedTypeMunger.Constructor;
	}

	public String toString() {
		return "(EclipseTypeMunger " + getMunger() + ")";
	}
	
	/**
	 * Modifies signatures of a TypeBinding through its ClassScope,
	 * i.e. adds Method|FieldBindings, plays with inheritance, ...
	 */
	public boolean munge(SourceTypeBinding sourceType) {
		if (!world.fromEclipse(sourceType).equals(targetTypeX)) return false; //??? move this test elsewhere
		//System.out.println("munging: " + sourceType);
//		System.out.println("match: " + world.fromEclipse(sourceType) +
//				" with " + targetTypeX);
		if (munger.getKind() == ResolvedTypeMunger.Field) {
			mungeNewField(sourceType, (NewFieldTypeMunger)munger);
		} else if (munger.getKind() == ResolvedTypeMunger.Method) {
			mungeNewMethod(sourceType, (NewMethodTypeMunger)munger);
		} else if (munger.getKind() == ResolvedTypeMunger.Constructor) {
			mungeNewConstructor(sourceType, (NewConstructorTypeMunger)munger);
		} else {
			throw new RuntimeException("unimplemented: " + munger.getKind());
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
		if (shouldTreatAsPublic() && !targetTypeX.isInterface()) {
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
		return sourceLocation;
	}

	public void setSourceLocation(ISourceLocation sourceLocation) {
		this.sourceLocation = sourceLocation;
	}

	/**
	 * @return AbstractMethodDeclaration
	 */
	public AbstractMethodDeclaration getSourceMethod() {
		return sourceMethod;
	}

}
