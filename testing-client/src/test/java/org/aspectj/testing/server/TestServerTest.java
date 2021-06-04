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
package org.aspectj.testing.server;

import java.io.IOException;

import junit.framework.TestCase;

import static org.aspectj.testing.server.TestServer.REGEX_PROJECT_ROOT_FOLDER;

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

	public void testFindProjectRootDirectory() throws IOException {
		// Give developers some advice in the build log about why their LTW tests fail
		assertNotNull(
			"Cannot find AspectJ project root folder. " +
				"This will lead to subsequent failures in all tests using class TestServer. " +
				"Please make sure to name your project root folder 'org.aspectj', 'AspectJ' or " +
				"something else matching regex '"+ REGEX_PROJECT_ROOT_FOLDER+"'.",
			server.findProjectRootFolder()
		);
	}

	public void testSetWorkingDirectory() {
		server.setWorkingDirectory("../testing-client/testdata");
	}
}
