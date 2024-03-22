/*******************************************************************************
 * Copyright (c) 2024 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *******************************************************************************/
package org.aspectj.systemtest.ajc1922;

import junit.framework.Test;
import org.aspectj.apache.bcel.Constants;
import org.aspectj.systemtest.ajc10x.Ajc10xTests;
import org.aspectj.testing.JavaVersionSpecificXMLBasedAjcTestCase;
import org.aspectj.testing.XMLBasedAjcTestCase;

/**
 * @author Alexander Kriegisch
 */
public class Ajc1922TestsJava extends JavaVersionSpecificXMLBasedAjcTestCase {

  private static final Constants.ClassFileVersion classFileVersion = Constants.ClassFileVersion.of(22);

  public Ajc1922TestsJava() {
    super(22);
  }

  public void testUnnamedPatterns() {
    runTest("unnamed patterns");
  }

  public void testUnnamedPatternsAspect() {
    runTest("unnamed patterns aspect");
  }

  /**
   * Same as {@link Ajc10xTests#test052()}, but compiled to target 22 instead of 1.4
   */
  public void testUnderscoreInPointcutPattern1() {
    runTest("underscore can still be used in pointcut patterns on Java 21+ - 1");
  }

  public void testUnderscoreInPointcutPattern2() {
    runTest("underscore can still be used in pointcut patterns on Java 21+ - 2");
  }

  public static Test suite() {
    return XMLBasedAjcTestCase.loadSuite(Ajc1922TestsJava.class);
  }

  @Override
  protected java.net.URL getSpecFile() {
    return getClassResource("ajc1922.xml");
  }

}
