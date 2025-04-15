/*******************************************************************************
 * Copyright (c) 2025 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *******************************************************************************/
package org.aspectj.systemtest.ajc1924;

import org.aspectj.testing.JavaVersionSpecificXMLBasedAjcTestCase;
import org.aspectj.testing.XMLBasedAjcTestCase;

import junit.framework.Test;

/**
 * @author Andy Clement
 */
public class Java24PreviewFeaturesTests extends JavaVersionSpecificXMLBasedAjcTestCase {

  public Java24PreviewFeaturesTests() {
    super(24, 24);
  }

  public static Test suite() {
    return XMLBasedAjcTestCase.loadSuite(Java24PreviewFeaturesTests.class);
  }
  
  public void testNothing() {
  }

  @Override
  protected java.net.URL getSpecFile() {
    return getClassResource("ajc1924.xml");
  }

}
