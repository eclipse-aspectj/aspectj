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
package org.eclipse.jdt.internal.compiler.codegen;

import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

public class ExceptionLabel extends Label {
	public int start = POS_NOT_SET;
	public int end = POS_NOT_SET;
	public TypeBinding exceptionType;
public ExceptionLabel(CodeStream codeStream, TypeBinding exceptionType) {
	super(codeStream);
	this.exceptionType = exceptionType;
	this.start = codeStream.position;	
}
public boolean isStandardLabel(){
	return false;
}
public void place() {
	// register the handler inside the codeStream then normal place
	codeStream.registerExceptionHandler(this);
	super.place();

}
public void placeEnd() {
	end = codeStream.position;
}
}
