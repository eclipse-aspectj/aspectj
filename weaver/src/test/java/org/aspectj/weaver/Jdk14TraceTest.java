/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors:
 *     Matthew Webster - initial implementation
 *******************************************************************************/
package org.aspectj.weaver;

import org.aspectj.weaver.tools.DefaultTrace;
import org.aspectj.weaver.tools.Jdk14Trace;

public class Jdk14TraceTest extends AbstractTraceTest {

	protected void setUp() throws Exception {
		super.setUp();
		trace = new Jdk14Trace(getClass());
		trace.setTraceEnabled(true);
	}

	public void testJdk14Trace() {
		new Jdk14Trace(getClass());
	}

	public void testSetTraceEnabled() {
		DefaultTrace trace = new DefaultTrace(getClass());
		trace.setTraceEnabled(true);
		assertTrue(trace.isTraceEnabled());
	}

}
