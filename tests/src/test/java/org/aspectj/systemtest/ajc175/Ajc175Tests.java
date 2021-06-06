/*******************************************************************************
 * Copyright (c) 2013 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors:
 *    Andy Clement - initial API and implementation
 *******************************************************************************/
package org.aspectj.systemtest.ajc175;

import org.aspectj.testing.XMLBasedAjcTestCase;

import junit.framework.Test;

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

	protected java.net.URL getSpecFile() {
		return getClassResource("ajc175.xml");
	}

	public static void main(String[] args) {

	}
}
