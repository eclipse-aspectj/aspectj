/* *******************************************************************
 * Copyright (c) 2004 IBM
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andy Clement -     initial implementation
 * ******************************************************************/

package org.aspectj.apache.bcel.classfile.tests;

import org.aspectj.apache.bcel.classfile.Code;
import org.aspectj.apache.bcel.classfile.JavaClass;
import org.aspectj.apache.bcel.classfile.LocalVariableTable;
import org.aspectj.apache.bcel.classfile.Method;
import org.aspectj.apache.bcel.classfile.tests.BcelTestCase;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicReference;


public class LocalVariableTableConcurrencyTest extends BcelTestCase {

	private final int nThreads = Runtime.getRuntime().availableProcessors();

	private final ExecutorService[] workers = new ExecutorService[nThreads];

	private LocalVariableTable reference;

	protected void setUp() throws Exception {
		super.setUp();
		for (int i = 0; i < nThreads; i++) workers[i] = Executors.newSingleThreadExecutor();

		JavaClass clazz = getClassFromJar("SimpleGenericsProgram");

		Method mainMethod = getMethod(clazz,"main");
		Code codeAttr = (Code) findAttribute("Code",mainMethod.getAttributes());

		reference =
				(LocalVariableTable) findAttribute("LocalVariableTable",codeAttr.getAttributes());
	}

	private LocalVariableTable createReferenceCopy() {
		try {
			return (LocalVariableTable) reference.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException("Faileed to clone LocalVariableTable", e);
		}
	}

	public void testLocalVariableTableAttributeConcurrency() throws RuntimeException, InterruptedException {
		final AtomicReference<RuntimeException> error = new AtomicReference<>();
		for (int i = 0; i < 100000; i++) {
			LocalVariableTable sharedInstance = createReferenceCopy();
			CountDownLatch preStart = new CountDownLatch(nThreads);
			Semaphore start = new Semaphore(0);
			CountDownLatch finish = new CountDownLatch(nThreads);

			for (int j = 0; j < nThreads; j++) {
				final boolean needsDelay = j > 0;
				workers[j].execute(() -> {
					preStart.countDown();
					start.acquireUninterruptibly();
					// trying to trigger concurrent unpacking - one tread should enter unpack() when other is about to leave it
					if (needsDelay) createReferenceCopy().getTableLength();
					try {
						sharedInstance.getTableLength();
					}
					catch (RuntimeException ex) {
						error.compareAndSet(null, ex);
					}
					finish.countDown();
				});
			}

			preStart.await();
			start.release(nThreads);
			finish.await();

			if (error.get() != null) throw error.get();
		}
	}

	protected void tearDown() throws Exception {
		for (int i = 0; i < nThreads; i++) if (workers[i] != null) workers[i].shutdownNow();
		super.tearDown();
	}
}
