/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.ajdt.internal.compiler.lookup;

import org.aspectj.org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference;
import org.aspectj.org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

public class AjTypeConstants {

	public static final char[] ORG = "org".toCharArray();
	public static final char[] ASPECTJ = "aspectj".toCharArray();
	public static final char[] RUNTIME = "runtime".toCharArray();
	public static final char[] LANG = "lang".toCharArray();
	public static final char[] INTERNAL = "internal".toCharArray();
	
	// Constant compound names
	public static final char[][] ORG_ASPECTJ_LANG_JOINPOINT =
	    new char[][] {ORG, ASPECTJ, LANG, "JoinPoint".toCharArray()};
	    
	public static final char[][] ORG_ASPECTJ_LANG_JOINPOINT_STATICPART =
	    new char[][] {ORG, ASPECTJ, LANG, "JoinPoint".toCharArray(), "StaticPart".toCharArray()};
	    
	public static final char[][] ORG_ASPECTJ_RUNTIME_INTERNAL_AROUNDCLOSURE =
	    new char[][] {ORG, ASPECTJ, RUNTIME, INTERNAL, "AroundClosure".toCharArray()};
	    
	public static final char[][] ORG_ASPECTJ_RUNTIME_INTERNAL_CONVERSIONS =
	    new char[][] {ORG, ASPECTJ, RUNTIME, INTERNAL, "Conversions".toCharArray()};
	    
	public static TypeReference getJoinPointType() {
		return new QualifiedTypeReference(ORG_ASPECTJ_LANG_JOINPOINT, new long[10]);
	}
	
	public static TypeReference getJoinPointStaticPartType() {
		return new QualifiedTypeReference(ORG_ASPECTJ_LANG_JOINPOINT_STATICPART, new long[10]);
	}

	public static TypeReference getAroundClosureType() {
		return new QualifiedTypeReference(ORG_ASPECTJ_RUNTIME_INTERNAL_AROUNDCLOSURE, new long[10]);
	}

	public static ReferenceBinding getConversionsType(Scope scope) {
		return (ReferenceBinding)scope.getType(
				ORG_ASPECTJ_RUNTIME_INTERNAL_CONVERSIONS,
				ORG_ASPECTJ_RUNTIME_INTERNAL_CONVERSIONS.length);
	}

	public static MethodBinding getConversionMethodToObject(Scope scope, TypeBinding fromType) {
		String name = new String(fromType.sourceName()) + "Object";
		return getConversionsType(scope).getMethods(name.toCharArray())[0];
	}
	
	public static MethodBinding getConversionMethodFromObject(Scope scope, TypeBinding toType) {
		String name = new String(toType.sourceName()) + "Value";
		return getConversionsType(scope).getMethods(name.toCharArray())[0];
	}

}
