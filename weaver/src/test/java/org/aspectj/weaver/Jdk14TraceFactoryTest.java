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

import org.aspectj.weaver.tools.Jdk14TraceFactory;
import org.aspectj.weaver.tools.Trace;

import junit.framework.TestCase;

public class Jdk14TraceFactoryTest extends TestCase {

	public void testJdk14TraceFactory() {
		new Jdk14TraceFactory();
	}

	public void testGetTrace() {
		Jdk14TraceFactory factory = new Jdk14TraceFactory();
		Trace trace = factory.getTrace(getClass());
		assertFalse("Tracing should be disbled by default",trace.isTraceEnabled());
	}

}
