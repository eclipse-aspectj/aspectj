/* *******************************************************************
 * Copyright (c) 2004 IBM Corporation
 * 
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *    Andy Clement     initial implementation 
 * ******************************************************************/
package org.aspectj.runtime.internal.cflowstack;

import java.util.Stack;

public class ThreadStackFactoryImpl implements ThreadStackFactory {

	private static class ThreadStackImpl extends ThreadLocal implements ThreadStack {
		public Object initialValue() {
		  return new Stack();
		}
		public Stack getThreadStack() {
			return (Stack)get();
		}
	}

	public ThreadStack getNewThreadStack() {
		return new ThreadStackImpl();
	}

}
