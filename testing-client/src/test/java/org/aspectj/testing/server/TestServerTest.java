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
package org.aspectj.testing.server;

import java.io.IOException;

import junit.framework.TestCase;

public class TestServerTest extends TestCase {

	private TestServer server;
	
	protected void setUp() throws Exception {
		super.setUp();
		server = new TestServer();
		server.setExitOntError(false);
	}

	public void testInitialize() {
		try {
			server.setWorkingDirectory("../testing-client/testdata");
			server.initialize();
		}
		catch (IOException ex) {
			fail(ex.toString());
		}
	}

	public void testSetWorkingDirectory() {
		server.setWorkingDirectory("../testing-client/testdata");
	}
}
