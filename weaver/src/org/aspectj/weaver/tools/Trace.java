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

public interface Trace {

	public void enter (String methodName, Object thiz, Object[] args);

	public void enter (String methodName, Object thiz);

	public void exit (String methodName, Object ret);

	public void exit (String methodName, Throwable th);

	public void exit (String methodName);

	public void event (String methodName);

	public void event (String methodName, Object thiz, Object[] args);
	
	public void debug (String message);
	
	public void info (String message);

	public void warn (String message);

	public void warn (String message, Throwable th);

	public void error (String message);

	public void error (String message, Throwable th);

	public void fatal (String message);

	public void fatal (String message, Throwable th);
	
	
	/*
	 * Convenience methods
	 */
	public void enter (String methodName, Object thiz, Object arg);

	public void enter (String methodName, Object thiz, boolean z);

	public void exit (String methodName, boolean b);

	public void exit (String methodName, int i);

	public void event (String methodName, Object thiz, Object arg);
	
	public boolean isTraceEnabled ();

	public void setTraceEnabled (boolean b);
}