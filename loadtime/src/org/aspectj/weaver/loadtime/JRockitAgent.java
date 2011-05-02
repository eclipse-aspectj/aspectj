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
package org.aspectj.weaver.loadtime;

import java.util.Stack;

import com.bea.jvm.ClassLibrary;
import com.bea.jvm.JVMFactory;

/**
 * BEA JRocket JMAPI agent.
 * 
 * Use "-Xmanagement:class=org.aspectj.weaver.loadtime.JRockitAgent"
 */
public class JRockitAgent implements com.bea.jvm.ClassPreProcessor {

	private ClassPreProcessor preProcessor;

	/*
	 * This is used to implement the recursion protection offered by JVMTI but not by JRockit JMAPI. I we are called to preProcess a
	 * class while already preProcessing another we will return immediately
	 */
	private static ThreadLocalStack stack = new ThreadLocalStack();

	public JRockitAgent() {
		this.preProcessor = new Aj();

		ClassLibrary cl = JVMFactory.getJVM().getClassLibrary();
		cl.setClassPreProcessor(this);
	}

	public byte[] preProcess(ClassLoader loader, String className, byte[] bytes) {
		byte[] newBytes = bytes;

		if (stack.empty()) {
			stack.push(className);
			newBytes = preProcessor.preProcess(className, bytes, loader, null);
			stack.pop();
		}

		return newBytes;
	}

	private static class ThreadLocalStack extends ThreadLocal {

		public boolean empty() {
			Stack stack = (Stack) get();
			return stack.empty();
		}

		public Object peek() {
			Object obj = null;
			Stack stack = (Stack) get();
			if (!stack.empty())
				obj = stack.peek();
			return obj;
		}

		public void push(Object obj) {
			Stack stack = (Stack) get();
			if (!stack.empty() && obj == stack.peek())
				throw new RuntimeException(obj.toString());
			stack.push(obj);
		}

		public Object pop() {
			Stack stack = (Stack) get();
			return stack.pop();
		}

		protected Object initialValue() {
			return new Stack();
		}
	}

}
