/*******************************************************************************
 * Copyright (c) 2025 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *******************************************************************************/
package org.aspectj.systemtest.ajc1923;

import junit.framework.Test;
import org.aspectj.testing.XMLBasedAjcTestCase;

/**
 * @author Andy Clement
 */
public class Bugs1923Tests extends XMLBasedAjcTestCase {

  public static Test suite() {
    return XMLBasedAjcTestCase.loadSuite(Bugs1923Tests.class);
  }
  
  public void testGh328_AroundAdviceInlineAccessor() {
	  runTest("problem with inline accessor generator for around advice");
  }

  public void testGh327_IntertypeFields_Static() {
	  runTest("problem with intertype field declaration code generation - static");
  }

  public void testGh327_IntertypeFields_NonStatic() {
	  runTest("problem with intertype field declaration code generation - nonstatic");
  }
  
  public void testGh327_IntertypeFields_Private() {
	  runTest("problem with intertype field declaration code generation - private");
  }

  public void testGh327_IntertypeMethods() {
	  runTest("problem with intertype method declaration code generation");
  }
  
  public void testGh326_ClassCastExceptionHandling() {
	 runTest("classcast on exception handling aspect");
  }
  
  public void testGh322_DeprecatedAnnotation() {
	  runTest("ajc error for particular Pointcut and @Deprecated");
  }

  @Override
  protected java.net.URL getSpecFile() {
    return getClassResource("ajc1923.xml");
  }

}
