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
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.impl.*;

public class LongLiteralMinValue extends LongLiteral {

	final static char[] CharValue = new char[]{'-', '9','2','2','3','3','7','2','0','3','6','8','5','4','7','7','5','8','0','8','L'};
	final static Constant MIN_VALUE = Constant.fromValue(Long.MIN_VALUE) ; 

public LongLiteralMinValue(){
	super(CharValue,0,0,Long.MIN_VALUE);
	constant = MIN_VALUE;
}
public void computeConstant() {

	/*precomputed at creation time*/}
}
