/* *******************************************************************
 * Copyright (c) 2004 IBM Corporation
 * 
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
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
		public void removeThreadStack() {
			this.remove();
		}
	}

	public ThreadStack getNewThreadStack() {
		return new ThreadStackImpl();
	}
	
	private static class ThreadCounterImpl extends ThreadLocal implements ThreadCounter {
		
		public Object initialValue() {
		  return new Counter();
		}
		public Counter getThreadCounter() {
			return (Counter)get();
		}
		
		public void removeThreadCounter() {
			this.remove();
		}
		
		public void inc() { getThreadCounter().value++; }
		public void dec() { getThreadCounter().value--; }
		public boolean isNotZero() { return getThreadCounter().value!= 0; }
		
		static class Counter {
		  protected int value = 0;
		}
	}
	
	public ThreadCounter getNewThreadCounter() {
		return new ThreadCounterImpl();
	}

}
