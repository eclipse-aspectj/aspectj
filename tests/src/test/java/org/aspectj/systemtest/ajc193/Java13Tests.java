/*******************************************************************************
 * Copyright (c) 2019 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.aspectj.systemtest.ajc193;

import org.aspectj.testing.XMLBasedAjcTestCase;
import org.aspectj.testing.XMLBasedAjcTestCaseForJava13OrLater;

import junit.framework.Test;

/**
 * @author Andy Clement
 */
public class Java13Tests extends XMLBasedAjcTestCaseForJava13OrLater {

	// ---

	public static Test suite() {
		return XMLBasedAjcTestCase.loadSuite(Java13Tests.class);
	}

	@Override
	protected java.net.URL getSpecFile() {
		return getClassResource("ajc193.xml");
	}

}
