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
package org.aspectj.systemtest.ajc162;

import java.io.File;

import junit.framework.Test;

import org.aspectj.testing.XMLBasedAjcTestCase;

public class Ajc162Tests extends org.aspectj.testing.XMLBasedAjcTestCase {
	
	// AspectJ1.6.2	
	public void testAnnotationDecp_pr239441() { runTest("annotation decp"); }

	public static Test suite() {
      return XMLBasedAjcTestCase.loadSuite(Ajc162Tests.class);
    }

    protected File getSpecFile() {
      return new File("../tests/src/org/aspectj/systemtest/ajc162/ajc162.xml");
    }
  
}