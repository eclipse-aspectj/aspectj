/**********************************************************************
Copyright (c) 2002 IBM Corp. and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html
 
Contributors:
     IBM Corporation - initial API and implementation
**********************************************************************/

package org.eclipse.jdt.core.compiler;

/**
 * Exception thrown by a scanner when encountering lexical errors.
 */
public class InvalidInputException extends Exception {

	/**
	 * InvalidInputException constructor comment.
	 */
	public InvalidInputException() {
		super();
	}
	/**
	 * InvalidInputException constructor comment.
	 * @param s java.lang.String
	 */
	public InvalidInputException(String s) {
		super(s);
	}
}
