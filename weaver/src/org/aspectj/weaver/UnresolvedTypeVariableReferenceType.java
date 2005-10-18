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
		super(aTypeVariable.getFirstBound().getSignature());
		this.typeVariable = aTypeVariable;
	}
	
	// only used when resolving circular refs...
	public void setTypeVariable(TypeVariable aTypeVariable) {
		this.signature = "T" + aTypeVariable.getName() + ";"; //aTypeVariable.getUpperBound().getSignature();
		this.typeVariable = aTypeVariable;
	}
	
	public ResolvedType resolve(World world) {
		TypeVariableDeclaringElement typeVariableScope = world.getTypeVariableLookupScope();
		TypeVariable resolvedTypeVariable = null;
		TypeVariableReferenceType tvrt    = null;
		if (typeVariableScope == null) {
			// throw new BCException("There is no scope in which to lookup type variables!");
			// FIXME asc correct thing to do is go bang, but to limp along, lets cope with the scope missing
			resolvedTypeVariable = typeVariable.resolve(world);
			tvrt = new TypeVariableReferenceType(resolvedTypeVariable,world);
		} else {
		    boolean foundOK = false;
			resolvedTypeVariable = typeVariableScope.getTypeVariableNamed(typeVariable.getName());
			// FIXME asc remove this when the shared type var stuff is sorted
			if (resolvedTypeVariable == null) {
				resolvedTypeVariable = typeVariable.resolve(world);
			} else {
			  foundOK = true;
			}
			tvrt = new TypeVariableReferenceType(resolvedTypeVariable,world);
			tvrt.fixedUp = foundOK;
		}
		
		return tvrt;
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
	
	public String toDebugString() {
		return typeVariable.getName();
	}
	
	public void write(DataOutputStream s) throws IOException {
		super.write(s);
	}

	public String getErasureSignature() {
		return typeVariable.getFirstBound().getSignature();
	}
	
}
