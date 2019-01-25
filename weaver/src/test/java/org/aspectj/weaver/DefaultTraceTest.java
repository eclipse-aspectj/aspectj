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

import org.aspectj.weaver.tools.DefaultTrace;

public class DefaultTraceTest extends AbstractTraceTest {

	protected void setUp() throws Exception {
		super.setUp();
		trace = new DefaultTrace(getClass());
		trace.setTraceEnabled(true);
	}

	public void testDefaultTrace() {
//		DefaultTrace trace = 
			new DefaultTrace(getClass());
	}

	public void testSetTraceEnabled() {
		DefaultTrace trace = new DefaultTrace(getClass());
		trace.setTraceEnabled(true);
		assertTrue(trace.isTraceEnabled());
	}
	
	public void testSetPrintStream () {
		DefaultTrace trace = new DefaultTrace(getClass());
		trace.setPrintStream(System.out);
	}
}
