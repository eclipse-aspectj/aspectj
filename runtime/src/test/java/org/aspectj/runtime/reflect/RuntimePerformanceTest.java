/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Webster - initial implementation
 *******************************************************************************/
package org.aspectj.runtime.reflect;

import java.lang.reflect.Method;
import java.util.Timer;
import java.util.TimerTask;

import org.aspectj.lang.Signature;

import junit.framework.TestCase;

public class RuntimePerformanceTest extends TestCase {

	private static final Timer timer = new Timer(true);
	private static final long TIMEOUT = 10000;
	private static final long ITERATIONS = 1000000;
	private static final long WARMUP_ITERATIONS = 10000;
	private static final long EXPECTED_RATIO = 8;
	private static final Factory factory = new Factory("RutimePerformanceTest.java",RuntimePerformanceTest.class);

	private boolean savedUseCaches;
	private Method method;
	private Signature signature;

	private TimerTask task;
	private boolean abort;

	public RuntimePerformanceTest(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		/* Save default state */
		savedUseCaches = SignatureImpl.getUseCache();

		/* If a test takes too long we can kill it and fail */
		abort = false;
		task = new TimerTask() {
			@Override
			public void run () {
				abort = true;
			}
		};
		timer.schedule(task,TIMEOUT);
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();

		/* Restore default state */
		SignatureImpl.setUseCache(savedUseCaches);

		task.cancel();
	}

	public void testempty() {}

	// No longer valid with Java being so quick now...
	public void xtestToString () {
		Signature signature = makeMethodSig("test");

		SignatureImpl.setUseCache(false);
		warmUp(signature);
		long noCache = invokeSignatureToString(signature,ITERATIONS/EXPECTED_RATIO);
		System.out.println("noCache=" + noCache);

		SignatureImpl.setUseCache(true);
		warmUp(signature);
		long cache = invokeSignatureToString(signature,ITERATIONS);
		System.out.println("cache=" + cache);

		long ratio = (EXPECTED_RATIO*noCache/cache);
		System.out.println("ratio=" + ratio);
		assertTrue("Using cache should be " + EXPECTED_RATIO + " times faster: " + ratio,(ratio >= EXPECTED_RATIO));
	}

	private long invokeSignatureToString (Signature sig, long iterations) {
		long start = System.currentTimeMillis();
		String s;

		for (long l = 0; !abort && (l < iterations); l++) {
			s = sig.toShortString();
			s = sig.toString();
			s = sig.toLongString();
		}
		if (abort) throw new RuntimeException("invokeSignatureToString aborted after " + (TIMEOUT/1000) + " seconds");

		long finish = System.currentTimeMillis();
		return (finish-start);
	}

	private void warmUp (Signature sig) {
		invokeSignatureToString(sig,WARMUP_ITERATIONS);
	}

	private Signature makeMethodSig (String methodName) {
		Class clazz = getClass();
		Class[] parameterTypes = new Class[] { String.class };
		String[] parameterNames = new String[] { "s" };
		Class[] exceptionTypes = new Class[] {};
		Class returnType = Void.TYPE;
		return factory.makeMethodSig(1,methodName,clazz,parameterTypes,parameterNames,exceptionTypes,returnType);
	}
}