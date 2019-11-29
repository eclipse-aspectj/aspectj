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

import org.aspectj.testing.XMLBasedAjcTestCase;

import junit.framework.Test;


/**
 * Annotations, the rules/tests:
 * 
 * 1. cannot make ITD (C,M or F) on an annotation
 * 2. cannot use declare parents to change the super type of an annotation
 * 3. cannot use decp to make an annotation type implement an interface
 * 4. cannot use decp to dec java.lang.annotation.Annotation as the parent of any type
 * 5. cannot extend set of values in an annotation via an ITD like construct
 * 6. Compilation error if you explicitly identify an Annotation type.  
 * 7. Lint warning if a non-explicit type pattern would match an annotation type.
 */
public class AnnotationsBinaryWeaving extends XMLBasedAjcTestCase {

	  public static Test suite() {
	    return XMLBasedAjcTestCase.loadSuite(AnnotationsBinaryWeaving.class);
	  }

	  protected java.net.URL getSpecFile() {
	    return getClassResource("ajc150.xml");
	  }
	
  // Cannot make ITD (c/m/f) on an annotation
  public void test001_itdsOnAnnotationsNotAllowed() {
  	runTest("no itds on annotation types");
  }
  
  // Deals with the cases where an explicit type is specified and it is an annotation type
  public void test002_decpOnAnnotationNotAllowed_errors() {
  	runTest("no declare parents on annotation types");
  }
  
  //Deals with the cases where an wild type pattern is specified and it hits an annotation type
  public void test004_decpOnAnnotationNotAllowed_xlints() {
  	runTest("declare parents wildcards matching annotation types");
  }

}