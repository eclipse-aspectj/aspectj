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
package org.aspectj.systemtest.ajc180;

import java.io.File;

import junit.framework.Test;

import org.aspectj.testing.XMLBasedAjcTestCase;

/**
 * @author Andy Clement
 */
public class Ajc180Tests extends org.aspectj.testing.XMLBasedAjcTestCase {
	
	public void testJava8Code() throws Exception {
		runTest("first advised java 8 code");
	}
	
	// ---

	public static Test suite() {
		return XMLBasedAjcTestCase.loadSuite(Ajc180Tests.class);
	}

	@Override
	protected File getSpecFile() {
		return new File("../tests/src/org/aspectj/systemtest/ajc180/ajc180.xml");
	}

}
