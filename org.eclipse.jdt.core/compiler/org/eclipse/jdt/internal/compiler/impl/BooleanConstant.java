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

public class BooleanConstant extends Constant {
	boolean value;

	
public BooleanConstant(boolean value) {
	this.value = value;
}
public boolean booleanValue() {
	return (boolean) value;
}
public String stringValue() {
	//spec 15.17.11
	
	String s = new Boolean(value).toString() ;
	if (s == null)
		return "null"; //$NON-NLS-1$
	else
		return s;
}
public String toString(){

	return "(boolean)" + value ; } //$NON-NLS-1$
public int typeID() {
	return T_boolean;
}
}
