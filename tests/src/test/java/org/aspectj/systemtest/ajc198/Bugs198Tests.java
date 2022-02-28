/*******************************************************************************
 * Copyright (c) 2021 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *******************************************************************************/
package org.aspectj.systemtest.ajc198;

import io.bmuskalla.system.properties.PropertyEnvironment;
import io.bmuskalla.system.properties.ScopedSystemProperties;
import junit.framework.Test;
import org.aspectj.testing.XMLBasedAjcTestCase;

/**
 * @author Alexander Kriegisch
 */
public class Bugs198Tests extends XMLBasedAjcTestCase {

  public void testGitHub_105() {
    runTest("ITD annotation with mandatory parameter via aspectpath");
  }

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

  public void testGitHub_125() {
    try (PropertyEnvironment env = ScopedSystemProperties.newPropertyEnvironment()) {
      env.setProperty("org.aspectj.weaver.openarchives", "20");
      runTest("compiler can re-open closed JARs");
    }
  }

  public void testAsyncProceedNestedAroundAdvice_gh128() {
    runTest("asynchronous proceed for nested around-advice chain");
  }

  public static Test suite() {
    return XMLBasedAjcTestCase.loadSuite(Bugs198Tests.class);
  }

  @Override
  protected java.net.URL getSpecFile() {
    return getClassResource("ajc198.xml");
  }

}
