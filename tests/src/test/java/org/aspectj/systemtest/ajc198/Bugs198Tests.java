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

  public static Test suite() {
    return XMLBasedAjcTestCase.loadSuite(Bugs198Tests.class);
  }

  @Override
  protected java.net.URL getSpecFile() {
    return getClassResource("ajc198.xml");
  }

}
