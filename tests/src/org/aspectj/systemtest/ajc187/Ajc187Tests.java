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
package org.aspectj.systemtest.ajc187;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

import junit.framework.Test;

import org.aspectj.testing.XMLBasedAjcTestCase;

/**
 * @author Andy Clement
 */
public class Ajc187Tests extends org.aspectj.testing.XMLBasedAjcTestCase {

	public void testLambda_470633() throws Exception {
		runTest("lambda");
	}
	
//	public void testBrokenJava_469889() throws Exception {
//		runTest("broken java");
//	}

	// ---

	public static Test suite() {
		return XMLBasedAjcTestCase.loadSuite(Ajc187Tests.class);
	}

	@Override
	protected File getSpecFile() {
		return getClassResource("ajc187.xml");
	}

}
