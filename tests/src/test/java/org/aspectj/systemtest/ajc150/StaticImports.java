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

import org.aspectj.testing.XMLBasedAjcTestCase;

import junit.framework.Test;

public class StaticImports extends XMLBasedAjcTestCase {

   public static Test suite() {
     return XMLBasedAjcTestCase.loadSuite(StaticImports.class);
   }

   protected java.net.URL getSpecFile() {
     return getClassResource("ajc150.xml");
   }
   
  public void testImportStaticSystemDotOut() {
   runTest("import static java.lang.System.out");
  }

}
