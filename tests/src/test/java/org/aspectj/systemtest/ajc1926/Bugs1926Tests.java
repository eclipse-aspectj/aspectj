/*******************************************************************************
 * Copyright (c) 2026 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *******************************************************************************/
package org.aspectj.systemtest.ajc1926;

import junit.framework.Test;
import org.aspectj.testing.XMLBasedAjcTestCase;

/**
 * @author Andy Clement
 */
public class Bugs1926Tests extends XMLBasedAjcTestCase {

  public static Test suite() {
    return XMLBasedAjcTestCase.loadSuite(Bugs1926Tests.class);
  }
  
  public void testNoTestsYet() {
	  
  }

  @Override
  protected java.net.URL getSpecFile() {
    return getClassResource("ajc1926.xml");
  }

}
