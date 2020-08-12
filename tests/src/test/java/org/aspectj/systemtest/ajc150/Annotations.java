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

import org.aspectj.apache.bcel.classfile.JavaClass;
import org.aspectj.apache.bcel.classfile.Method;
import org.aspectj.testing.XMLBasedAjcTestCase;

import junit.framework.Test;

public class Annotations extends XMLBasedAjcTestCase {

	  public static Test suite() {
	    return XMLBasedAjcTestCase.loadSuite(Annotations.class);
	  }

	  protected java.net.URL getSpecFile() {
		    return getClassResource("ajc150.xml");
	  }
	  
  public void testCompilingAnnotation() {
  	runTest("compiling an annotation");
  }
  
  public void testCompilingAnnotatedFile() {
  	runTest("compiling annotated file");
  }
  
  public void testCompilingUsingWithinAndAnnotationTypePattern() {
  	runTest("annotations and within (src)");
  }
  
  /**
   * We had a bug where annotations were not present in the output class file for methods
   * that got woven.  This was due to unpacking bugs in LazyMethodGen.  This test compiles
   * a simple program then checks the annotations were copied across.
   */
  public void testBugWithAnnotationsLostOnWovenMethods() throws ClassNotFoundException {
  	runTest("losing annotations...");
  	if (getCurrentTest().canRunOnThisVM()) {
	  	
	  	JavaClass jc = getClassFrom(ajc.getSandboxDirectory(),"Program");
	    Method[] meths = jc.getMethods();
		for (Method method : meths) {
			if (method.getName().equals("m1")) {
				assertTrue("Didn't have annotations - were they lost? method=" + method.getName(), method.getAnnotations().length == 1);
			}
		}
  	}
  }
  
  public void testAnnotatedAnnotations() {
  	runTest("annotated annotations (@Target)");
  }
  
  public void testSimpleAnnotatedAspectMembers() {
  	runTest("simple annotated aspect members");
  }
  
  public void testAnnotatedAspectMembersWithWrongAnnotationType() {
  	runTest("simple annotated aspect members with bad target");
  }
  
  // more implementation work needed before this test passes
  public void testAnnotatedITDs() {
  	runTest("annotated itds");
  }
  
  public void testAnnotatedITDs2() {
    runTest("annotated public itds");
  }

  public void testAnnotatedITDs3() {
	runTest("annotated public itds - values");
  }

  public void testAnnotatedITDs4() {
	runTest("annotated public itds - multiple complex annotations");
  }
  
  public void testAnnotatedITDsWithWrongAnnotationType() {
  	runTest("annotated itds with bad target");
  }
  
  public void testAnnotatedAdvice() {
  	runTest("annotated advice");
  }
  
  public void testAnnotatedAdviceWithWrongAnnotationType() {
  	runTest("annotated advice with bad target");
  }
  
  public void testAnnotatedPointcut() {
  	runTest("annotated pointcut");
  }
  
  // FIXME asc uncomment this test when parser is opened up
//  public void testAnnotatedDeclareStatements() {
//  	runTest("annotated declare statements");
//  }
  
  public void testBasicDeclareAnnotation() {
  	runTest("basic declare annotation parse test");
  }
  
  public void testAJDKAnnotatingAspects() {
  	runTest("ajdk: annotating aspects chapter");
  }
  
  public void testAJDKAnnotatingAspects2() {
  	runTest("ajdk: annotating aspects chapter, ex 2");
  }
  
  public void testAnnotationPatterns() {
  	runTest("ajdk: annotation pattern matching");
  }
  
  public void testAnnotationTypePatterns() {
  	runTest("ajdk: annotation type pattern matching");
  }
  
  public void testAnnotationSigPatterns() {
  	runTest("ajdk: annotations in sig patterns");
  }
  
  public void testAnnotationRuntimeMatching() {
  	runTest("ajdk: runtime annotations");
  }
  
  public void testAnnotationRetentionChecking() {
  	runTest("ajdk: @retention checking");
  }
  
  public void testAnnotationInheritance() {
  	runTest("ajdk: @inherited");
  }
  
  public void testAnnotationDEOW() {
	  runTest("ajdk: deow-ann");
  }
  
  public void testAnnotationDecp() {
	  runTest("ajdk: decp-ann");
  }
  
  public void testAnnotationDecPrecedence() {
	  runTest("ajdk: dec precedence");
  }
  
  public void testAnnotationDecAnnotation() {
	  runTest("ajdk: dec annotation");
  }
  
  public void testAnnotationsAndITDs() {
	  runTest("nasty annotation and itds test");
  }
  
  // helper methods.....

}