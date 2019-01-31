/*******************************************************************************
 * Copyright (c) 2012 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andy Clement - initial API and implementation
 *******************************************************************************/
package org.aspectj.systemtest.ajc171;

import java.io.File;

import junit.framework.Test;

import org.aspectj.testing.XMLBasedAjcTestCase;

/**
 * @author Andy Clement
 */ 
public class Ajc171Tests_need17jre extends org.aspectj.testing.XMLBasedAjcTestCase {

	public void testSoft17_pr387444() {
		runTest("soft 17");
	}
	
	public void testSoft17_pr387444_2() {
		runTest("soft 17 2");
	}

	// ---

	public static Test suite() {
		return XMLBasedAjcTestCase.loadSuite(Ajc171Tests_need17jre.class);
	}

	@Override
	protected File getSpecFile() {
		return new File("../tests/src/org/aspectj/systemtest/ajc171/ajc171.xml");
	}

}
