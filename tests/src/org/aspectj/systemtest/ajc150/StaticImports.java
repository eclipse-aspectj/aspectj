/*******************************************************************************
 * Copyright (c) 2005 Contributors 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Andrew Huff - initial implementation
 *******************************************************************************/
package org.aspectj.systemtest.ajc150;

import java.io.File;

import junit.framework.Test;

import org.aspectj.testing.XMLBasedAjcTestCase;

public class StaticImports extends XMLBasedAjcTestCase {

   public static Test suite() {
     return XMLBasedAjcTestCase.loadSuite(StaticImports.class);
   }

   protected File getSpecFile() {
     return new File("../tests/src/org/aspectj/systemtest/ajc150/ajc150.xml");
   }
   
  public void testImportStaticSystemDotOut() {
   runTest("import static java.lang.System.out");
  }

}
