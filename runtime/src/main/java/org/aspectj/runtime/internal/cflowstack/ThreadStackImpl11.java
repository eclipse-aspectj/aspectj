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
 * 					   Copied from bits of original CFlowStack
 * ******************************************************************/
package org.aspectj.runtime.internal.cflowstack;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Stack;

public class ThreadStackImpl11 implements ThreadStack {
	private Hashtable stacks = new Hashtable();
	private Thread cached_thread;
	private Stack cached_stack;
	private int change_count = 0;
	private static final int COLLECT_AT = 20000;
	private static final int MIN_COLLECT_AT = 100; 

	public synchronized Stack getThreadStack() {
		if (Thread.currentThread() != cached_thread) {
			cached_thread = Thread.currentThread();
			cached_stack = (Stack)stacks.get(cached_thread);
			if (cached_stack == null) {
				cached_stack = new Stack();
				stacks.put(cached_thread, cached_stack);
			}
			change_count++;
			// Collect more often if there are many threads, but not *too* often
			int size = Math.max(1, stacks.size()); // should be >1 b/c always live threads, but...
			if (change_count > Math.max(MIN_COLLECT_AT, COLLECT_AT/size)) {
				Stack dead_stacks = new Stack();
				for (Enumeration e = stacks.keys(); e.hasMoreElements(); ) {
					Thread t = (Thread)e.nextElement();
					if (!t.isAlive()) dead_stacks.push(t);
				}
				for (Enumeration e = dead_stacks.elements(); e.hasMoreElements(); ) {
					Thread t = (Thread)e.nextElement();
					stacks.remove(t);
				}
				change_count = 0;
			}
		}
		return cached_stack;
	}

	public void removeThreadStack() {
		// TODO
	}

}
