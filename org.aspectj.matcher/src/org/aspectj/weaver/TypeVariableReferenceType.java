/* *******************************************************************
 * Copyright (c) 2005-2010 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html
 * ******************************************************************/
package org.aspectj.weaver;

import java.util.Map;

/**
 * ReferenceType representing a type variable. The delegate for this reference type is the upperbound on the type variable (so
 * Object if not otherwise specified).
 * 
 * @author Adrian Colyer
 * @author Andy Clement
 */
public class TypeVariableReferenceType extends ReferenceType implements TypeVariableReference {

	private TypeVariable typeVariable;

	// If 'fixedUp' then the type variable in here is a reference to the real one that may
	// exist either on a member or a type. Not fixedUp means that we unpacked a generic
	// signature and weren't able to fix it up during resolution (didn't quite know enough
	// at the right time). Wonder if we can fix it up late?
	boolean fixedUp = false;

	public TypeVariableReferenceType(TypeVariable typeVariable, World world) {
		super(typeVariable.getGenericSignature(), typeVariable.getErasureSignature(), world);
		this.typeVariable = typeVariable;
		// setDelegate(new BoundedReferenceTypeDelegate(backing));
		// this.isExtends = false;
		// this.isSuper = false;
	}

	/**
	 * For a TypeVariableReferenceType the delegate is the delegate for the first bound.
	 */
	@Override
	public ReferenceTypeDelegate getDelegate() {
		if (this.delegate == null) {
			ResolvedType resolvedFirstBound = typeVariable.getFirstBound().resolve(world);
			BoundedReferenceTypeDelegate brtd = null;
			if (resolvedFirstBound.isMissing()) {
				brtd = new BoundedReferenceTypeDelegate((ReferenceType) world.resolve(UnresolvedType.OBJECT));
				setDelegate(brtd); // set now because getSourceLocation() below will cause a recursive step to discover the delegate
				world.getLint().cantFindType.signal(
						"Unable to find type for generic bound.  Missing type is " + resolvedFirstBound.getName(),
						getSourceLocation());
			} else {
				brtd = new BoundedReferenceTypeDelegate((ReferenceType) resolvedFirstBound);
				setDelegate(brtd);
			}

		}
		return this.delegate;
	}

	@Override
	public UnresolvedType parameterize(Map<String, UnresolvedType> typeBindings) {
		UnresolvedType ut = typeBindings.get(getName());
		if (ut != null) {
			return world.resolve(ut);
		}
		return this;
	}

	public TypeVariable getTypeVariable() {
		// if (!fixedUp)
		// throw new BCException("ARGH"); // fix it up now?
		return typeVariable;
	}

	@Override
	public boolean isTypeVariableReference() {
		return true;
	}

	@Override
	public String toString() {
		return typeVariable.getName();
	}

	@Override
	public boolean isGenericWildcard() {
		return false;
	}

	@Override
	public boolean isAnnotation() {
		ReferenceType upper = (ReferenceType) typeVariable.getUpperBound();
		if (upper.isAnnotation()) {
			return true;
		}
		World world = upper.getWorld();
		typeVariable.resolve(world);
		ResolvedType annotationType = ResolvedType.ANNOTATION.resolve(world);
		UnresolvedType[] ifBounds = typeVariable.getSuperInterfaces();// AdditionalBounds();
		for (int i = 0; i < ifBounds.length; i++) {
			if (((ReferenceType) ifBounds[i]).isAnnotation()) {
				return true;
			}
			if (ifBounds[i].equals(annotationType)) {
				return true; // annotation itself does not have the annotation flag set in Java!
			}
		}
		return false;
	}

	/**
	 * return the signature for a *REFERENCE* to a type variable, which is simply: Tname; there is no bounds info included, that is
	 * in the signature of the type variable itself
	 */
	@Override
	public String getSignature() {
		StringBuffer sb = new StringBuffer();
		sb.append("T");
		sb.append(typeVariable.getName());
		sb.append(";");
		return sb.toString();
	}

	/**
	 * @return the name of the type variable
	 */
	public String getTypeVariableName() {
		return typeVariable.getName();
	}

	public ReferenceType getUpperBound() {
		return (ReferenceType) typeVariable.resolve(world).getUpperBound();
	}

	/**
	 * resolve the type variable we are managing and then return this object. 'this' is already a ResolvedType but the type variable
	 * may transition from a not-resolved to a resolved state.
	 */
	public ResolvedType resolve(World world) {
		typeVariable.resolve(world);
		return this;
	}

	/**
	 * @return true if the type variable this reference is managing is resolved
	 */
	public boolean isTypeVariableResolved() {
		return typeVariable.isResolved;
	}

}
