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

import org.aspectj.lang.reflect.DeclareErrorOrWarning;

/**
 * @author colyer
 *
 */
public class DeclareErrorOrWarningImpl implements DeclareErrorOrWarning {

	private String pc;
	private String msg;
	private boolean isError;
	
	public DeclareErrorOrWarningImpl(String pointcut, String message, boolean isError) {
		this.pc = pointcut;
		this.msg = message;
		this.isError = isError;
	}
	
	/* (non-Javadoc)
	 * @see org.aspectj.lang.reflect.DeclareErrorOrWarning#getPointcutExpression()
	 */
	public String getPointcutExpression() {
		return pc;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.lang.reflect.DeclareErrorOrWarning#getMessage()
	 */
	public String getMessage() {
		return msg;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.lang.reflect.DeclareErrorOrWarning#isError()
	 */
	public boolean isError() {
		return isError;
	}

}
