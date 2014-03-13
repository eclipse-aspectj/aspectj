/*******************************************************************************
 * Copyright (c) 2013 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andy Clement - initial API and implementation
 *******************************************************************************/
package org.aspectj.systemtest.ajc175;

import java.io.File;

import junit.framework.Test;

import org.aspectj.testing.XMLBasedAjcTestCase;

/**
 * @author Andy Clement
 */
public class Ajc175Tests extends org.aspectj.testing.XMLBasedAjcTestCase {
	
//	public void testIllegalAccessError_430243() throws Exception {
//		runTest("illegalaccesserror");
//	}
	
	public void testVertxVerifyError_423257() throws Exception {
		runTest("vertx verify error");
	}
	
	// ---

	public static Test suite() {
		return XMLBasedAjcTestCase.loadSuite(Ajc175Tests.class);
	}

	protected File getSpecFile() {
		return new File("../tests/src/org/aspectj/systemtest/ajc175/ajc175.xml");
	}

	public static void main(String[] args) {
		
	}
}
