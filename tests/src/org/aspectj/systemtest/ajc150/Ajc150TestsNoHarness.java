/*******************************************************************************
 * Copyright (c) 2005 IBM 
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

import org.aspectj.apache.bcel.classfile.JavaClass;
import org.aspectj.apache.bcel.classfile.Method;
import org.aspectj.tools.ajc.CompilationResult;


/**
 * These are tests that run on Java 1.4 and use the new ajctestcase format.
 * If you have a test that *needs* to run on Java 1.5 then look in Ajc150TestsRequireJava15.java
 */
public class Ajc150TestsNoHarness extends TestUtils {

  protected void setUp() throws Exception {
	super.setUp();
	baseDir = new File("../tests/bugs150");
  }
  
  public void testIncorrectExceptionTableWhenBreakInMethod_pr78021() {
	CompilationResult cR=ajc(baseDir,new String[]{"PR78021.java"});
	if (verbose) { System.err.println(cR); System.err.println(cR.getStandardError());}
  	RunResult rR = run("PR78021");
  	if (verbose) {System.err.println(rR.getStdErr());}
  }
  
  
  public void testIncorrectExceptionTableWhenReturnInMethod_pr79554() {
	CompilationResult cR=ajc(baseDir,new String[]{"PR79554.java"});
	if (verbose) { System.err.println(cR); System.err.println(cR.getStandardError());}
  	RunResult rR = run("PR79554");
  	if (verbose) {System.err.println(rR.getStdErr());}
  }
  
  
  public void testMissingDebugInfoForGeneratedMethods_pr82570() throws ClassNotFoundException {
  	boolean f = false;
    CompilationResult cR = ajc(baseDir,new String[]{"PR82570_1.java"});
    System.err.println(cR.getStandardError());
    assertTrue("Expected no compile problem:"+cR,!cR.hasErrorMessages());
    JavaClass jc = getClassFrom(ajc.getSandboxDirectory(),"PR82570_1");
    Method[] meths = jc.getMethods();
    for (int i = 0; i < meths.length; i++) {
		Method method = meths[i];
		if (f) System.err.println("Line number table for "+method.getName()+method.getSignature()+" = "+method.getLineNumberTable());
		assertTrue("Didn't find a line number table for method "+method.getName()+method.getSignature(),
				method.getLineNumberTable()!=null);
	}

    // This test would determine the info isn't there if you pass -g:none ...
//    cR = ajc(baseDir,new String[]{"PR82570_1.java","-g:none"});
//    assertTrue("Expected no compile problem:"+cR,!cR.hasErrorMessages());
//    System.err.println(cR.getStandardError());
//    jc = getClassFrom(ajc.getSandboxDirectory(),"PR82570_1");
//    meths = jc.getMethods();
//    for (int i = 0; i < meths.length; i++) {
//		Method method = meths[i];
//		assertTrue("Found a line number table for method "+method.getName(),
//				method.getLineNumberTable()==null);
//	}
  }
  
  public void testCanOverrideProtectedMethodsViaITDandDecp_pr83303() {
  	CompilationResult cR = ajc(baseDir,new String[]{"PR83303.java"});
  	assertTrue("Should be no errors:"+cR,!cR.hasErrorMessages());
  }
}