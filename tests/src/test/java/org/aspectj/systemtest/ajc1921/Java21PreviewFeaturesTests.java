/*******************************************************************************
 * Copyright (c) 2023 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *******************************************************************************/
package org.aspectj.systemtest.ajc1921;

import junit.framework.Test;
import org.aspectj.testing.XMLBasedAjcTestCase;
import org.aspectj.testing.XMLBasedAjcTestCaseForJava21Only;

/**
 * @author Alexander Kriegisch
 */
public class Java21PreviewFeaturesTests extends XMLBasedAjcTestCaseForJava21Only {

  public void testDummyPreviewJava21() {
    //runTest("dummy preview Java 21");
  }

  public static Test suite() {
    return XMLBasedAjcTestCase.loadSuite(Java21PreviewFeaturesTests.class);
  }

  @Override
  protected java.net.URL getSpecFile() {
    return getClassResource("ajc1921.xml");
  }

}
