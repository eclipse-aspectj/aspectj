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
 * @author colyer
 * Represents a type variable encountered in the Eclipse Source world,
 * which when resolved will turn into a TypeVariableReferenceType
 */
public class UnresolvedTypeVariableReferenceType extends UnresolvedType implements TypeVariableReference {

	private TypeVariable typeVariable;
	
	// constructor used as place-holder when dealing with circular refs such as Enum
	public UnresolvedTypeVariableReferenceType() {
		super("Ljava/lang/Object;");
	}
	
	public UnresolvedTypeVariableReferenceType(TypeVariable aTypeVariable) {
		super(aTypeVariable.getUpperBound().getSignature());
		this.typeVariable = aTypeVariable;
	}
	
	// only used when resolving circular refs...
	public void setTypeVariable(TypeVariable aTypeVariable) {
		this.signature = aTypeVariable.getUpperBound().getSignature();
		this.typeVariable = aTypeVariable;
	}
	
	public ResolvedType resolve(World world) {
		if (typeVariable == null) {
		    throw new BCException("Cannot resolve this type variable reference, the type variable has not been set!");
		}
		typeVariable.resolve(world);
		return new TypeVariableReferenceType(typeVariable,world);
	}
	
	public boolean isTypeVariableReference() {
		return true;
	}
	
	public TypeVariable getTypeVariable() {
		return typeVariable;
	}

//	public String getName() {
//		if (typeVariable == null) return "<type variable not set!>";
//		return typeVariable.getDisplayName();
//	}
	
	public String toString() {
		if (typeVariable == null) {
			return "<type variable not set!>";
		} else {
			return "T" + typeVariable.getName() + ";";
		}
	}

}
