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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.StringTokenizer;

import org.aspectj.lang.reflect.AjType;
import org.aspectj.lang.reflect.AjTypeSystem;
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
	
	/**
	 * Parents arg is a comma-separate list of type names that needs to be turned into 
	 * AjTypes 
	 */
	public DeclareParentsImpl(String targets, String parentsAsString, boolean isExtends, AjType<?> declaring) 
	{
		this.targetTypesPattern = new TypePatternImpl(targets);
		this.isExtends = isExtends;
		this.declaringType = declaring;
		this.parentsString = parentsAsString;
		this.parents = commaSeparatedListToTypeArray(parentsAsString);
	}

	/* (non-Javadoc)
	 * @see org.aspectj.lang.reflect.DeclareParents#getDeclaringType()
	 */
	public AjType getDeclaringType() {
		return this.declaringType;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.lang.reflect.DeclareParents#getTargetTypesPattern()
	 */
	public TypePattern getTargetTypesPattern() {
		return this.targetTypesPattern;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.lang.reflect.DeclareParents#isExtends()
	 */
	public boolean isExtends() {
		return this.isExtends;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.lang.reflect.DeclareParents#isImplements()
	 */
	public boolean isImplements() {
		return !this.isExtends;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.lang.reflect.DeclareParents#getParentTypes()
	 */
	public Type[] getParentTypes() throws ClassNotFoundException {
		if (parentsError) {
			throw new ClassNotFoundException(this.firstMissingTypeName);
		}
		return this.parents;
	}

	private Type[] commaSeparatedListToTypeArray(String typeNames) {
		StringTokenizer strTok = new StringTokenizer(typeNames,",");
		Type[] ret = new Type[strTok.countTokens()];
		int index = 0;
		outer: while (strTok.hasMoreTokens()) {
			String parentTypeName = strTok.nextToken().trim();
			try {
				if (parentTypeName.indexOf("<") == -1) {
					ret[index] = AjTypeSystem.getAjType(Class.forName(parentTypeName));
				} else {
					ret[index] = makeParameterizedType(parentTypeName);
				}
			} catch (ClassNotFoundException e) {
				// could be a type variable
				TypeVariable[] tVars = this.declaringType.getJavaClass().getTypeParameters();
				for (int i = 0; i < tVars.length; i++) {
					if (tVars[i].getName().equals(parentTypeName)) {
						ret[index] = tVars[i];
						continue outer;
					}
				}
				ret[index] = null;
				if (this.firstMissingTypeName == null) this.firstMissingTypeName = parentTypeName;
				this.parentsError = true;
			}
			index++;
		}
		return ret;
	}
	
	private Type makeParameterizedType(String typeName) 
	throws ClassNotFoundException {
		int paramStart = typeName.indexOf('<');
		String baseName = typeName.substring(0, paramStart);
		final Class baseClass = Class.forName(baseName);
		int paramEnd = typeName.lastIndexOf('>');
		String params = typeName.substring(paramStart+1,paramEnd);
		final Type[] typeParams = commaSeparatedListToTypeArray(params);
		return new ParameterizedType() {

			public Type[] getActualTypeArguments() {
				return typeParams;
			}

			public Type getRawType() {
				return baseClass;
			}

			public Type getOwnerType() {
				return baseClass.getEnclosingClass();
			}			
		};
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
