/*******************************************************************************
 * Copyright (c) 2021 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *******************************************************************************/
package org.aspectj.systemtest.ajc199;

import io.bmuskalla.system.properties.PropertyEnvironment;
import io.bmuskalla.system.properties.ScopedSystemProperties;
import junit.framework.Test;
import org.aspectj.testing.XMLBasedAjcTestCase;

/**
 * @author Alexander Kriegisch
 */
public class Bugs199Tests extends XMLBasedAjcTestCase {

  public void testAnnotationStyleSpecialIfClauses() {
    runTest("annotation style A");
  }

  public void testAnnotationStylePointcutInheritanceWithIfClauses() {
    runTest("annotation style B");
  }

  public void testAnnotationStyleSpecialIfClauses2_gh120() {
    runTest("annotation style C");
  }

  public void testAnnotationStyleSpecialIfClauses3_gh120() {
    runTest("annotation style D");
  }

  public void testAnnotationStyleNegatedIf_gh122() {
    runTest("annotation style negated if");
  }

  public void testCompilerCanReopenClosedJARs_gh125() {
    try (PropertyEnvironment env = ScopedSystemProperties.newPropertyEnvironment()) {
      env.setProperty("org.aspectj.weaver.openarchives", "20");
      runTest("compiler can re-open closed JARs");
    }
  }

  public void testAsyncProceedNestedAroundAdvice_gh128() {
    runTest("asynchronous proceed for nested around-advice (@AspectJ)");
  }

  public void testAsyncProceedNestedAroundAdviceThreadPool_gh128() {
    // TODO: future improvement, see https://github.com/eclipse/org.aspectj/issues/141
    // runTest("asynchronous proceed for nested around-advice (@AspectJ, thread pool)");
  }

  public void testAsyncProceedNestedAroundAdviceNative_gh128() {
    runTest("asynchronous proceed for nested around-advice (native)");
  }

  public void testAsyncProceedNestedAroundAdviceNativeThreadPool_gh128() {
    runTest("asynchronous proceed for nested around-advice (native, thread pool)");
  }

  public void testAddExports_gh145() {
    runTest("use --add-exports");
  }

  public void testAddReads_gh145() {
    runTest("use --add-reads");
  }

  public void testAddModules_gh145() {
    runTest("use --add-modules");
  }

  public void testAddModulesJDK_gh145() {
    runTest("use --add-modules with non-public JDK module");
  }

  public static Test suite() {
    return XMLBasedAjcTestCase.loadSuite(Bugs199Tests.class);
  }

  @Override
  protected java.net.URL getSpecFile() {
    return getClassResource("ajc199.xml");
  }

}
