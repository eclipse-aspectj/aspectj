/* *******************************************************************
 * Copyright (c) 2004 IBM Corporation.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * ******************************************************************/

package org.aspectj.weaver.tools;

import org.aspectj.util.TypeSafeEnum;

/**
 * An enumeration of the different kinds of pointcut primitives
 * supported by AspectJ.
 */
public class PointcutPrimitives extends TypeSafeEnum {

	public static final PointcutPrimitives CALL = new PointcutPrimitives("call",1);
	public static final PointcutPrimitives EXECUTION = new PointcutPrimitives("execution",2);
	public static final PointcutPrimitives GET = new PointcutPrimitives("get",3);
	public static final PointcutPrimitives SET = new PointcutPrimitives("set",4);
	public static final PointcutPrimitives INITIALIZATION = new PointcutPrimitives("initialization",5);
	public static final PointcutPrimitives PRE_INITIALIZATION = new PointcutPrimitives("preinitialization",6);
	public static final PointcutPrimitives STATIC_INITIALIZATION = new PointcutPrimitives("staticinitialization",7);
	public static final PointcutPrimitives HANDLER = new PointcutPrimitives("handler",8);
	public static final PointcutPrimitives ADVICE_EXECUTION = new PointcutPrimitives("adviceexecution",9);
	public static final PointcutPrimitives WITHIN = new PointcutPrimitives("within",10);
	public static final PointcutPrimitives WITHIN_CODE = new PointcutPrimitives("withincode",11);
	public static final PointcutPrimitives CFLOW = new PointcutPrimitives("cflow",12);
	public static final PointcutPrimitives CFLOW_BELOW = new PointcutPrimitives("cflowbelow",13);
	public static final PointcutPrimitives IF = new PointcutPrimitives("if",14);
	public static final PointcutPrimitives THIS = new PointcutPrimitives("this",15);
	public static final PointcutPrimitives TARGET = new PointcutPrimitives("target",16);
	public static final PointcutPrimitives ARGS = new PointcutPrimitives("args",17);

	private PointcutPrimitives(String name, int key) {
		super(name, key);
	}

}
