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

import org.aspectj.lang.reflect.PerClause;
import org.aspectj.lang.reflect.PerClauseKind;

/**
 * @author colyer
 *
 */
public class PerClauseImpl implements PerClause {

	private final PerClauseKind kind;
	
	protected PerClauseImpl(PerClauseKind kind) {
		this.kind = kind;
	}
	
	public PerClauseKind getKind() {
		return kind;
	}

	public String toString() {
		return "issingleton()";
	}
}
