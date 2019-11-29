/*******************************************************************************
 * Copyright (c) 2014 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andy Clement - initial API and implementation
 *******************************************************************************/
package org.aspectj.systemtest.ajc185;

import org.aspectj.testing.XMLBasedAjcTestCase;

import junit.framework.Test;

/**
 * @author Andy Clement
 */
public class Ajc185Tests extends org.aspectj.testing.XMLBasedAjcTestCase {

	public void testUnresolvableMember_456357() throws Exception {
		runTest("unresolvable member");
	}
	
	// Waiting on JDT fix. Second test is a 'variant' that is also causing me issues but not JDT it seems. Let's
	// see what happens when we pick up the real fixes.
//	public void testBadAnnos_455608() throws Exception {
//		runTest("bad annos");
//		JavaClass jc = getClassFrom(ajc.getSandboxDirectory(), "Code2");
//		File f = new File(ajc.getSandboxDirectory(), "Code2.class");
//		byte[] data = loadFileAsByteArray(f);
//		// Will throw ClassFormatException if there is a problem
//		new ClassFileReader(data, null);
//	}
//	
//	public void testBadAnnos_455608_2() throws Exception {
//		runTest("bad annos 2");
//		JavaClass jc = getClassFrom(ajc.getSandboxDirectory(), "Code3");
//		File f = new File(ajc.getSandboxDirectory(), "Code3.class");
//		byte[] data = loadFileAsByteArray(f);
//		// Will throw ClassFormatException if there is a problem
//		new ClassFileReader(data, null);
//	}
	
	public void testITDInterface_451966() throws Exception {
		runTest("itd interface");
	}

	public void testITDInterface_451966_2() throws Exception {
		// call made from inner type
		runTest("itd interface - 2");
	}

	// ---

	public static Test suite() {
		return XMLBasedAjcTestCase.loadSuite(Ajc185Tests.class);
	}

	@Override
	protected java.net.URL getSpecFile() {
        return getClassResource("ajc185.xml");
	}

}
