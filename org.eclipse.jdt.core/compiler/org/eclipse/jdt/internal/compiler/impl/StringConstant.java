/*******************************************************************************
 * Copyright (c) 2000, 2001, 2002 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.internal.compiler.impl;

public class StringConstant extends Constant {
	public String value;
    
public StringConstant(String value) {
	this.value = value ;
}
public boolean compileTimeEqual(StringConstant right){
	//String are intermed in the compiler==>thus if two string constant
	//get to be compared, it is an equal on the vale which is done
	if (this.value == null) {
		return right.value == null;
	}
	return this.value.equals(right.value);
}
public String stringValue() {
	//spec 15.17.11

	//the next line do not go into the toString() send....!
	return value ;

	/*
	String s = value.toString() ;
	if (s == null)
		return "null";
	else
		return s;
	*/
	
}
public String toString(){

	return "(String)\"" + value +"\""; } //$NON-NLS-2$ //$NON-NLS-1$
public int typeID() {
	return T_String;
}
}
