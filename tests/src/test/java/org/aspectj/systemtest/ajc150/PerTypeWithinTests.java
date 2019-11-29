/*******************************************************************************
 * Copyright (c) 2005 IBM 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andy Clement - initial API and implementation
 *******************************************************************************/
package org.aspectj.systemtest.ajc150;

import java.io.File;

import org.aspectj.testing.XMLBasedAjcTestCase;
import org.aspectj.tools.ajc.CompilationResult;

import junit.framework.Test;



public class PerTypeWithinTests extends XMLBasedAjcTestCase {

	  public static Test suite() {
	    return XMLBasedAjcTestCase.loadSuite(PerTypeWithinTests.class);
	  }

	  protected java.net.URL getSpecFile() {
	    return getClassResource("ajc150.xml");
	  }
  
  /**
   * First few tests:
   * 
   * Five types p.A, p.B, p.C, q.D, q.E and an aspect a.X.
   * 
   * The aspect is pertypewithin(p..*) - this should match A,B,C but not D,E.
   * 
   * Aspect instances should be accessible for A,B,C but not D,E.
   * The aspect instances for A,B,C should be different.
   * 
   * hasAspect(), aspectOf() should work.
   * 
   * We test these assumptions in A,B,C,D,E. 
   */
  public void testDoesItWorkAtAll() {
  	runTest("basic ptw test");
  }
  
  public void testCheckHasAspectWorks() {
  	runTest("ptw hasAspect");
  }

  public void testCheckAspectOfWorks() {
  	runTest("ptw aspectOf");
  }
  /**
   * Aspects Q and R match P with a pertypewithin() - they shouldn't clash in any way
   *
   */
  public void testTwoAspectsHittingOneType() {
  	runTest("ptw multi-aspects");
  }

  /**
   * Checks the use of pertypewithin() doesn't result in extra join points (i.e. the
   * infrastructure is properly hidden in ajc$ or synthetic members)
   */
  public void testPervasivenessOfWeaving() {
  	CompilationResult cR = ajc(new File("../tests/java5/pertypewithin"),new String[]{"U.java","-showWeaveInfo"});
  	int weavingMessagesFromNormalDeploymentModel = cR.getWeaveMessages().size();

    cR = ajc(new File("../tests/java5/pertypewithin"),new String[]{"V.java","-showWeaveInfo"});
  	int weavingMessagesFromPerTypeWithin = cR.getWeaveMessages().size();

  	assertEquals("Expected same number of messages regardless of perclause",
  			weavingMessagesFromNormalDeploymentModel,weavingMessagesFromPerTypeWithin);
  }
  
  
  public void testBinaryWeaving_ClassesAreBinary() {
  	runTest("ptw binary");
  }

    public void testBinaryWeaving_AspectsAreBinary() {
    	runTest("ptw binary aspect");
    }
	
  public void testAJDKExamples() {
	  runTest("ajdk: ptw");
  }
//  	// Compile the aspect H.java into classes3
//  	CompilationResult cR = ajc(new File("../tests/java5/pertypewithin"),new String[]{"H.java","-outjar","aspects.jar"});
//  	setShouldEmptySandbox(false);
//  	// Compile the class with H.class as aspectpath, should be binary woven correctly
//  	cR = ajc(new File("../tests/java5/pertypewithin"),new String[]{"G.java","-aspectpath","aspects.jar"});
//  	RunResult rR = run("G");
//  	assertTrue("Expected aspect related message 'advice running' in output from G",
//  			rR.getStdErr().indexOf("advice running")!=-1);
//  	setShouldEmptySandbox(true);
//  }
//  
//  // binary weaving case ...
}