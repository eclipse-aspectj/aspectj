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

import java.util.StringTokenizer;

import org.aspectj.lang.reflect.AjType;
import org.aspectj.lang.reflect.DeclarePrecedence;
import org.aspectj.lang.reflect.TypePattern;

/**
 * @author colyer
 *
 */
public class DeclarePrecedenceImpl implements DeclarePrecedence {

	private AjType<?> declaringType;
	private TypePattern[] precedenceList;
	private String precedenceString;
	
	public DeclarePrecedenceImpl(String precedenceList, AjType declaring) {
		this.declaringType = declaring;
		this.precedenceString = precedenceList;
		String toTokenize = precedenceList;
		if (toTokenize.startsWith("(")) {
			toTokenize = toTokenize.substring(1,toTokenize.length() - 1);
		}
		StringTokenizer strTok = new StringTokenizer(toTokenize,",");
		this.precedenceList = new TypePattern[strTok.countTokens()];
		for (int i = 0; i < this.precedenceList.length; i++) {
			this.precedenceList[i] = new TypePatternImpl(strTok.nextToken().trim());
		}
	}

	public AjType getDeclaringType() {
		return this.declaringType;
	}

	public TypePattern[] getPrecedenceOrder() {
		return this.precedenceList;
	}
	
	public String toString() {
		return "declare precedence : " + this.precedenceString;
	}

}
