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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import junit.framework.Test;

import org.aspectj.apache.bcel.classfile.JavaClass;
import org.aspectj.apache.bcel.classfile.Method;
import org.aspectj.apache.bcel.util.ClassPath;
import org.aspectj.apache.bcel.util.SyntheticRepository;
import org.aspectj.asm.AsmManager;
import org.aspectj.testing.XMLBasedAjcTestCase;

/**
 * These are tests that will run on Java 1.4 and use the old harness format for test specification.
 */
public class Ajc150Tests extends org.aspectj.testing.XMLBasedAjcTestCase {
	  
  public static Test suite() {
    return XMLBasedAjcTestCase.loadSuite(Ajc150Tests.class);
  }

  protected File getSpecFile() {
    return new File("../tests/src/org/aspectj/systemtest/ajc150/ajc150.xml");
  }

  public void test_typeProcessingOrderWhenDeclareParents() {
	runTest("Order of types passed to compiler determines weaving behavior");
  }
  
  public void test_aroundMethod() {
  	runTest("method called around in class");
  }
 
  public void test_aroundMethodAspect() {
  	runTest("method called around in aspect");
  }
  
  public void test_ambiguousBindingsDetection() {
  	runTest("Various kinds of ambiguous bindings");
  }
  
  public void test_ambiguousArgsDetection() {
  	runTest("ambiguous args");
  }
  
  public void testIncorrectExceptionTableWhenBreakInMethod_pr78021() {
  	runTest("Injecting exception into while loop with break statement causes catch block to be ignored");
  }
  
  
  public void testIncorrectExceptionTableWhenReturnInMethod_pr79554() {
  	runTest("Return in try-block disables catch-block if final-block is present");
  }

  public void testMissingDebugInfoForGeneratedMethods_pr82570() throws ClassNotFoundException {
  	runTest("Weaved code does not include debug lines");
  	boolean f = false;
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
//    }
  }

  
  public void testCanOverrideProtectedMethodsViaITDandDecp_pr83303() {
  	runTest("compiler error when mixing inheritance, overriding and polymorphism");
  }
  
  public void testPerTypeWithinMissesNamedInnerTypes() {
  	runTest("pertypewithin() handing of inner classes (1)");
  }
  
  public void testPerTypeWithinMissesAnonymousInnerTypes() {
  	runTest("pertypewithin() handing of inner classes (2)");
  }

  public void testPerTypeWithinIncorrectlyMatchingInterfaces() {
  	runTest("pertypewithin({interface}) illegal field modifier");
  }
  
  public void test051_arrayCloningInJava5() {
    runTest("AJC possible bug with static nested classes");
  }
 
  public void testBadASMforEnums() throws IOException {
  	runTest("bad asm for enums");
  	
  	if (System.getProperty("java.vm.version").startsWith("1.5")) {
	  	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	  	PrintWriter pw = new PrintWriter(baos);
	  	AsmManager.dumptree(pw,AsmManager.getDefault().getHierarchy().getRoot(),0);
	  	pw.flush();
	  	String tree = baos.toString();
	  	assertTrue("Expected 'Red [enumvalue]' somewhere in here:"+tree,tree.indexOf("Red  [enumvalue]")!=-1);
  	}
  }
  
  public void npeOnTypeNotFound() {
	  runTest("structure model npe on type not found");
  }
 
  public void testNoRuntimeExceptionSoftening() {
	  runTest("declare soft of runtime exception");
  }
  
  public void testRuntimeNoSoftenWithHandler() {
	  runTest("declare soft w. catch block");
  }
  
  public void testSyntaxError() {
	  runTest("invalid cons syntax");
  }
  
  public void testVarargsInConsBug() {
	  runTest("varargs in constructor sig");
  }
  
  // helper methods.....
  
  public SyntheticRepository createRepos(File cpentry) {
	ClassPath cp = new ClassPath(cpentry+File.pathSeparator+System.getProperty("java.class.path"));
	return SyntheticRepository.getInstance(cp);
  }
  
  protected JavaClass getClassFrom(File where,String clazzname) throws ClassNotFoundException {
	SyntheticRepository repos = createRepos(where);
	return repos.loadClass(clazzname);
  }

}