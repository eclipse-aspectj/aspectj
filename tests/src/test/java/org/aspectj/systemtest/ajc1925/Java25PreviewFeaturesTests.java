/*******************************************************************************
 * Copyright (c) 2025 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *******************************************************************************/
package org.aspectj.systemtest.ajc1925;

import org.aspectj.testing.JavaVersionSpecificXMLBasedAjcTestCase;
import org.aspectj.testing.XMLBasedAjcTestCase;

import junit.framework.Test;

/**
 * @author Andy Clement
 */
public class Java25PreviewFeaturesTests extends JavaVersionSpecificXMLBasedAjcTestCase {

  public Java25PreviewFeaturesTests() {
    super(25, 25);
  }

  public static Test suite() {
    return XMLBasedAjcTestCase.loadSuite(Java25PreviewFeaturesTests.class);
  }
  
  public void testNothing() {
  }

  @Override
  protected java.net.URL getSpecFile() {
    return getClassResource("ajc1925.xml");
  }

}
