/*******************************************************************************
 * Copyright (c) 2004 IBM 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *    Andy Clement - initial API and implementation
 *******************************************************************************/
package org.aspectj.systemtest.ajc150;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.aspectj.tools.ajc.CompilationResult;


/**
 * Tests the use of Annotations in pointcuts
 */
public class AnnotationPointcutsTests extends TestUtils {
	
  protected void setUp() throws Exception {
	super.setUp();
	baseDir = new File("../tests/java5/annotations");
  }
	
  // before(): call(@SimpleAnnotation * *(..)) { }
  public void test001_usingAnnotationsInPointcuts() {
  	CompilationResult cR = binaryWeave("testcode.jar","AnnotationAspect02.aj",0,0);
  	System.err.println(cR.getStandardError());
  	System.err.println(cR.getErrorMessages());
  	System.err.println(cR.getInfoMessages());
  	verifyWeavingMessagesOutput(cR,new String[]{
  			"weaveinfo Type 'AnnotatedType' (AnnotatedType.java:3) advised by before advice from 'AnnotationAspect02' (AnnotationAspect02.aj:4)",
			"weaveinfo Type 'AnnotatedType' (AnnotatedType.java:3) advised by before advice from 'AnnotationAspect02' (AnnotationAspect02.aj:2)",
  			"weaveinfo Type 'AnnotatedType' (AnnotatedType.java:4) advised by before advice from 'AnnotationAspect02' (AnnotationAspect02.aj:4)"});
  }
  
  public void test002_AtAnnotationMatching() {
      CompilationResult cR = binaryWeave("testcode.jar","AnnotationAspect03.aj",0,1);
      List expectedWarnings = new ArrayList();
      expectedWarnings.add(new Message("@annotation matched here"));  // L 8
      assertMessages(cR, new MessageSpec(expectedWarnings, new ArrayList()));
  }
  
  
  // TODO extra tests
  // 1) @within  (matches, does not match, matches inherited annotation)
  // 2) @withincode (matches, does not match)
  // 3) @annotation on the different join point kinds, matches with inherited annotation
  
}