/*******************************************************************************
 * Copyright (c) 2014 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andy Clement - initial API and implementation
 *******************************************************************************/
package org.aspectj.systemtest.apt;

import junit.framework.Test;
import org.aspectj.apache.bcel.classfile.Method;
import org.aspectj.testing.XMLBasedAjcTestCase;
import org.aspectj.util.LangUtil;

import java.io.File;

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
    runTest("disabled annotation processing");
  }

  public void testAptWithJavaFilesAsAspects() {
    runTest("annotation processing generating java files with aspects");
  }

  public static Test suite() {
    return XMLBasedAjcTestCase.loadSuite(AptTests.class);
  }

  @Override
  protected File getSpecFile() {
    return getClassResource("apt-spec.xml");
  }

}
