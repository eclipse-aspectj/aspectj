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

/**
 * Represents a type variable in a type or generic method declaration
 */
public class TypeVariableReferenceType extends BoundedReferenceType implements TypeVariableReference {

	private TypeVariable typeVariable;
	private boolean resolvedIfBounds = false;
	
	public TypeVariableReferenceType(
			TypeVariable aTypeVariable,
			World aWorld) {
		super(aTypeVariable.getUpperBound().getSignature(),aWorld);
		this.typeVariable = aTypeVariable;
		this.isExtends    = false;
		this.isSuper      = false;
		setDelegate(new ReferenceTypeReferenceTypeDelegate((ReferenceType)aTypeVariable.getUpperBound()));
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
	
	public ReferenceType[] getAdditionalBounds() {
		if (!resolvedIfBounds) {
			setAdditionalInterfaceBoundsFromTypeVar();
			resolvedIfBounds = true;
		}
		return super.getAdditionalBounds();
	}
	
	public TypeVariable getTypeVariable() {
		return typeVariable;
	}
	
	public boolean isTypeVariableReference() {
		return true;
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
}
