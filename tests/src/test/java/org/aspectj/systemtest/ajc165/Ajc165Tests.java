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

import java.util.List;

import org.aspectj.bridge.IMessage;
import org.aspectj.testing.XMLBasedAjcTestCase;
import org.aspectj.weaver.LintMessage;

import junit.framework.Test;

public class Ajc165Tests extends org.aspectj.testing.XMLBasedAjcTestCase {

// fix is too disruptive for 1.6.5
//	public void testGenericsBridge_pr279983() {
//		runTest("generics bridge");
//	}

	public void testVerifyError_pr277959() {
		runTest("verifyerror");
	}

	// on the build machine (linux) - these tests don't expect the right output - i suspect due to CR/LF issues
	// since that will affect the generated name of the if methods

	// public void testIfNames_pr277508() {
	// runTest("if method names");
	// }
	//
	// public void testIfNames_pr277508_2() {
	// runTest("if method names - 2");
	// }

	public void testDecAnnoMethod_pr275625() {
		runTest("dec anno method");
	}

	public void testDecAnnoField_pr275625() {
		runTest("dec anno field");
	}

	// check ITD can replace a generated default constructor
	public void testItdDefaultCtor_pr275032() {
		runTest("itd default ctor");
	}

	// check ITD can't overwrite an existing constructor
	public void testItdDefaultCtor_pr275032_2() {
		runTest("itd default ctor - 2");
	}

	// binary weaving version of case 2 - check ITD can't overwrite an existing constructor
	public void testItdDefaultCtor_pr275032_3() {
		runTest("itd default ctor - 3");
	}

	// binary weaving version of case 4 - check ITD can replace a generated default constructor
	public void testItdDefaultCtor_pr275032_4() {
		runTest("itd default ctor - 4");
	}

	public void testVerifyOnAnnoBind_pr273628() {
		runTest("verifyerror on anno bind");
	}

	public void testFunkyPointcut_pr272233() {
		runTest("funky pointcut");
	}

	public void testFunkyPointcut_pr272233_2() {
		runTest("funky pointcut 2");
	}

	public void testAnnotationStyle_pr265356() {
		runTest("annotation style message positions");
		List<IMessage> ms = ajc.getLastCompilationResult().getWarningMessages();
		boolean checked = true;
		// Look for the message relating to 'List' and check the offsets
		for (IMessage iMessage : ms) {
			LintMessage m = (LintMessage) iMessage;
			if (m.toString().contains("List")) {
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

	public void testGenericITD_pr272825() {
		runTest("generic ITD");
	}

	// ---

	public static Test suite() {
		return XMLBasedAjcTestCase.loadSuite(Ajc165Tests.class);
	}

	@Override
	protected java.net.URL getSpecFile() {
		return getClassResource("ajc165.xml");
	}

}