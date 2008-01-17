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

import java.lang.reflect.Modifier;
import java.util.Map;

import org.aspectj.bridge.ISourceLocation;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.aspectj.org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.TypeVariableBinding;
import org.aspectj.weaver.ConcreteTypeMunger;
import org.aspectj.weaver.NewConstructorTypeMunger;
import org.aspectj.weaver.NewFieldTypeMunger;
import org.aspectj.weaver.NewMethodTypeMunger;
import org.aspectj.weaver.ResolvedMember;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.ResolvedTypeMunger;
import org.aspectj.weaver.World;


public class EclipseTypeMunger extends ConcreteTypeMunger {
	private ResolvedType targetTypeX;
	//protected ReferenceBinding targetBinding = null;
	private AbstractMethodDeclaration sourceMethod;
	private EclipseFactory world;
	private ISourceLocation sourceLocation;
	

	public EclipseTypeMunger(EclipseFactory world, ResolvedTypeMunger munger, ResolvedType aspectType,
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
		// AMC, needed until generic and raw have distinct sigs...
		if (targetTypeX.isParameterizedType() || targetTypeX.isRawType()) targetTypeX = targetTypeX.getGenericType();
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
	public boolean munge(SourceTypeBinding sourceType, ResolvedType onType) {
		ResolvedType rt = onType;
		if (rt.isRawType() || rt.isParameterizedType()) rt = rt.getGenericType();
		boolean isExactTargetType = rt.equals(targetTypeX);
		if (!isExactTargetType) {
			// might be the topmost implementor of an interface we care about
			if (munger.getKind() != ResolvedTypeMunger.Method) return false;
			if (onType.isInterface()) return false;
			if (!munger.needsAccessToTopmostImplementor()) return false;
			// so we do need access, and this type could be it...
			if (!onType.isTopmostImplementor(targetTypeX)) return false;
			// we are the topmost implementor of an interface type that needs munging
			// but we only care about public methods here (we only do this at all to 
			// drive the JDT MethodVerifier correctly)
			if (!Modifier.isPublic(munger.getSignature().getModifiers())) return false;
		}
		//System.out.println("munging: " + sourceType);
//		System.out.println("match: " + world.fromEclipse(sourceType) +
//				" with " + targetTypeX);
		if (munger.getKind() == ResolvedTypeMunger.Field) {
			mungeNewField(sourceType, (NewFieldTypeMunger)munger);
		} else if (munger.getKind() == ResolvedTypeMunger.Method) {
			return mungeNewMethod(sourceType, onType, (NewMethodTypeMunger)munger, isExactTargetType);
		} else if (munger.getKind() == ResolvedTypeMunger.Constructor) {
			mungeNewConstructor(sourceType, (NewConstructorTypeMunger)munger);
		} else {
			throw new RuntimeException("unimplemented: " + munger.getKind());
		}
		return true;
	}
	

	private boolean mungeNewMethod(SourceTypeBinding sourceType, ResolvedType onType, NewMethodTypeMunger munger, boolean isExactTargetType) {
		InterTypeMethodBinding binding =
			new InterTypeMethodBinding(world, munger, aspectType, sourceMethod);

		if (!isExactTargetType) {
			// we're munging an interface ITD onto a topmost implementor
			ResolvedMember existingMember = onType.lookupMemberIncludingITDsOnInterfaces(getSignature()); 
			if (existingMember != null) {
				// already have an implementation, so don't do anything
				if (onType == existingMember.getDeclaringType() && Modifier.isFinal(munger.getSignature().getModifiers())) {
					// final modifier on default implementation is taken to mean that
					// no-one else can provide an implementation
					MethodBinding offendingBinding = sourceType.getExactMethod(binding.selector, binding.parameters, sourceType.scope.compilationUnitScope());
					sourceType.scope.problemReporter().finalMethodCannotBeOverridden(offendingBinding, binding);
				}
				// so that we find methods from our superinterfaces later on...
				findOrCreateInterTypeMemberFinder(sourceType);
				return false;
			} 
		}
		
		// retain *only* the visibility modifiers and abstract when putting methods on an interface...
		if (sourceType.isInterface()) {
			boolean isAbstract = (binding.modifiers & ClassFileConstants.AccAbstract) != 0;
			binding.modifiers = (binding.modifiers & (ClassFileConstants.AccPublic | ClassFileConstants.AccProtected | ClassFileConstants.AccPrivate));
			if (isAbstract) binding.modifiers |= ClassFileConstants.AccAbstract;
		}
		if (munger.getSignature().isVarargsMethod()) binding.modifiers |= ClassFileConstants.AccVarargs;
		findOrCreateInterTypeMemberFinder(sourceType).addInterTypeMethod(binding);
		return true;
	}
	
	
	private void mungeNewConstructor(SourceTypeBinding sourceType, NewConstructorTypeMunger munger) {		
		if (shouldTreatAsPublic()) {
			MethodBinding binding = world.makeMethodBinding(munger.getSignature(),munger.getTypeVariableAliases());
			findOrCreateInterTypeMemberFinder(sourceType).addInterTypeMethod(binding);
			TypeVariableBinding[] typeVariables = binding.typeVariables;
			for (int i = 0; i < typeVariables.length; i++) {
				TypeVariableBinding tv = typeVariables[i];
				String name = new String(tv.sourceName);
				TypeVariableBinding[] tv2 = sourceMethod.binding.typeVariables;
				for (int j = 0; j < tv2.length; j++) {
					TypeVariableBinding typeVariable = tv2[j];
					if (new String(tv2[j].sourceName).equals(name)) typeVariables[i].declaringElement = binding;
				}
			}
			for (int i = 0; i < typeVariables.length; i++) {
				if (typeVariables[i].declaringElement==null) throw new RuntimeException("Declaring element not set");
				
			}
			//classScope.referenceContext.binding.addMethod(binding);
		} else {
			InterTypeMethodBinding binding =
				new InterTypeMethodBinding(world, munger, aspectType, sourceMethod);
			findOrCreateInterTypeMemberFinder(sourceType).addInterTypeMethod(binding);
		}

	}
	

	private void mungeNewField(SourceTypeBinding sourceType, NewFieldTypeMunger munger) {		
		if (shouldTreatAsPublic() && !targetTypeX.isInterface()) {
			FieldBinding binding = world.makeFieldBinding(munger);
			findOrCreateInterTypeMemberFinder(sourceType).addInterTypeField(binding);
			//classScope.referenceContext.binding.addField(binding);
		} else {
			InterTypeFieldBinding binding =
				new InterTypeFieldBinding(world, munger, aspectType, sourceMethod);
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
	
	public ConcreteTypeMunger parameterizedFor(ResolvedType target) {
		return new EclipseTypeMunger(world,munger.parameterizedFor(target),aspectType,sourceMethod);
	}

	public ConcreteTypeMunger parameterizeWith(Map m,World w) {
		return new EclipseTypeMunger(world,munger.parameterizeWith(m,w),aspectType,sourceMethod);
	}

}
