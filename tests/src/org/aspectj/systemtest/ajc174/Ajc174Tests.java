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
package org.aspectj.systemtest.ajc174;

import java.io.File;

import junit.framework.Test;

import org.aspectj.testing.XMLBasedAjcTestCase;

/**
 * @author Andy Clement
 */
public class Ajc174Tests extends org.aspectj.testing.XMLBasedAjcTestCase {
	
	public void testSuperItdCtor_413378() throws Exception {
		runTest("super itd ctor");
	}
	
	// ---

	public static Test suite() {
		return XMLBasedAjcTestCase.loadSuite(Ajc174Tests.class);
	}

	@Override
	protected File getSpecFile() {
		return new File("../tests/src/org/aspectj/systemtest/ajc174/ajc174.xml");
	}

}
