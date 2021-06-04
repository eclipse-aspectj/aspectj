/* *******************************************************************
 * Copyright (c) 2003 Contributors.
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors:
 *     Mik Kersten     initial implementation
 * ******************************************************************/
 package org.aspectj.tools.ajdoc;

import java.io.File;


/**
 * @author Mik Kersten
 */
public class SpacewarTestCase extends AjdocTestCase {

	private String[] dirs = {"spacewar","coordination"};

	protected void setUp() throws Exception {
		super.setUp();
		initialiseProject("spacewar");
	}

	public void testSimpleExample() {
		runAjdoc(dirs);
	}

	public void testPublicModeExample() {
		runAjdoc("public",dirs);
	}

	public void testPr134063() {
		String lstFile = "spacewar" + File.separatorChar + "demo.lst";
		runAjdoc("private",lstFile);
	}
}
