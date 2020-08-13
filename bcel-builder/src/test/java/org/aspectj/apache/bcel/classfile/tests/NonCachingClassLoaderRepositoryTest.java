/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache BCEL" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache BCEL", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package org.aspectj.apache.bcel.classfile.tests;

import java.util.ArrayList;
import java.util.List;

import org.aspectj.apache.bcel.classfile.JavaClass;
import org.aspectj.apache.bcel.util.NonCachingClassLoaderRepository;

import junit.framework.TestCase;

/**
 * @author Kristian Rosenvold
 */
public class NonCachingClassLoaderRepositoryTest extends TestCase {

	private final NonCachingClassLoaderRepository nonCachingClassLoaderRepository = new NonCachingClassLoaderRepository(
			NonCachingClassLoaderRepositoryTest.class.getClassLoader());

	protected void setUp() throws Exception {
		super.setUp();
	}

	abstract class DoneChecker implements Runnable {
		private volatile boolean success = false;
		private volatile boolean done = false;

		public boolean isSuccess() {
			return success;
		}

		public boolean isDone() {
			return done;
		}

		protected void setDone(boolean successFully) {
			success = successFully;
			done = true;
		}

		public abstract void run();
	}

	class Loader extends DoneChecker implements Runnable {
		public void run() {
			try {
				JavaClass javaClass = nonCachingClassLoaderRepository.loadClass(NonCachingClassLoaderRepositoryTest.class
						.getCanonicalName());
				nonCachingClassLoaderRepository.clear();
				setDone(true);
			} catch (Throwable e) {
				e.printStackTrace(System.out);
				setDone(false);
			}
		}
	}

	class Clearer extends DoneChecker implements Runnable {
		public void run() {
			try {
				nonCachingClassLoaderRepository.clear();
				setDone(true);
			} catch (Throwable e) {
				e.printStackTrace(System.out);
				setDone(false);
			}
		}
	}

	public void testConcurrency() throws ClassNotFoundException, InterruptedException {
		List<DoneChecker> loaders = new ArrayList<>();
		int i1 = 1000;
		for (int i = 0; i < i1; i++) {
			DoneChecker loader = new Loader();
			loaders.add(loader);
			new Thread(loader).start();
			DoneChecker clearer = new Clearer();
			loaders.add(clearer);
			new Thread(clearer).start();
		}

		for (int i = 0; i < i1 * 2; i++) {
			DoneChecker loader = loaders.get(i);
			while (!loader.isDone()) {
				Thread.sleep(10);
			}
			assertTrue("Loader " + i + " is supposed to run successfully", loader.isSuccess());
		}

	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

}
