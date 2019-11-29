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
 * Checks if we are obeying migration rules. 
 */
public class MigrationTests extends XMLBasedAjcTestCase {

	  public static Test suite() {
	    return XMLBasedAjcTestCase.loadSuite(MigrationTests.class);
	  }

	  protected java.net.URL getSpecFile() {
	    return getClassResource("ajc150.xml");
	  }
  /**
   * Compile a simple java class with an aspect library built with aspectj 1.2.1 - this
   * checks that we can load in attributes (especially pointcuts) that were written out
   * in the 'old way'
   *
   */
  public void testMigrationFrom121_pointcutsAndAdvice() {
  	runTest("load aspectj 1.2.1 aspects in aspectj 5");
//  	CompilationResult cR = ajc(baseDir,new String[]{"-aspectpath","aspects121.jar","Program.java"});
//  	System.err.println(cR.getStandardError());
//  	assertTrue("Should not coredump: "+cR.getStandardError(),cR.getStandardError().indexOf("Dumping to ajcore")==-1);
//    assertTrue("Should be no error messages: \n"+cR.getErrorMessages(),cR.getErrorMessages().size()==0);
//    File f = new File(ajc.getSandboxDirectory()+File.separator+"Program.class");
//    assertTrue("Missing class file",f.exists());
//  	run("Program");
  }
  
//  /**
//   * We cannot support all aspects built prior to AspectJ 1.2.1 - so we don't support any.
//   * There are probably many reasons but the first one I've hit is:
//   * - Changes for cflow optimizations (counters instead of stacks where we can) mean that an aspect
//   *   compiled at AspectJ1.2.0 will contain stack cases but AspectJ1.5.0 will look for counter
//   *   fields in some cases.
//   * 
//   * This means we should get a reasonable failure message in this case.
//   */
//  public void testMigrationFrom120_pointcutsAndAdvice() {
//  	CompilationResult cR = ajc(baseDir,new String[]{"-aspectpath","aspects120.jar","Program.java"});
//  	assertTrue("Should have failed",cR.getFailMessages().size()>0);
//  	assertTrue("Should have produced nice message",cR.getFailMessages().get(0).toString().indexOf("Unable to continue")!=-1);
//  }
 
}