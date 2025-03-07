/*******************************************************************************
 * Copyright (c) 2025 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *******************************************************************************/
package org.aspectj.systemtest.ajc1923;

import org.aspectj.testing.JavaVersionSpecificXMLBasedAjcTestCase;
import org.aspectj.testing.XMLBasedAjcTestCase;

import junit.framework.Test;

/**
 * @author Andy Clement
 */
public class Java23PreviewFeaturesTests extends JavaVersionSpecificXMLBasedAjcTestCase {

  public Java23PreviewFeaturesTests() {
    super(23, 23);
  }

  public static Test suite() {
    return XMLBasedAjcTestCase.loadSuite(Java23PreviewFeaturesTests.class);
  }
  
  public void testJep455PrimitivePatternsSwitch1() {
	  runTest("primitive types patterns - switch");
  }

  @Override
  protected java.net.URL getSpecFile() {
    return getClassResource("ajc1923.xml");
  }

}
