/* *******************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *   Adrian Colyer			Initial implementation
 * ******************************************************************/
package org.aspectj.weaver;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.aspectj.weaver.patterns.PerClause;

/**
 * A BoundedReferenceType is the result of a generics wildcard expression
 * ? extends String, ? super Foo etc..
 * 
 * The "signature" for a bounded reference type follows the generic signature
 * specification in section 4.4 of JVM spec: *,+,- plus signature strings.
 * 
 * The bound may be a type variable (e.g. ? super T)
 */
public class BoundedReferenceType extends ReferenceType {

	protected ReferenceType[] additionalInterfaceBounds = new ReferenceType[0];
	
	protected boolean isExtends = true;
	protected boolean isSuper   = false;
	
	public BoundedReferenceType(ReferenceType aBound, boolean isExtends, World world) {
		super((isExtends ? "+" : "-") + aBound.signature,aBound.signatureErasure,world);
		this.isExtends = isExtends; 
		this.isSuper   = !isExtends;
		if (isExtends) { 
			setUpperBound(aBound);
		} else {
			setLowerBound(aBound);
			setUpperBound(world.resolve(UnresolvedType.OBJECT));
		}
		setDelegate(new ReferenceTypeReferenceTypeDelegate((ReferenceType)getUpperBound()));
	}
		
	public BoundedReferenceType(ReferenceType aBound, boolean isExtends, World world, ReferenceType[] additionalInterfaces) {
		this(aBound,isExtends,world);
		this.additionalInterfaceBounds = additionalInterfaces;
	}
	
	public ReferenceType[] getAdditionalBounds() {
		return additionalInterfaceBounds;
	}
	
	public UnresolvedType parameterize(Map typeBindings) {
		ReferenceType[] parameterizedAdditionalInterfaces = new ReferenceType[additionalInterfaceBounds==null?0:additionalInterfaceBounds.length];
		for (int i=0; i<parameterizedAdditionalInterfaces.length;i++) {
			parameterizedAdditionalInterfaces[i] = (ReferenceType)additionalInterfaceBounds[i].parameterize(typeBindings);
		}
		if (isExtends) {
			return new BoundedReferenceType((ReferenceType)getUpperBound().parameterize(typeBindings),isExtends,world,parameterizedAdditionalInterfaces);
		} else {
			return new BoundedReferenceType((ReferenceType)getLowerBound().parameterize(typeBindings),isExtends,world,parameterizedAdditionalInterfaces);
		}
	}
	
	/**
	 * only for use when resolving GenericsWildcardTypeX or a TypeVariableReferenceType
	 */
	protected BoundedReferenceType(String sig, String sigErasure, World world) {
		super(sig, sigErasure, world);
		setUpperBound(world.resolve(UnresolvedType.OBJECT));
		setDelegate(new ReferenceTypeReferenceTypeDelegate((ReferenceType)getUpperBound()));
	}
	
	public ReferenceType[] getInterfaceBounds() { 
		return additionalInterfaceBounds; 
	}
	
	public boolean hasLowerBound() {
		return getLowerBound() != null;
	}
	
	public boolean isExtends() { return (isExtends && !getUpperBound().getSignature().equals("Ljava/lang/Object;")); }
	public boolean isSuper()   { return isSuper;   }
	
	public boolean alwaysMatches(ResolvedType aCandidateType) {
		if (isExtends()) {
			// aCandidateType must be a subtype of upperBound
			return ((ReferenceType)getUpperBound()).isAssignableFrom(aCandidateType);
		} else if (isSuper()) {
			// aCandidateType must be a supertype of lowerBound
			return aCandidateType.isAssignableFrom((ReferenceType)getLowerBound());
		} else {
			return true; // straight '?'
		}
	}
	
	// this "maybe matches" that
	public boolean canBeCoercedTo(ResolvedType aCandidateType) {
		if (alwaysMatches(aCandidateType)) return true;
		if (aCandidateType.isGenericWildcard()) {
			ResolvedType myUpperBound = (ResolvedType) getUpperBound();
			ResolvedType myLowerBound = (ResolvedType) getLowerBound();
			if (isExtends()) {
				if (aCandidateType.isExtends()) {
					return myUpperBound.isAssignableFrom((ResolvedType)aCandidateType.getUpperBound()); 
				} else if (aCandidateType.isSuper()) {
					return myUpperBound == aCandidateType.getLowerBound();
				} else {
					return true;  // it's '?'
				}
			} else if (isSuper()) {
				if (aCandidateType.isSuper()) {
					return ((ResolvedType)aCandidateType.getLowerBound()).isAssignableFrom(myLowerBound);
				} else if (aCandidateType.isExtends()) {
					return myLowerBound == aCandidateType.getUpperBound();
				} else {
					return true;
				}
			} else {
				return true;
			}
		} else {
			return false;
		}
	}
	
