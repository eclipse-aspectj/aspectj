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

import org.aspectj.weaver.patterns.PerClause;

/**
 * A BoundedReferenceType is the result of a generics wildcard expression
 * ? extends String, ? super Foo etc..
 * The "signature" for a bounded reference type follows the generic signature
 * specification in section 4.4 of JVM spec: *,+,- plus signature strings
 */
public class BoundedReferenceType extends ReferenceType {
	protected ReferenceType upperBound;
	protected ReferenceType[] additionalInterfaceBounds = new ReferenceType[0];
	protected ReferenceType lowerBound = null;
	protected boolean isExtends = true;
	protected boolean isSuper = false;
	
	public BoundedReferenceType(ReferenceType aBound, boolean isExtends, World world) {
		super((isExtends ? "+" : "-") + aBound.signature,world);
		this.isExtends = isExtends; this.isSuper=!isExtends;
		if (isExtends) { 
			this.upperBound = aBound;
		} else {
			this.lowerBound = aBound;
			this.upperBound = (ReferenceType) world.resolve(TypeX.OBJECT);
		}
		setDelegate(new ReferenceTypeReferenceTypeDelegate(upperBound));
	}
	
	public BoundedReferenceType(ReferenceType aBound, boolean isExtends, World world, ReferenceType[] additionalInterfaces) {
		this(aBound,isExtends,world);
		this.additionalInterfaceBounds = additionalInterfaces;
	}
	
	/**
	 * only for use when resolving GenericsWildcardTypeX or a TypeVariableReferenceType
	 */
	BoundedReferenceType(String sig, World world) {
		super(sig,world);
		this.upperBound = (ReferenceType) world.resolve(TypeX.OBJECT);
	}
	
	public ReferenceType getUpperBound() { return upperBound; }
	
	public ReferenceType[] getInterfaceBounds() { return additionalInterfaceBounds; }
	
	public boolean hasLowerBound() {
		return lowerBound != null;
	}
	
	public ReferenceType getLowerBound() { return lowerBound; }
	
	public boolean isExtends() { return isExtends; }
	public boolean isSuper() { return isSuper; }
	
	// override to include additional interface bounds...
	public ResolvedTypeX[] getDeclaredInterfaces() {
		ResolvedTypeX[] interfaces = super.getDeclaredInterfaces();
		if (additionalInterfaceBounds.length > 0) {
			ResolvedTypeX[] allInterfaces = 
				new ResolvedTypeX[interfaces.length + additionalInterfaceBounds.length];
			System.arraycopy(interfaces, 0, allInterfaces, 0, interfaces.length);
			System.arraycopy(additionalInterfaceBounds,0,allInterfaces,interfaces.length,additionalInterfaceBounds.length);
			return allInterfaces;
		} else {
			return interfaces;
		}
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

		public boolean isGeneric() {
			return resolvedTypeX.isGeneric();
		}

		public String getDeclaredGenericSignature() {
			return resolvedTypeX.getDeclaredGenericSignature();
		}
		
		public boolean hasAnnotation(TypeX ofType) {
			return resolvedTypeX.hasAnnotation(ofType);
		}

		public AnnotationX[] getAnnotations() {
			return resolvedTypeX.getAnnotations();
		}

		public ResolvedTypeX[] getAnnotationTypes() {
			return resolvedTypeX.getAnnotationTypes();
		}

		public ResolvedMember[] getDeclaredFields() {
			return resolvedTypeX.getDeclaredFields();
		}

		public ResolvedTypeX[] getDeclaredInterfaces() {
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

		public ResolvedTypeX getSuperclass() {
			return resolvedTypeX.getSuperclass();
		}

		public WeaverStateInfo getWeaverState() {
			return null;
		}

		public TypeVariable[] getTypeVariables() {
			return resolvedTypeX.getTypeVariables();
		}

	}
}
