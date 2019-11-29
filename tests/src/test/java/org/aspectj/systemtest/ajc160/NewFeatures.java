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

import org.aspectj.testing.XMLBasedAjcTestCase;

import junit.framework.Test;

public class NewFeatures extends org.aspectj.testing.XMLBasedAjcTestCase {

  // Supporting -Xset:weaveJavaPackages=true and -Xset:weaveJavaxPackages=true
  public void testWeaveJavaxClassesNo() { runTest("weave javax classes - no");}
  public void testWeaveJavaxClassesYes() { runTest("weave javax classes - yes");}

  /////////////////////////////////////////
  public static Test suite() {
    return XMLBasedAjcTestCase.loadSuite(NewFeatures.class);
  }

  protected java.net.URL getSpecFile() {
    return getClassResource("newfeatures-tests.xml");
  }

  
}
