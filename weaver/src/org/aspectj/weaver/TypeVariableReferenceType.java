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
	
	public ReferenceType[] getAdditionalBounds() {
		if (additionalInterfaceBounds ==null && typeVariable.getAdditionalInterfaceBounds()!=null) {
			UnresolvedType [] ifBounds = typeVariable.getAdditionalInterfaceBounds();
			additionalInterfaceBounds = new ReferenceType[ifBounds.length];
			for (int i = 0; i < ifBounds.length; i++) {
				additionalInterfaceBounds[i] = (ReferenceType) ifBounds[i]; 
			}
		}
		return additionalInterfaceBounds;
	}
	
	public TypeVariable getTypeVariable() {
		return typeVariable;
	}
	
	public boolean isTypeVariableReference() {
		return true;
	}
	
//	public ResolvedType resolve(World world) {
	//	return super.resolve(world);
	//}
	
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
