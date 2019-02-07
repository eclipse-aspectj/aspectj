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
package org.aspectj.internal.lang.reflect;

import java.lang.reflect.Type;

import org.aspectj.lang.reflect.AjType;
import org.aspectj.lang.reflect.DeclareParents;
import org.aspectj.lang.reflect.TypePattern;

/**
 * @author colyer
 *
 */
public class DeclareParentsImpl implements DeclareParents {

	private AjType<?> declaringType;
	private TypePattern targetTypesPattern;
	private Type[] parents;
	private String parentsString;
	private String firstMissingTypeName;
	private boolean isExtends;
	private boolean parentsError = false;
	
	
	// Parents arg is a comma-separate list of type names that needs to be turned into 
	// AjTypes 
	public DeclareParentsImpl(String targets, String parentsAsString, boolean isExtends, AjType<?> declaring) 
	{
		this.targetTypesPattern = new TypePatternImpl(targets);
		this.isExtends = isExtends;
		this.declaringType = declaring;
		this.parentsString = parentsAsString;
		try {
			this.parents = StringToType.commaSeparatedListToTypeArray(parentsAsString, declaring.getJavaClass());
		} catch (ClassNotFoundException cnfEx) {
			this.parentsError = true;
			this.firstMissingTypeName = cnfEx.getMessage();
		}
	}

	public AjType getDeclaringType() {
		return this.declaringType;
	}

	public TypePattern getTargetTypesPattern() {
		return this.targetTypesPattern;
	}

	public boolean isExtends() {
		return this.isExtends;
	}

	public boolean isImplements() {
		return !this.isExtends;
	}

	public Type[] getParentTypes() throws ClassNotFoundException {
		if (parentsError) {
			throw new ClassNotFoundException(this.firstMissingTypeName);
		}
		return this.parents;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("declare parents : ");
		sb.append(getTargetTypesPattern().asString());
		sb.append(isExtends() ? " extends " : " implements ");
		sb.append(this.parentsString);
		return sb.toString();
	}
}
