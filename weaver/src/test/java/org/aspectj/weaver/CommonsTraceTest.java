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

import org.aspectj.weaver.tools.CommonsTrace;

public class CommonsTraceTest extends AbstractTraceTest {

	protected void setUp() throws Exception {
		super.setUp();
		trace = new CommonsTrace(getClass());
		trace.setTraceEnabled(true);
	}

	public void testCommonsTrace() {
//		CommonsTrace trace =
			new CommonsTrace(getClass());
	}

	public void testSetTraceEnabled() {
		CommonsTrace trace = new CommonsTrace(getClass());
		trace.setTraceEnabled(true);
		/* XXX Need to find out how to turn tracing on */
//		assertTrue(trace.isTraceEnabled());
	}

}
