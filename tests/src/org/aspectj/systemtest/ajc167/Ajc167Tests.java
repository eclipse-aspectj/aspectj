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
package org.aspectj.systemtest.ajc167;

import java.io.File;

import junit.framework.Test;

import org.aspectj.testing.XMLBasedAjcTestCase;

public class Ajc167Tests extends org.aspectj.testing.XMLBasedAjcTestCase {

	public void testAnnoMatching_pr293203() {
		runTest("anno matching");
	}

	public void testScalaOuterClassNames_pr288064() {
		runTest("outer class names - scala");
	}

	public void testScalaOuterClassNames_pr288064_ltw() {
		runTest("outer class names - scala - ltw");
	}

	// ---

	public static Test suite() {
		return XMLBasedAjcTestCase.loadSuite(Ajc167Tests.class);
	}

	@Override
	protected File getSpecFile() {
		return new File("../tests/src/org/aspectj/systemtest/ajc167/ajc167.xml");
	}

}