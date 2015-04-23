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
package org.aspectj.systemtest.ajc186;

import java.io.File;

import junit.framework.Test;

import org.aspectj.testing.XMLBasedAjcTestCase;

/**
 * @author Andy Clement
 */
public class Ajc186Tests extends org.aspectj.testing.XMLBasedAjcTestCase {

	public void testMissingExtends() throws Exception {
		runTest("missing extends on generic target");
	}
	
	public void testMissingMethod_462821() throws Exception {
		runTest("missing method");
	}

	// ---

	public static Test suite() {
		return XMLBasedAjcTestCase.loadSuite(Ajc186Tests.class);
	}

	@Override
	protected File getSpecFile() {
        return getClassResource("ajc186.xml");
	}

}
