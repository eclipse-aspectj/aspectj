/*******************************************************************************
 * Copyright (c) 2014,2018 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andy Clement - initial API and implementation
 *******************************************************************************/
package org.aspectj.systemtest.apt;

import org.aspectj.testing.XMLBasedAjcTestCase;
import org.aspectj.util.LangUtil;

import junit.framework.Test;

/**
 * Annotation processing tool tests.
 *
 * @author Sergey Stupin.
 */
public class AptTests extends XMLBasedAjcTestCase {

  public void testAptWithSpecifiedProcessor() {
	  if (LangUtil.is19VMOrGreater()) {
		  return;
	  }
    runTest("annotation processing with specified processor");
  }

  /**
   * SPI - http://docs.oracle.com/javase/tutorial/sound/SPI-intro.html
   */
  public void testAptUsingSPI() {
	  if (LangUtil.is19VMOrGreater()) {
		  return;
	  }
    runTest("annotation processing in action using SPI");
  }

  public void testDisabledApt() {
	  if (LangUtil.is11VMOrGreater()) {
		  // javax.annotation.Generated not in Java11
		  return;
	  }
    runTest("disabled annotation processing");
  }

  public void testAptWithJavaFilesAsAspects() {
    runTest("annotation processing generating java files with aspects");
  }

  public static Test suite() {
    return XMLBasedAjcTestCase.loadSuite(AptTests.class);
  }

  @Override
  protected java.net.URL getSpecFile() {
    return getClassResource("apt-spec.xml");
  }

}
