/*******************************************************************************
 * Copyright (c) 2024 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *******************************************************************************/
package org.aspectj.systemtest.ajc1922;

import junit.framework.Test;
import org.aspectj.systemtest.ajc10x.Ajc10xTests;
import org.aspectj.testing.JavaVersionSpecificXMLBasedAjcTestCase;
import org.aspectj.testing.XMLBasedAjcTestCase;

/**
 * @author Alexander Kriegisch
 */
public class Java22PreviewFeaturesTests extends JavaVersionSpecificXMLBasedAjcTestCase {

  public Java22PreviewFeaturesTests() {
    super(22, 22);
  }

  public void testStringPatterns() {
    runTest("string patterns");
  }

  public void testStringPatternsAspect() {
    runTest("string patterns aspect");
  }

  public void testNamedClassWithSimpleMainMethod() {
    runTest("named class with simple main method");
  }

  public void testNamedAspectWithSimpleMainMethod() {
    runTest("named aspect with simple main method");
  }

  /**
   * Still not implemented with the Java 21 release Eclipse 2023-12 (4.30),
   * see <a href="https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1106">GitHub issue 1106</a>.
   */
  public void testUnnamedClassWithSimpleMainMethod() {
    runTest("unnamed class with simple main method");
  }

  public static Test suite() {
    return XMLBasedAjcTestCase.loadSuite(Java22PreviewFeaturesTests.class);
  }

  @Override
  protected java.net.URL getSpecFile() {
    return getClassResource("ajc1922.xml");
  }

}
