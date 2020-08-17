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

	void enter(String methodName, Object thiz, Object[] args);

	void enter(String methodName, Object thiz);

	void exit(String methodName, Object ret);

	void exit(String methodName, Throwable th);

	void exit(String methodName);

	void event(String methodName);

	void event(String methodName, Object thiz, Object[] args);

	void debug(String message);

	void info(String message);

	void warn(String message);

	void warn(String message, Throwable th);

	void error(String message);

	void error(String message, Throwable th);

	void fatal(String message);

	void fatal(String message, Throwable th);

	void enter(String methodName, Object thiz, Object arg);

	void enter(String methodName, Object thiz, boolean z);

	void exit(String methodName, boolean b);

	void exit(String methodName, int i);

	void event(String methodName, Object thiz, Object arg);

	boolean isTraceEnabled();

	void setTraceEnabled(boolean b);
}
