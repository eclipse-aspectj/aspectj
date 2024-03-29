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

import org.aspectj.weaver.tools.CommonsTraceFactory;
import org.aspectj.weaver.tools.Trace;

import junit.framework.TestCase;

public class CommonsTraceFactoryTest extends TestCase {

	public void testGetTraceFactory() {
		CommonsTraceFactory factory = new CommonsTraceFactory();
		Trace trace = factory.getTrace(getClass());
		assertFalse("Tracing should be disbled by default",trace.isTraceEnabled());
	}

}
