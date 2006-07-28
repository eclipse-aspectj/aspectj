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

import org.aspectj.weaver.tools.Trace;
import org.aspectj.weaver.tools.TraceFactory;

import junit.framework.TestCase;

public class TraceFactoryTest extends TestCase {

	public void testGetTraceFactory() {
		TraceFactory traceFactory = TraceFactory.getTraceFactory();
		assertNotNull(traceFactory);
	}

	public void testGetTrace() {
		TraceFactory traceFactory = TraceFactory.getTraceFactory();
		Trace trace = traceFactory.getTrace(getClass());
		assertNotNull(trace);
	}

}
