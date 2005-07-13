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
public class TypeVariableReferenceType extends BoundedReferenceType {

	private TypeVariable typeVariable;
	
	public TypeVariableReferenceType(
			TypeVariable aTypeVariable,
			World aWorld) {
		super(aTypeVariable.getUpperBound().getSignature(),aWorld);
		this.typeVariable = aTypeVariable;
		this.isExtends = false;
		this.isSuper = false;
		this.upperBound = (ReferenceType) aTypeVariable.getUpperBound();
		this.lowerBound = (ReferenceType) aTypeVariable.getLowerBound();
		TypeX[] ifBounds = aTypeVariable.getAdditionalInterfaceBounds();
		if (ifBounds.length > 0) {
			this.additionalInterfaceBounds = new ReferenceType[ifBounds.length];
			for (int i = 0; i < ifBounds.length; i++) {
				this.additionalInterfaceBounds[i] = (ReferenceType) ifBounds[i]; 
			}
		}
		setDelegate(new ReferenceTypeReferenceTypeDelegate((ReferenceType)aTypeVariable.getUpperBound()));
	}
	
	public TypeVariable getTypeVariable() {
		return typeVariable;
	}
	
	public boolean isTypeVariable() {
		return true;
	}
	
}
