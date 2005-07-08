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

	private String name;
	
	public TypeVariableReferenceType(
			String aTypeVariableName,
			ReferenceType aBound,
			boolean isExtends,
			World aWorld
			) {
		super(aBound,isExtends,aWorld);
		this.name = aTypeVariableName;
	}
	
	public TypeVariableReferenceType(
			TypeVariable aTypeVariable,
			World aWorld) {
		super((ReferenceType)aTypeVariable.getUpperBound(),true,aWorld);
		this.name = aTypeVariable.getName();
		if (aTypeVariable.getLowerBound() != null) {
			this.isExtends = false;
			this.isSuper = true;
			this.lowerBound = (ReferenceType) aTypeVariable.getLowerBound();
		}
		if (aTypeVariable.getAdditionalInterfaceBounds().length > 0) {
			this.additionalInterfaceBounds = (ReferenceType[]) aTypeVariable.getAdditionalInterfaceBounds();
		}
	}
	
	public String getTypeVariableName() {
		return name;
	}
}
