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

import org.aspectj.bridge.Version;

/**
 * @author Mik Kersten
 */
public class ExecutionTestCase extends AjdocTestCase {

	public void testVersionMatch() {
		String ajdocVersion = Main.getVersion();
		String compilerVersion = Version.getText();
		assertTrue("version check", ajdocVersion.endsWith(compilerVersion));
	}

	public void testFailingBuild() {
		initialiseProject("failing-build");
		File file1 = new File(getAbsoluteProjectDir() + File.separatorChar + "Fail.java");
		String[] args = { file1.getAbsolutePath() };
		org.aspectj.tools.ajdoc.Main.main(args);
		assertTrue(Main.hasAborted());
	}
}
