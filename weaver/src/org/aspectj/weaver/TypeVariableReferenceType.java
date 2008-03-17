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

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;

/**
 * Represents a type variable in a type or generic method declaration
 */
public class TypeVariableReferenceType extends BoundedReferenceType implements TypeVariableReference {

	private TypeVariable typeVariable;
	private boolean resolvedIfBounds = false;
	
	// If 'fixedUp' then the type variable in here is a reference to the real one that may
	// exist either on a member or a type.  Not fixedUp means that we unpacked a generic
	// signature and weren't able to fix it up during resolution (didn't quite know enough
	// at the right time).  Wonder if we can fix it up late?
	boolean fixedUp = false;
	
	public TypeVariableReferenceType(
			TypeVariable aTypeVariable,
			World aWorld) {
		super(
				aTypeVariable.getGenericSignature(),
				aTypeVariable.getErasureSignature(),
			  aWorld);
		this.typeVariable = aTypeVariable;
		this.isExtends    = false;
		this.isSuper      = false;
	}
	
	public ReferenceTypeDelegate getDelegate() {
		if (delegate==null) 
		  setDelegate(new ReferenceTypeReferenceTypeDelegate((ReferenceType)typeVariable.getFirstBound()));
		return delegate;
	}

	public UnresolvedType getUpperBound() {
		if (typeVariable==null) return super.getUpperBound();
		return typeVariable.getUpperBound();
	}
	
	public UnresolvedType getLowerBound() {
		return typeVariable.getLowerBound();
	}
	
	private void setAdditionalInterfaceBoundsFromTypeVar() {
		if (typeVariable.getAdditionalInterfaceBounds() == null) {
			return;
		} else {
			UnresolvedType [] ifBounds = typeVariable.getAdditionalInterfaceBounds();
			additionalInterfaceBounds = new ReferenceType[ifBounds.length];
			for (int i = 0; i < ifBounds.length; i++) {
				additionalInterfaceBounds[i] = (ReferenceType) ifBounds[i].resolve(getWorld()); 
			}
		}
	}

	public UnresolvedType parameterize(Map typeBindings) {
		UnresolvedType ut = (UnresolvedType) typeBindings.get(getName());
		if (ut!=null) return world.resolve(ut);
		return this;
	}
	
	public ReferenceType[] getAdditionalBounds() {
		if (!resolvedIfBounds) {
			setAdditionalInterfaceBoundsFromTypeVar();
			resolvedIfBounds = true;
		}
		return super.getAdditionalBounds();
	}
	
	public TypeVariable getTypeVariable() {
		// if (!fixedUp) throw new BCException("ARGH"); // SAUSAGES - fix it up now?
		return typeVariable;
	}
	
	public boolean isTypeVariableReference() {
		return true;
	}
	
	public String toString() {
		return typeVariable.getName();
	}
	
	public boolean isGenericWildcard() {
		return false;
	}
	
	//public ResolvedType resolve(World world) {
	//	return super.resolve(world);
	//}
	
	public boolean isAnnotation() {
		World world = ((ReferenceType)getUpperBound()).getWorld();
		ResolvedType annotationType = ResolvedType.ANNOTATION.resolve(world);
		if (getUpperBound() != null && ((ReferenceType)getUpperBound()).isAnnotation()) return true;
		ReferenceType[] ifBounds = getAdditionalBounds();
		for (int i = 0; i < ifBounds.length; i++) {
			if (ifBounds[i].isAnnotation()) return true;
			if (ifBounds[i] == annotationType) return true; // annotation itself does not have the annotation flag set in Java!
		}
		return false;
	}
	
	/**
     * return the signature for a *REFERENCE* to a type variable, which is simply:
     *   Tname;
     * there is no bounds info included, that is in the signature of the type variable itself
     */
	public String getSignature() {
	  StringBuffer sb = new StringBuffer();
	  sb.append("T");
	  sb.append(typeVariable.getName());
	  sb.append(";");
	  return sb.toString();
	}
	
	public void write(DataOutputStream s) throws IOException {
		super.write(s);
//		TypeVariableDeclaringElement tvde = typeVariable.getDeclaringElement();
//		if (tvde == null) {
//			s.writeInt(TypeVariable.UNKNOWN);
//		} else {			
//			s.writeInt(typeVariable.getDeclaringElementKind());
//			if (typeVariable.getDeclaringElementKind() == TypeVariable.TYPE) {
//				((UnresolvedType)tvde).write(s);
//			} else if (typeVariable.getDeclaringElementKind() == TypeVariable.METHOD){
//				// it's a method
//				((ResolvedMember)tvde).write(s);
//			}
//		}
	}
}
