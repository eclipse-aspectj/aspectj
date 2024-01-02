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
 * 					   Copied from bits of original CFlowStack
 * ******************************************************************/
package org.aspectj.runtime.internal.cflowstack;

import java.util.List;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

public class ThreadCounterImpl11 implements ThreadCounter {
	private Hashtable<Thread, Counter> counters = new Hashtable<>();
	private Thread cached_thread;
	private Counter cached_counter;

	private int change_count = 0;
	private static final int COLLECT_AT = 20000;
	private static final int MIN_COLLECT_AT = 100;

	static class Counter {
		protected int value = 0;
	}

	private synchronized Counter getThreadCounter() {
		if (Thread.currentThread() != cached_thread) {
			cached_thread = Thread.currentThread();
			cached_counter = counters.get(cached_thread);
			if (cached_counter == null) {
				cached_counter = new Counter();
				counters.put(cached_thread, cached_counter);
			}
			change_count++;
			// Collect more often if there are many threads, but not *too* often
			int size = Math.max(1, counters.size()); // should be >1 b/c always live threads, but...
			if (change_count > Math.max(MIN_COLLECT_AT, COLLECT_AT/size)) {
				List<Thread> dead_stacks = new ArrayList<>();
				for (Enumeration<Thread> e = counters.keys(); e.hasMoreElements(); ) {
					Thread t = e.nextElement();
					if (!t.isAlive()) dead_stacks.add(t);
				}
				for (Thread t : dead_stacks) {
					counters.remove(t);
				}
				change_count = 0;
			}
		}
		return cached_counter;
	}

	public void inc() {
		getThreadCounter().value++;
	}

	public void dec() {
		getThreadCounter().value--;
	}

	public boolean isNotZero() {
		return getThreadCounter().value!=0;
	}

	public void removeThreadCounter() {
		// TODO
	}

}
