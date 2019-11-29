/*******************************************************************************
 * Copyright (c) 2004 IBM 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andy Clement - initial API and implementation
 *******************************************************************************/
package org.aspectj.systemtest.ajc150;

import java.net.URL;

import org.aspectj.testing.XMLBasedAjcTestCase;

import junit.framework.Test;


/**
 * Tests the use of Annotations in pointcuts
 */
public class AnnotationPointcutsTests extends XMLBasedAjcTestCase {

	  public static Test suite() {
	    return XMLBasedAjcTestCase.loadSuite(AnnotationPointcutsTests.class);
	  }

	  protected URL getSpecFile() {
	    return getClassResource("ajc150.xml");
	  }
	  
  // before(): call(@SimpleAnnotation * *(..)) { }
  public void test001_usingAnnotationsInPointcuts() {
  	runTest("annotation matching on call");
  }

  public void test002_AtAnnotationMatching() {
  	runTest("at annotation matching");
  }

  public void test003_Within_Code() {
  	runTest("annotations and within(code)");
  }

  public void test004_Within() {
  	runTest("annotations and within");
  }

  // TODO extra tests
  // 3) @annotation on the different join point kinds, matches with inherited annotation
  
}