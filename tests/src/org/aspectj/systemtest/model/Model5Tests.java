/********************************************************************
 * Copyright (c) 2006 Contributors. All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: IBM Corporation - initial API and implementation 
 * 				 Helen Hawkins   - initial version
 *******************************************************************/
package org.aspectj.systemtest.model;

import java.io.File;

import junit.framework.Test;

import org.aspectj.testing.XMLBasedAjcTestCase;

/**
 * Tests the model when there is a requirement on Java5 features.
 * @see org.aspectj.systemtest.model.ModelTestCase 
 */
public class Model5Tests extends ModelTestCase {

	static {
		// Switch this to true for a single iteration if you want to reconstruct the
		// 'expected model' files.
		regenerate = false;
		// Switch this to true if you want to debug the comparison
		debugTest = false;
	}
	
	// ///////////////////////////////////////
	public static Test suite() {
		return XMLBasedAjcTestCase.loadSuite(Model5Tests.class);
	}

	protected File getSpecFile() {
		return new File("../tests/src/org/aspectj/systemtest/model/model.xml");
	}

}
