/*******************************************************************************
 * Copyright (c) 2006 IBM 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andy Clement - initial API and implementation
 *******************************************************************************/
package org.aspectj.systemtest.ajc160;

import java.io.File;

import junit.framework.Test;

import org.aspectj.testing.XMLBasedAjcTestCase;

public class NewFeatures extends org.aspectj.testing.XMLBasedAjcTestCase {

  // Supporting -Xset:weaveJavaPackages=true and -Xset:weaveJavaxPackages=true
  public void testWeaveJavaxClassesNo() { runTest("weave javax classes - no");}
  public void testWeaveJavaxClassesYes() { runTest("weave javax classes - yes");}

  /////////////////////////////////////////
  public static Test suite() {
    return XMLBasedAjcTestCase.loadSuite(NewFeatures.class);
  }

  protected File getSpecFile() {
    return new File("../tests/src/org/aspectj/systemtest/ajc160/newfeatures-tests.xml");
  }

  
}
