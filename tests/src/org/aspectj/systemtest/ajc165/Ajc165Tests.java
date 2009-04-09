/*******************************************************************************
 * Copyright (c) 2008 Contributors 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andy Clement - initial API and implementation
 *******************************************************************************/
package org.aspectj.systemtest.ajc165;

import java.io.File;
import java.util.List;

import junit.framework.Test;

import org.aspectj.testing.XMLBasedAjcTestCase;
import org.aspectj.weaver.LintMessage;

public class Ajc165Tests extends org.aspectj.testing.XMLBasedAjcTestCase {

	public void testAnnotationStyle_pr265356() {
		runTest("annotation style message positions");
		List ms = ajc.getLastCompilationResult().getWarningMessages();
		boolean checked = true;
		// Look for the message relating to 'List' and check the offsets
		for (int i = 0; i < ms.size(); i++) {
			LintMessage m = (LintMessage) ms.get(i);
			if (m.toString().indexOf("List") != -1) {
				// 225/228 on windows - 237/240 on linux
				if (!(m.getSourceStart() == 225 || m.getSourceStart() == 237)) {
					fail("Did not get expected start position, was:" + m.getSourceStart());
				}
				if (!(m.getSourceEnd() == 228 || m.getSourceEnd() == 240)) {
					fail("Did not get expected end position, was:" + m.getSourceEnd());
				}
				checked = true;
			}
		}
		assertTrue("Failed to check the message", checked);
	}

	public void testAroundCall_pr271169() {
		runTest("around call npe");
	}

	// ---

	public static Test suite() {
		return XMLBasedAjcTestCase.loadSuite(Ajc165Tests.class);
	}

	protected File getSpecFile() {
		return new File("../tests/src/org/aspectj/systemtest/ajc165/ajc165.xml");
	}

}