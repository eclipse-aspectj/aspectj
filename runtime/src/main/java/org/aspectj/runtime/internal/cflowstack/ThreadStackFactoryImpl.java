/* *******************************************************************
 * Copyright (c) 2004 IBM Corporation
 *
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors:
 *    Andy Clement     initial implementation
 * ******************************************************************/
package org.aspectj.runtime.internal.cflowstack;

import java.util.Stack;

public class ThreadStackFactoryImpl implements ThreadStackFactory {

	private static class ThreadStackImpl extends ThreadLocal<Stack> implements ThreadStack {
		public Stack initialValue() {
		  return new Stack();
		}
		public Stack getThreadStack() {
			return get();
		}
		public void removeThreadStack() {
			this.remove();
		}
	}

	public ThreadStack getNewThreadStack() {
		return new ThreadStackImpl();
	}

	private static class ThreadCounterImpl extends ThreadLocal<ThreadCounterImpl.Counter> implements ThreadCounter {

		public Counter initialValue() {
		  return new Counter();
		}
		public Counter getThreadCounter() {
			return get();
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
