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

public class NullConstant extends Constant {
	public static final NullConstant Default = new NullConstant();

	final static String NullString = new StringBuffer(4).append((String)null).toString();
private NullConstant() {
}
public String stringValue() {
	
	return NullString;
}
public String toString(){

	return "(null)" + null ; } //$NON-NLS-1$
public int typeID() {
	return T_null;
}
}
