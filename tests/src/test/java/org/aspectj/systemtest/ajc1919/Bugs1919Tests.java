/*******************************************************************************
 * Copyright (c) 2022 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *******************************************************************************/
package org.aspectj.systemtest.ajc1919;

import junit.framework.Test;
import org.aspectj.testing.XMLBasedAjcTestCase;

/**
 * @author Alexander Kriegisch
 */
public class Bugs1919Tests extends XMLBasedAjcTestCase {

  public void testDeclareAnnotationWithSourceRetention() {
    runTest("declare annotation with SOURCE retention");
  }

  public void testSwitchWith_Integer_MAX_VALUE() {
    runTest("switch with Integer.MAX_VALUE case");
  }

  public void testParenthesisedExpressionWithAjKeyword() {
    runTest("parenthesised expression with AspectJ keyword");
  }

  public static Test suite() {
    return XMLBasedAjcTestCase.loadSuite(Bugs1919Tests.class);
  }

  @Override
  protected java.net.URL getSpecFile() {
    return getClassResource("ajc1919.xml");
  }

}
