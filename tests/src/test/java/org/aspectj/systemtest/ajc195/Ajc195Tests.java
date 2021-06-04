/*******************************************************************************
 * Copyright (c) 2019 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *******************************************************************************/
package org.aspectj.systemtest.ajc195;

import org.aspectj.testing.XMLBasedAjcTestCase;

import junit.framework.Test;

/**
 * @author Andy Clement
 */
public class Ajc195Tests extends XMLBasedAjcTestCase {

	public void testAtDecpNPE_code_550494() {
		runTest("at decp npe - code");
	}

	public void testAtDecpNPE_anno_550494() {
		runTest("at decp npe - anno");
	}

	public void testAvoidWeavingSwitchInfrastructure() {
		runTest("avoid weaving switch infrastructure");
	}

	public void testFinallyBlocksAndUnlinkingAndExceptions() {
		runTest("around finally blocks and unlinking");
	}


	// ---

	public static Test suite() {
		return XMLBasedAjcTestCase.loadSuite(Ajc195Tests.class);
	}

	@Override
	protected java.net.URL getSpecFile() {
		return getClassResource("ajc195.xml");
	}

}
