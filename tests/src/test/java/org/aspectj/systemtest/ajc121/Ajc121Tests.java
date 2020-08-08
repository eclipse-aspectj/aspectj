/* *******************************************************************
 * Copyright (c) 2004 IBM Corporation
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html 
 * 
 * ******************************************************************/
package org.aspectj.systemtest.ajc121;

import java.net.URL;

import org.aspectj.testing.XMLBasedAjcTestCase;

import junit.framework.Test;

public class Ajc121Tests extends org.aspectj.testing.XMLBasedAjcTestCase {

  public static Test suite() {
    return XMLBasedAjcTestCase.loadSuite(Ajc121Tests.class);
  }

  protected URL getSpecFile() {
    return getClassResource("ajc121.xml");
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
  	assertTrue("Expected to find [b2] in this output but didn't:"+output, output.contains("[b2]"));
  	assertTrue("Expected to find [b127] in this output but didn't:"+output, output.contains("[b127]"));
  	assertTrue("Expected to find [b0] in this output but didn't:"+output, output.contains("[b0]"));

  	assertTrue("Expected to find [c65] in this output but didn't:"+output, output.contains("[c65]"));
  	assertTrue("Expected to find [c66] in this output but didn't:"+output, output.contains("[c66]"));
  	assertTrue("Expected to find [c67] in this output but didn't:"+output, output.contains("[c67]"));

  	assertTrue("Expected to find [s1] in this output but didn't:"+output, output.contains("[s1]"));
  	assertTrue("Expected to find [s32767] in this output but didn't:"+output, output.contains("[s32767]"));
  	assertTrue("Expected to find [b0] in this output but didn't:"+output, output.contains("[b0]"));
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
  
  public void test028_itdsAndInitializers() {
    runTest("resolution of IT field inits");
  }

  public void test029_falseInvalidAbsoluteTypeName() {
    runTest("Valid but inaccessible type names should not be flagged by XLint:invalidAbsoluteTypeName");
  }
  
  public void test030_privateITDinitialisersBeingMatched() {
    runTest("intertype initialisers should match field set pointcuts");
  }

  public void test031_privateITDinitialisersBeingMatched_OxfordTest() {
    runTest("intertype initialisers should match field set pointcuts (oxford testcase)");
    //System.err.println(">"+getLastRunResult().getStdErr());
    String exp = ":set field set(int C.n):set field set(int C.m):get field get(int C.n):set field set(int C.n)";
    assertTrue("Expected output '"+exp+"' but got "+getLastRunResult().getStdErr(),
    		getLastRunResult().getStdErr().equals(exp));
  }
  
  public void test032_stringConcatForDEOW() {
    runTest("Compile time declarations (warning and error) do not accept string concatenation (with +)");
  }

  public void test033_stringConcatForDEOWErrorCase() {
    runTest("Compile time declarations (warning and error) do not accept string concatenation (with +) (2)");
  }

  public void test034_scopeForITDS_pr61768() {
      runTest("scope for inter-type methods");
  }
  
  public void test035_innerAspectCallsPrivateMethod_pr71372() {
    runTest("NoSuchMethodError calling private method from around advice in inner aspect");
    String s = getLastRunResult().getStdErr();
    assertTrue("Expected ':before:around' but got "+s,
    		   s.equals(":before:around"));
  }
  
  public void test036_innerAspectCallsPrivateMethod_pr71372_2() {
    runTest("NoSuchMethodError calling private method from around advice in inner aspect (2)");
    String s = getLastRunResult().getStdErr();
    assertTrue("Expected ':before:around' but got "+s,
    		   s.equals(":before:around"));
  }
  
  public void test037_innerAspectCallsPrivateMethod_pr71372_3() {
    runTest("NoSuchMethodError calling private method from around advice in inner aspect (3)");
    String s = getLastRunResult().getStdErr();
    assertTrue("Expected ':before:around' but got "+s,
    		   s.equals(":before:around"));
  }
  
  public void test038_innerAspectCallsPrivateMethod_pr71372_4() {
    runTest("NoSuchMethodError calling private method from around advice in inner aspect (4)");
    String s = getLastRunResult().getStdErr();
    assertTrue("Expected ':before:around' but got "+s,
    		   s.equals(":before:around"));
  }

  public void test039_privilegedAspectAccessingPrivateMethods_pr67579() {
  	runTest("NPE on privileged aspect error");
  }
  
  public void test040_privilegedAspectAccessingPrivateMethods_pr67579_2() {
  	runTest("NPE on privileged aspect error (2)");
  }
  
  public void test041_ITDaccessingPrivateMethod_pr67578() {
  	runTest("Privileged Aspect Access Problem Across Packages");
  }
  
  public void test042_ITDaccessingPrivateMethod_pr67578_2() {
  	runTest("Privileged Aspect Access Problem Across Packages (2)");
  }
  
  public void test043_pr62642_ExceptionInInitializerError() {
    runTest("proper handling of ExceptionInIntializer inside clinit in presence of after throwing advice");
    String s = getLastRunResult().getStdErr();
    assertTrue("Output should contain java.lang.ExceptionInInitializerError but is '"+s+"'",
			s.contains("java.lang.ExceptionInInitializerError"));
    // No getCause on 1.3 JVMs
//    assertTrue("Output should contain 'CAUSE=org.aspectj.lang.NoAspectBoundException' but is '"+s+"'",
//            s.indexOf("CAUSE=org.aspectj.lang.NoAspectBoundException")!=-1);
  }
  
  public void test044_ITDnameClashes() {
    	runTest("ITD name clashes with private members");
  }
  
  public void test045_ITDprotectedVisibility() {
  	runTest("Inconsistency in scoping of protected members in ITDs");
  }
  
  public void test045_wrongLineForExecutionJoinPoint() {
  	runTest("wrong line for method execution join point");
  }

  public void test046_interfaceITD_pr70794_1() {
    runTest("The introduction on interface causes the interface implementation class error (1)");
  }

  public void test047_interfaceITD_pr70794_2() {
    runTest("The introduction on interface causes the interface implementation class error (2)");
  }

  public void test048_interfaceITD_pr70794_3() {
    runTest("The introduction on interface causes the interface implementation class error (3)");
  }

  public void test049_interfaceITD_pr70794_4() {
    runTest("The introduction on interface causes the interface implementation class error (4)");
  }
  
  public void test050_typePatternMatchingWithArrays() {
  	runTest("declare warning warns at wrong points");
  }
  
  public void test052_bogusMessage1() {
    runTest("Bogus error message: The abstract method ajc$pointcut$$tracingScope$a2 in type Tracing can only be defined by an abstract class (1)");
  }
  
  public void test053_bogusMessage2() {
    runTest("Bogus error message: The abstract method ajc$pointcut$$tracingScope$a2 in type Tracing can only be defined by an abstract class (2)");
  }
  
  public void test054_cnfe() {
    runTest("passing null to array arguments confuzes static join point signature. (1)");
  }

  public void test055_cnfe() {
    runTest("passing null to array arguments confuzes static join point signature. (2)");
  }
  
  public void test056_arrayCloning() {
    runTest("around advice throws java.lang.VerifyError at runtime");
  }
   
  public void test057_decSoftWithSuper() {
      runTest("declare soft can cause programs with invalid exception behaviour to be generated");
  }

  public void test058_npeOnTJPerror() {
    runTest("NPE on thisJoinPoint mistake");
  }
  
  public void test059_cflowOptimization_counters() {
  	runTest("Optimization of cflow - counters (1)");
  }
  
  public void test060_cflowOptimization_counters() {
  	runTest("Optimization of cflow - shared counters (2)");
  }
  
  public void test061_cflowOptimization_counters() {
  	runTest("Optimization of cflow - shared stacks (3)");
  }
  
  public void test062_cflowOptimization_counters() {
  	runTest("Optimization of cflow - counters (4)");
  }
   
  public void test063_cflowOptimization_countersWithAbstractPcuts() {
  	runTest("Optimization of cflow - counters with abstract pointcuts (5)");
  }

  public void test064() {
  	runTest("Anonymous classes unaware of introductions into abstract classes");
  }
  
  private int countLines(String s) {
    int lines = 0;
    int idx = 0;
    while (s.indexOf("\n",idx)!=-1) {
    	lines++;
    	idx = s.indexOf("\n",idx)+1;
    }
    return lines;
  }
  
  public void test065() {
  	runTest("before,after not (cflow(within(Trace*))) prints nothing");
  	String s = getLastRunResult().getStdErr();
  	int lines = countLines(s);
	assertTrue("The actual output does not match the expected output.  Expected 102 lines but got "+
			   lines+" lines.  Actual output =\n"+s,lines==102);
	// IF YOU WANT TO SEE THE EXPECTED OUTPUT, LOOK IN THE TEST PROGRAM bugs/WhatsGoingOn.java
  }

}