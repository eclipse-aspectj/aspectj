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


package org.aspectj.weaver.patterns;


public final class BasicToken implements IToken {
	private String value;
	private boolean isIdentifier;
	private String literalKind;

	private int start;
	private int end;
	
	public static BasicToken makeOperator(String value, int start, int end) {
		return new BasicToken(value.intern(), false, null, start, end);
	}
	
	public static BasicToken makeIdentifier(String value, int start, int end) {
		return new BasicToken(value, true, null, start, end);
	}
	
	public static BasicToken makeLiteral(String value, String kind, int start, int end) {
		return new BasicToken(value, false, kind.intern(), start, end);
	}
	
	
	private BasicToken(String value, boolean isIdentifier, String literalKind, int start, int end) {
		this.value = value;
		this.isIdentifier = isIdentifier;
		this.literalKind = literalKind;
		this.start = start;
		this.end = end;
	}
	
	public int getStart() { return start; }
	public int getEnd() { return end; }
	public String getFileName() { return "unknown"; }
	
	public String getString() {
		return value;
	}

	public boolean isIdentifier() {
		return isIdentifier;
	}
	
	public Pointcut maybeGetParsedPointcut() {
		return null;
	}

	
	
	public String toString() {
		String s;
		if (isIdentifier) s = value;
		else s = "'" + value + "'";
		
		return s + "@" + start + ":" + end;
	}
	public String getLiteralKind() {
		return literalKind;
	}

}
