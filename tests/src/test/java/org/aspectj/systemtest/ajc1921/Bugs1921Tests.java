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

/**
 * @author Alexander Kriegisch
 */
public class Bugs1921Tests extends XMLBasedAjcTestCase {

  public void testSwitchWith_Integer_MAX_VALUE() {
    runTest("switch with Integer.MAX_VALUE case");
  }

  public void testParenthesisedExpressionWithAjKeyword() {
    runTest("parenthesised expression with AspectJ keyword");
  }

  public void testInterfaceInnerAspectImplicitlyStatic() {
    runTest("inner aspect of interface is implicitly static");
  }

  public void testExactArrayTypeMatchCompiledTogether() {
    runTest("exact array type matching, aspect compiled together with target class");
  }

  public void testExactArrayTypeMatchCompiledSeparately() {
    runTest("exact array type matching, aspect compiled separately from target class");
  }

  public void testFuzzyArrayTypeMatchCompiledTogether() {
    runTest("fuzzy array type matching, aspect compiled together with target class");
  }

  public void testFuzzyArrayTypeMatchCompiledSeparately() {
    runTest("fuzzy array type matching, aspect compiled separately from target class");
  }

  public void test_GitHub_214() {
    runTest("ArrayIndexOutOfBoundsException with Xlint unorderedAdviceAtShadow=warning");
  }

  /**
   * Add correct annotations to multiple ITD methods with the same name and same number of arguments, i.e. copy the
   * annotations correctly from the aspect into the target class instead of falsely always copying the annotations (if
   * any) from the first ITD method found.
   * <p>
   * See <a href="https://github.com/eclipse-aspectj/aspectj/issues/246">GitHub issue 246</a>.
   */
  public void test_GitHub_246() {
    runTest("add correct annotations to multiple ITD methods with the same name and same number of arguments");
  }

  /**
   * Make sure to create one {@code ajc$inlineAccessMethod} for identically named (overloaded) private aspect methods.
   * <p>
   * See <a href="https://github.com/eclipse-aspectj/aspectj/issues/250">GitHub issue 250</a>.
   */
  public void test_GitHub_250() {
    runTest("correctly handle overloaded private methods in aspects");
  }

  /**
   * If one generic method overrides another one with a narrower return type, avoid matching bridge methods.
   * <p>
   * See <a href="https://github.com/spring-projects/spring-framework/issues/27761">Spring GitHub issue 27761</a>.
   * <p>
   * This test uses an ASM-modified class file reproducing the problem seen in Spring in plain AspectJ. Before the
   * bugfix, it fails with <b>"advice defined in RepositoryAspect has not been applied [Xlint:adviceDidNotMatch]".</b>
   */
  public void test_Spring_GitHub_27761() {
    runTest("do not match bridge methods");
  }

  /**
   * In 1.9.20, a regression bug occurred, matching negated types like '!void' and '!String' incorrectly.
   * <p>
   * See <a href="https://github.com/eclipse-aspectj/aspectj/issues/257">GitHub issue 257</a>.
   */
  public void test_GitHub_257() {
    runTest("handle negated type patterns correctly");
  }

  public static Test suite() {
    return XMLBasedAjcTestCase.loadSuite(Bugs1921Tests.class);
  }

  @Override
  protected java.net.URL getSpecFile() {
    return getClassResource("ajc1921.xml");
  }

}
