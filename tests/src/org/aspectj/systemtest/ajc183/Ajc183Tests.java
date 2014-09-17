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
package org.aspectj.systemtest.ajc183;

import java.io.File;

import junit.framework.Test;

import org.aspectj.testing.XMLBasedAjcTestCase;

/**
 * @author Andy Clement
 */
public class Ajc183Tests extends org.aspectj.testing.XMLBasedAjcTestCase {

	public void testAbstractAspectNPE_444398() {
		runTest("abstract aspect npe");
	}
	
//	public void testVerifyError_443447() {
//		runTest("verifyerror");
//	}
//	
//	public void testAnnoStyleDecp_442425() {
//		runTest("anno style decp");
//	}

	// ---

	public static Test suite() {
		return XMLBasedAjcTestCase.loadSuite(Ajc183Tests.class);
	}

	@Override
	protected File getSpecFile() {
        return getClassResource("ajc183.xml");
	}

}
