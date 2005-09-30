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

import org.aspectj.lang.reflect.SignaturePattern;

/**
 * Basic implementation of signature pattern
 *
 */
public class SignaturePatternImpl implements SignaturePattern {

	private String sigPattern;
	
	public SignaturePatternImpl(String pattern) {
		this.sigPattern = pattern;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.lang.reflect.SignaturePattern#asString()
	 */
	public String asString() {
		return sigPattern;
	}
	
	public String toString() { return asString(); }

}
