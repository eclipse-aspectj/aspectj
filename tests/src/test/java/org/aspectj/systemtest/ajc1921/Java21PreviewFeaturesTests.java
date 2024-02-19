/*******************************************************************************
 * Copyright (c) 2023 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *******************************************************************************/
package org.aspectj.systemtest.ajc1921;

import junit.framework.Test;
import org.aspectj.systemtest.ajc10x.Ajc10xTests;
import org.aspectj.testing.JavaVersionSpecificXMLBasedAjcTestCase;
import org.aspectj.testing.XMLBasedAjcTestCase;

/**
 * @author Alexander Kriegisch
 */
public class Java21PreviewFeaturesTests extends JavaVersionSpecificXMLBasedAjcTestCase {

  public Java21PreviewFeaturesTests() {
    super(21, 21);
  }

  public void testStringPatterns() {
    runTest("string patterns");
  }

  public void testStringPatternsAspect() {
    runTest("string patterns aspect");
  }

  /**
   * Still not implemented with the Java 21 release Eclipse 2023-12 (4.30),
   * see <a href="https://github.com/eclipse-jdt/eclipse.jdt.core/issues/893">GitHub issue 893</a>.
   * <p>
   * TODO: Activate after JDT Core implementation and merge.
   */
  public void testUnnamedPatterns() {
    //runTest("unnamed patterns");
    System.out.println("Unnamed patterns still are not implemented with the Java 21 release Eclipse 2023-12 (4.30)");
  }

  /**
   * Still not implemented with the Java 21 release Eclipse 2023-12 (4.30),
   * see <a href="https://github.com/eclipse-jdt/eclipse.jdt.core/issues/893">GitHub issue 893</a>.
   * <p>
   * TODO: Activate after JDT Core implementation and merge.
   */
  public void testUnnamedPatternsAspect() {
    //runTest("unnamed patterns aspect");
    System.out.println("Unnamed patterns still are not implemented with the Java 21 release Eclipse 2023-12 (4.30)");
  }

  /**
   * Same as {@link Ajc10xTests#test052()}, but compiled to target 21 instead of 1.4
   */
  public void testUnderscoreInPointcutPattern1() {
    runTest("underscore can still be used in pointcut patterns on Java 21+ - 1");
  }

  public void testUnderscoreInPointcutPattern2() {
    runTest("underscore can still be used in pointcut patterns on Java 21+ - 2");
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
   * <p>
   * TODO: Activate after JDT Core implementation and merge.
   */
  public void testUnnamedClassWithSimpleMainMethod() {
    //runTest("unnamed class with simple main method");
    System.out.println("Unnamed classes still are not implemented with the Java 21 release Eclipse 2023-12 (4.30)");
  }

  public static Test suite() {
    return XMLBasedAjcTestCase.loadSuite(Java21PreviewFeaturesTests.class);
  }

  @Override
  protected java.net.URL getSpecFile() {
    return getClassResource("ajc1921.xml");
  }

}
