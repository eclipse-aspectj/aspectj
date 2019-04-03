/* *******************************************************************
 * Copyright (c) 2018 Contributors
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Andy Clement
 * ******************************************************************/
package org.aspectj.testing;

import org.aspectj.util.LangUtil;

/**
 * Makes sure tests are running on the right level of JDK.
 * 
 * @author Andy Clement
 */
public abstract class XMLBasedAjcTestCaseForJava11OrLater extends XMLBasedAjcTestCase {

	@Override
	public void runTest(String title) {
		if (!LangUtil.is11VMOrGreater()) {
			throw new IllegalStateException("These tests should be run on Java 11 or later");
		}
		super.runTest(title);
	}
	
}
