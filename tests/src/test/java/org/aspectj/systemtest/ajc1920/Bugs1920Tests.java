/*******************************************************************************
 * Copyright (c) 2022 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *******************************************************************************/
package org.aspectj.systemtest.ajc1920;

import junit.framework.Test;
import org.aspectj.testing.XMLBasedAjcTestCase;

/**
 * @author Alexander Kriegisch
 */
public class Bugs1920Tests extends XMLBasedAjcTestCase {

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

  public static Test suite() {
    return XMLBasedAjcTestCase.loadSuite(Bugs1920Tests.class);
  }

  @Override
  protected java.net.URL getSpecFile() {
    return getClassResource("ajc1920.xml");
  }

}