	public String getSimpleName() {
		if (!isExtends() && !isSuper()) return "?";
		if (isExtends()) {
			return ("? extends " + getUpperBound().getSimpleName());
		} else {
			return ("? super " + getLowerBound().getSimpleName());
		}
	}
	
	// override to include additional interface bounds...
	public ResolvedType[] getDeclaredInterfaces() {
		ResolvedType[] interfaces = super.getDeclaredInterfaces();
		if (additionalInterfaceBounds.length > 0) {
			ResolvedType[] allInterfaces = 
				new ResolvedType[interfaces.length + additionalInterfaceBounds.length];
			System.arraycopy(interfaces, 0, allInterfaces, 0, interfaces.length);
			System.arraycopy(additionalInterfaceBounds,0,allInterfaces,interfaces.length,additionalInterfaceBounds.length);
			return allInterfaces;
		} else {
			return interfaces;
		}
	}
	
	public boolean isGenericWildcard() {
		return true;
	}
	
	protected static class ReferenceTypeReferenceTypeDelegate extends AbstractReferenceTypeDelegate {

		public ReferenceTypeReferenceTypeDelegate(ReferenceType backing) {
			super(backing,false);
		}
		
		public void addAnnotation(AnnotationX annotationX) {
			throw new UnsupportedOperationException("What on earth do you think you are doing???");
		}

		public boolean isAspect() {
			return resolvedTypeX.isAspect();
		}

		public boolean isAnnotationStyleAspect() {
			return resolvedTypeX.isAnnotationStyleAspect();
		}

		public boolean isInterface() {
			return resolvedTypeX.isInterface();
		}

		public boolean isEnum() {
			return resolvedTypeX.isEnum();
		}

		public boolean isAnnotation() {
			return resolvedTypeX.isAnnotation();
		}

		public boolean isAnnotationWithRuntimeRetention() {
			return resolvedTypeX.isAnnotationWithRuntimeRetention();
		}
		
		public boolean isAnonymous() {
			return resolvedTypeX.isAnonymous();
		}
		
		public boolean isNested() {
			return resolvedTypeX.isNested();
		}
		
		public ResolvedType getOuterClass() {
			return resolvedTypeX.getOuterClass();
		}
		
		public String getRetentionPolicy() {
			return resolvedTypeX.getRetentionPolicy();
		}

		public boolean canAnnotationTargetType() {
			return resolvedTypeX.canAnnotationTargetType();
		}
		
		public AnnotationTargetKind[] getAnnotationTargetKinds() {
			return resolvedTypeX.getAnnotationTargetKinds();
		}
		
		public boolean isGeneric() {
			return resolvedTypeX.isGenericType();
		}

		public String getDeclaredGenericSignature() {
			return resolvedTypeX.getDeclaredGenericSignature();
		}
		
		public boolean hasAnnotation(UnresolvedType ofType) {
			return resolvedTypeX.hasAnnotation(ofType);
		}

		public AnnotationX[] getAnnotations() {
			return resolvedTypeX.getAnnotations();
		}

		public ResolvedType[] getAnnotationTypes() {
			return resolvedTypeX.getAnnotationTypes();
		}

		public ResolvedMember[] getDeclaredFields() {
			return resolvedTypeX.getDeclaredFields();
		}

		public ResolvedType[] getDeclaredInterfaces() {
			return resolvedTypeX.getDeclaredInterfaces();
		}

		public ResolvedMember[] getDeclaredMethods() {
			return resolvedTypeX.getDeclaredMethods();
		}

		public ResolvedMember[] getDeclaredPointcuts() {
			return resolvedTypeX.getDeclaredPointcuts();
		}

		public PerClause getPerClause() {
			return resolvedTypeX.getPerClause();
		}

		public Collection getDeclares() {
			return resolvedTypeX.getDeclares();
		}

		public Collection getTypeMungers() {
			return resolvedTypeX.getTypeMungers();
		}

		public Collection getPrivilegedAccesses() {
			return Collections.EMPTY_LIST;
		}

		public int getModifiers() {
			return resolvedTypeX.getModifiers();
		}

		public ResolvedType getSuperclass() {
			return resolvedTypeX.getSuperclass();
		}

		public WeaverStateInfo getWeaverState() {
			return null;
		}

		public TypeVariable[] getTypeVariables() {
			return resolvedTypeX.getTypeVariables();
		}

		public void ensureDelegateConsistent() {
			resolvedTypeX.getDelegate().ensureDelegateConsistent();
		}

	}
}
