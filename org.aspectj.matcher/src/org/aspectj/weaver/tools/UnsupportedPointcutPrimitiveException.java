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
package org.aspectj.weaver.tools;

import org.aspectj.weaver.WeaverMessages;

/**
 * @author colyer
 *
 */
public class UnsupportedPointcutPrimitiveException extends RuntimeException {

	private static final long serialVersionUID = 3258689888517043251L;

	private PointcutPrimitive unsupportedPrimitive; 
	private String pointcutExpression;
	
	public UnsupportedPointcutPrimitiveException(String pcExpression, PointcutPrimitive primitive) {
		super(WeaverMessages.format(WeaverMessages.UNSUPPORTED_POINTCUT_PRIMITIVE,pcExpression,primitive.getName()));
		this.pointcutExpression = pcExpression;
		this.unsupportedPrimitive = primitive;
	}

	/**
	 * @return Returns the unsupportedPrimitive.
	 */
	public PointcutPrimitive getUnsupportedPrimitive() {
		return unsupportedPrimitive;
	}
	
	public String getInvalidPointcutExpression() {
		return pointcutExpression;
	}
	
}
