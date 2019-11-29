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
package org.aspectj.weaver;

import java.util.ArrayList;

import org.aspectj.weaver.tools.AbstractTrace;
import org.aspectj.weaver.tools.DefaultTrace;
import org.aspectj.weaver.tools.Traceable;

import junit.framework.TestCase;

public abstract class AbstractTraceTest extends TestCase {

	protected AbstractTrace trace;

	public void testIsTraceEnabled() {
		DefaultTrace trace = new DefaultTrace(getClass());
		assertFalse(trace.isTraceEnabled());
	}

	public void testEnterWithThisAndArgs() {
		trace.enter("testEnterWithThisAndArgs",this,new Object[] { "arg1", "arg2" });
	}

	public void testEnterWithThisAndArray() {
		Object arg1 = new String[] { "s1", "s2" };
		Object arg2 = new char[] { 'a', 'b', 'c' };
		trace.enter(getName(),this,new Object[] { arg1, arg2 });
	}

	public void testEnterWithThisAndCollection() {
		Object arg1 = new ArrayList();
		trace.enter(getName(),this,new Object[] { arg1 });
	}

	public void testEnterWithThisAndTraceable () {
		Object arg1 = new Traceable() {

			public String toTraceString() {
				return getClass().getName() + "[Traceable]";
			}
			
		};
		trace.enter(getName(),this,new Object[] { arg1 });
	}

	public void testEnterWithThisAndToStringException () {
		Object arg1 = new Object() {

			public String toString() {
				throw new RuntimeException("toString() can throw an Exception");
			}
			
		};
		trace.enter(getName(),this,new Object[] { arg1 });
	}

	public void testEnterWithThisAndHashCodeException () {
		Object arg1 = new Object() {

			public int hashCode() {
				throw new RuntimeException("hashCode can throw an Exception");
			}
			
		};
		trace.enter(getName(),this,new Object[] { arg1 });
	}

	public void testEnterWithThisAndClassLoader () {
		Object arg1 = new ClassLoader() {

			public String toString() {
				throw new Error("Don't call ClassLoader.toString()");
			}
			
		};
		trace.enter(getName(),this,new Object[] { arg1 });
	}

	public void testEnterWithThis() {
		trace.enter("testEnterWithThis",this);
	}

	public void testEnter() {
		trace.enter("testEnter");
	}

	public void testExitWithReturn() {
		trace.exit("testExitWithReturn","ret");
	}

	public void testExitWithThrowable() {
		trace.exit("testExitWithThrowable",new RuntimeException());
	}

	public void testExit() {
		trace.exit("testExit");
	}

	public void testEvent() {
		trace.event("testEvent");
	}

	public void testEventWithThisAndArgs() {
		trace.event("testEventWithThisAndArgs",this,new Object[] { "arg1", "arg2" });
	}

	public void testEventWithThisAndArg() {
		trace.event("testEventWithThisAndArg",this,"arg1");
	}

	public void testDebug() {
		trace.debug("debug");
	}

	public void testInfo() {
		trace.info("information");
	}

	public void testWarn() {
		trace.warn("warning");
	}

	public void testWarnWithException() {
		trace.warn("warning",new RuntimeException("warning"));
	}

	public void testError() {
		trace.error("error");
	}

	public void testErrorWithException() {
		trace.error("error",new RuntimeException("error"));
	}

	public void testFatal() {
		trace.fatal("fatal");
	}

	public void testFatalWithException() {
		trace.fatal("fatal",new RuntimeException("fatal"));
	}

}
