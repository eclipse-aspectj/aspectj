/*******************************************************************************
 * Copyright (c) 2005 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
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
