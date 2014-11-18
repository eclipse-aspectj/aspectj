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

import java.io.File;

import junit.framework.Test;

import org.aspectj.testing.XMLBasedAjcTestCase;

/**
 * @author Andy Clement
 */
public class Ajc185Tests extends org.aspectj.testing.XMLBasedAjcTestCase {

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
	protected File getSpecFile() {
        return getClassResource("ajc185.xml");
	}

}
