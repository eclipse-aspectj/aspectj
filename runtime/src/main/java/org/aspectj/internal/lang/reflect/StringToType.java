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

import org.aspectj.lang.reflect.AjTypeSystem;

/**
 * @author colyer
 * Helper class for converting type representations in Strings into java.lang.reflect.Types.
 */
public class StringToType {

	public static Type[] commaSeparatedListToTypeArray(String typeNames, Class classScope) 
	throws ClassNotFoundException {
		StringTokenizer strTok = new StringTokenizer(typeNames,",");
		Type[] ret = new Type[strTok.countTokens()];
		int index = 0;
		//outer: 
			while (strTok.hasMoreTokens()) {
			String typeName = strTok.nextToken().trim();
			ret[index++] = stringToType(typeName, classScope);
		}
		return ret;
	}
	
	public static Type stringToType(String typeName, Class classScope) 
	throws ClassNotFoundException {
		try {
			if (!typeName.contains("<")) {
				return AjTypeSystem.getAjType(Class.forName(typeName,false,classScope.getClassLoader()));
			} else {
				return makeParameterizedType(typeName,classScope);
			}
		} catch (ClassNotFoundException e) {
			// could be a type variable
			TypeVariable[] tVars = classScope.getTypeParameters();
			for (TypeVariable tVar : tVars) {
				if (tVar.getName().equals(typeName)) {
					return tVar;
				}
			}
			throw new ClassNotFoundException(typeName);
		}
	}
	
	private static Type makeParameterizedType(String typeName, Class classScope) 
	throws ClassNotFoundException {
		int paramStart = typeName.indexOf('<');
		String baseName = typeName.substring(0, paramStart);
		final Class baseClass = Class.forName(baseName,false,classScope.getClassLoader());
		int paramEnd = typeName.lastIndexOf('>');
		String params = typeName.substring(paramStart+1,paramEnd);
		final Type[] typeParams = commaSeparatedListToTypeArray(params,classScope);
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
}
