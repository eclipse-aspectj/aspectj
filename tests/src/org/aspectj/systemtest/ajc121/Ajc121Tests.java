/* *******************************************************************
 * Copyright (c) 2004 IBM Corporation
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Common Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/cpl-v10.html 
 * 
 * ******************************************************************/
package org.aspectj.systemtest.ajc121;

import java.io.File;

import junit.framework.Test;

import org.aspectj.testing.XMLBasedAjcTestCase;

public class Ajc121Tests extends org.aspectj.testing.XMLBasedAjcTestCase {

  public static Test suite() {
    return XMLBasedAjcTestCase.loadSuite(Ajc121Tests.class);
  }

  protected File getSpecFile() {
    return new File("../tests/src/org/aspectj/systemtest/ajc121/ajc121.xml");
  }


  public void test001(){
    runTest("false ambiguous binding error (introduced in 1.2rc2)");
  }

  public void test002(){
    runTest("An if() pointcut inside a perthis() clauses an ABORT - null pointer exception in ajc");
  }

  public void test003(){
    runTest("An if() pointcut inside a perthis() clauses an ABORT - null pointer exception in ajc");
  }

  public void test004(){
    runTest("An if() pointcut inside a perthis() clauses an ABORT - null pointer exception in ajc");
  }

  public void test005(){
    runTest("compiler aborts with 'conflicting dominates orders' with circular declare precedences");
  }

  public void test006(){
    runTest("'can't bind type' message has $ for . in type name for declare soft");
  }

  public void test007(){
    runTest("Hiding of Instance Methods by static methods");
  }

  public void test008(){
    runTest("if(false) optimisation");
  }

  public void test009(){
    runTest("if(true) optimisation");
  }

  public void test010(){
    runTest("java.lang.NullPointerException in WeaverMessageHandler class");
  }

  public void test011(){
    runTest("ClassCastException at BcelRenderer.java:169");
  }

  public void test012(){
    runTest("Front-end bug, shouldn't allow patterns of the form foo.., should be foo..*");
  }

  public void test013() {
  	runTest("Nullpointer-Exception when defining a withincode() pointcut");
  }

  public void test014() {
  	runTest("NPE, Incorrect XLint:unmatchedSuperTypeInCall warning");
  }
  
  public void test015_invalidXlint() { // keywords: "laurie hendren"
  	runTest("invalid warning indicating no match when a match really occurs");
  }
  
  public void test016_ByteConversionInstructions() {
  	runTest("RuntimeException thrown: Could not find instruction: org.apache.bcel.generic.B2I");
  	String output = getLastRunResult().getStdErr();
  	assertTrue("Expected to find [b2] in this output but didn't:"+output,output.indexOf("[b2]")!=-1);
  	assertTrue("Expected to find [b127] in this output but didn't:"+output,output.indexOf("[b127]")!=-1);
  	assertTrue("Expected to find [b0] in this output but didn't:"+output,output.indexOf("[b0]")!=-1);

  	assertTrue("Expected to find [c65] in this output but didn't:"+output,output.indexOf("[c65]")!=-1);
  	assertTrue("Expected to find [c66] in this output but didn't:"+output,output.indexOf("[c66]")!=-1);
  	assertTrue("Expected to find [c67] in this output but didn't:"+output,output.indexOf("[c67]")!=-1);

  	assertTrue("Expected to find [s1] in this output but didn't:"+output,output.indexOf("[s1]")!=-1);
  	assertTrue("Expected to find [s32767] in this output but didn't:"+output,output.indexOf("[s32767]")!=-1);
  	assertTrue("Expected to find [b0] in this output but didn't:"+output,output.indexOf("[b0]")!=-1);
  }
  
  public void test017_PrivateMethodCallsInAroundAdvice() {
    runTest("Cannot advise private method call in around advice");
    System.err.println(getLastRunResult().getStdErr());
  }
  
  public void test018_PrivateFieldSetsInAroundAdvice() {
    runTest("Cannot advise private field sets in around advice");
    System.err.println(getLastRunResult().getStdErr());
  }
  
  public void test019_PrivateFieldGetsInAroundAdvice() {
    runTest("Cannot advise private field gets in around advice");
    System.err.println(getLastRunResult().getStdErr());
  }
  
// test takes over 5 minutes to run, so not included in normal suite run  
//  public void test020_branchTargetOffsetTooLargeForShort() {
//      runTest("Branch target offset too large for short");
//  }
  
//  public void test021_BcelFailureWithVeryLargeClasses() {
//      runTest("Weaver fails in BCEL for large classes");
//  }
//  
//  public void test022_BcelFailureWithVeryLargeClasses_Binary() {
//      runTest("Weaver fails in BCEL for large classes (binary)");
//  }
  
  public void test023_proceedInAround1() {
      runTest("proceed used as method name in around advice (1)");
  }

  public void test024_proceedInAround2() {
      runTest("proceed used as method name in around advice (2)");
  }

  public void test025_proceedInAround3() {
      runTest("proceed used as method name in around advice (3)");
  }
  
  public void test026_bindingThisAndTargetToTheSameFormal() {
  	runTest("ajc crashes when compiling the following program (binding this() and target())");
  }
  
  public void test027_itdsOnInnerClassesAsStatic() {
      runTest("ITDs on inner classes should be static context");
  }
  
//  public void test028_itdsAndInitializers() {
//    runTest("resolution of IT field inits");
//  }

  public void test029_falseInvalidAbsoluteTypeName() {
    runTest("Valid but inaccessible type names should not be flagged by XLint:invalidAbsoluteTypeName");
  }
}

