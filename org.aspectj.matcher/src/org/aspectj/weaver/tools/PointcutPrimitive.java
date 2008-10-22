/* *******************************************************************
 * Copyright (c) 2004 IBM Corporation.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * ******************************************************************/

package org.aspectj.weaver.tools;

import org.aspectj.util.TypeSafeEnum;

/**
 * An enumeration of the different kinds of pointcut primitives
 * supported by AspectJ.
 */
public final class PointcutPrimitive extends TypeSafeEnum {

	public static final PointcutPrimitive CALL = new PointcutPrimitive("call",1);
	public static final PointcutPrimitive EXECUTION = new PointcutPrimitive("execution",2);
	public static final PointcutPrimitive GET = new PointcutPrimitive("get",3);
	public static final PointcutPrimitive SET = new PointcutPrimitive("set",4);
	public static final PointcutPrimitive INITIALIZATION = new PointcutPrimitive("initialization",5);
	public static final PointcutPrimitive PRE_INITIALIZATION = new PointcutPrimitive("preinitialization",6);
	public static final PointcutPrimitive STATIC_INITIALIZATION = new PointcutPrimitive("staticinitialization",7);
	public static final PointcutPrimitive HANDLER = new PointcutPrimitive("handler",8);
	public static final PointcutPrimitive ADVICE_EXECUTION = new PointcutPrimitive("adviceexecution",9);
	public static final PointcutPrimitive WITHIN = new PointcutPrimitive("within",10);
	public static final PointcutPrimitive WITHIN_CODE = new PointcutPrimitive("withincode",11);
	public static final PointcutPrimitive CFLOW = new PointcutPrimitive("cflow",12);
	public static final PointcutPrimitive CFLOW_BELOW = new PointcutPrimitive("cflowbelow",13);
	public static final PointcutPrimitive IF = new PointcutPrimitive("if",14);
	public static final PointcutPrimitive THIS = new PointcutPrimitive("this",15);
	public static final PointcutPrimitive TARGET = new PointcutPrimitive("target",16);
	public static final PointcutPrimitive ARGS = new PointcutPrimitive("args",17);
	public static final PointcutPrimitive REFERENCE = new PointcutPrimitive("reference pointcut",18);
	public static final PointcutPrimitive AT_ANNOTATION = new PointcutPrimitive("@annotation",19);
	public static final PointcutPrimitive AT_THIS = new PointcutPrimitive("@this",20);
	public static final PointcutPrimitive AT_TARGET = new PointcutPrimitive("@target",21);
	public static final PointcutPrimitive AT_ARGS = new PointcutPrimitive("@args",22);
	public static final PointcutPrimitive AT_WITHIN = new PointcutPrimitive("@within",23);
	public static final PointcutPrimitive AT_WITHINCODE = new PointcutPrimitive("@withincode",24);

	private PointcutPrimitive(String name, int key) {
		super(name, key);
	}

}
