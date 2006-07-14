/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Matthew Webster - initial implementation
 *******************************************************************************/
package org.aspectj.weaver.tools;

public class DefaultTrace extends AbstractTrace {
	
	public DefaultTrace (Class clazz) {
		super(clazz);
	}
	
	public void enter (String methodName, Object thiz, Object[] args) {
		if (tracingEnabled) {
//			println("> " + tracedClass.getName() + "." + methodName + " " + formatObj(thiz) + " " + formatArgs(args));
			println("> " + formatMessage(tracedClass.getName(),methodName,thiz,args));
		}
	}
	
	public void enter (String methodName, Object thiz) {
		if (tracingEnabled) {
//			println("> " + tracedClass.getName() + "." + methodName + " " + formatObj(thiz));
			println("> " + formatMessage(tracedClass.getName(),methodName,thiz,null));
		}
	}

	public void exit (String methodName, Object ret) {
		if (tracingEnabled) {
//			println("< " + tracedClass.getName() + "." + methodName + " " + formatObj(ret));
			println("< " + formatMessage(tracedClass.getName(),methodName,ret,null));
		}
	}

	public void exit (String methodName) {
		if (tracingEnabled) {
//			println("< " + tracedClass.getName() + "." + methodName);
			println("< " + formatMessage(tracedClass.getName(),methodName,null,null));
		}
	}

	public void exit(String methodName, Throwable th) {
		exit(methodName,th);
	}

	/**
	 * Template method that allows choice of destination for output
	 * 
	 * @param s message to be traced
	 */
	protected void println (String s) {
		System.err.println(s);
	}

	private static boolean tracingEnabled = getBoolean("org.aspectj.weaver.tools.tracing",false);

	private static boolean getBoolean (String name, boolean def) {
		String defaultValue = String.valueOf(def);
		String value = System.getProperty(name,defaultValue);
		return Boolean.valueOf(value).booleanValue();
	}

}
