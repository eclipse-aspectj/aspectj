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

import org.aspectj.bridge.IMessage;
import org.aspectj.tools.ajc.CompilationResult;


/**
 * Varargs, the rules/tests:
 * 
 * 1. cannot match on a varargs method by using 'Object[]' in your signature, 
 *    this affects call/execution/initialization/withincode
 */
public class VarargsTests extends TestUtils {
	
  protected void setUp() throws Exception {
	super.setUp();
	baseDir = new File("../tests/java5/varargs");
  }

  // check when signature is from a call PCD
  //   should get message: 
  // "an array type as the last parameter in a signature does not match on the varargs declared method: <blah>"
  public void test001_cantMatchVarargsWithObjectArray_callPCD() {
  	CompilationResult cR = binaryWeave("testcode.jar","VarargsAspect01.aj",0,3,true);
  	assertTrue("Did not get expected message about a varargs mismatch, instead got: "+cR.getWarningMessages(),
  			((IMessage)cR.getWarningMessages().get(0)).toString().indexOf("varargs declared method")!=-1);
  	verifyWeavingMessagesOutput(cR,new String[]{});
  }

  // check when signature is from an execution PCD
  public void test002_cantMatchVarargsWithObjectArray_execPCD() {
  	CompilationResult cR = binaryWeave("testcode.jar","VarargsAspect02.aj",0,1,true);
  	assertTrue("Did not get expected message about a varargs mismatch, instead got: "+cR.getWarningMessages(),
  			((IMessage)cR.getWarningMessages().get(0)).toString().indexOf("varargs declared method")!=-1);
  	verifyWeavingMessagesOutput(cR,new String[]{});
  }

  // check when signature is from an initialization PCD
  public void test003_cantMatchVarargsWithObjectArray_initPCD() {
  	CompilationResult cR = binaryWeave("testcode.jar","VarargsAspect03.aj",0,1,true);
  	assertTrue("Did not get expected message about a varags mismatch, instead got: "+cR.getWarningMessages(),
  			((IMessage)cR.getWarningMessages().get(0)).toString().indexOf("varargs declared method")!=-1);
  	verifyWeavingMessagesOutput(cR,new String[]{});
  }

  // check when signature is from an withincode PCD
  // In this test, it can be tricky to understand the results!!  The reason being that the shadow 
  // isn't included in the error message (it really should be, but thats a bit hard to do cleanly)
  public void test003_cantMatchVarargsWithObjectArray_withincodePCD() {
  	CompilationResult cR = binaryWeave("testcode.jar","VarargsAspect04.aj",0,6,true);
  	
  	// There are 7.  Each piece of the pointcut is matched against all the shadows, so both
  	// the 'withincode' PCD and the 'call' PCD are matched against every join point.
  	// AMC - there are now SIX. We detect early that a call(* *(..)) pcd cannot match 
  	//       constructor call shadows and never do the match.
  	assertTrue("Did not get expected message about a varargs mismatch, instead got: "+cR.getWarningMessages(),
  			((IMessage)cR.getWarningMessages().get(0)).toString().indexOf("varargs declared method")!=-1);
  	  	
  	verifyWeavingMessagesOutput(cR,new String[]{});
  }
  

  // before(): call(* *(Integer...)) { }
  public void test_usingVarargsInPointcuts1() {
  	CompilationResult cR = binaryWeave("testcode.jar","VarargsAspect05.aj",0,0,true);
  	System.err.println(cR.getStandardError());
  	System.err.println(cR.getErrorMessages());
  	System.err.println(cR.getInfoMessages());
  	verifyWeavingMessagesOutput(cR,new String[]{
  		"weaveinfo Type 'SimpleVarargs' (SimpleVarargs.java:15) advised by before advice from 'VarargsAspect05' (VarargsAspect05.aj:3)",
  		"weaveinfo Type 'SimpleVarargs' (SimpleVarargs.java:16) advised by before advice from 'VarargsAspect05' (VarargsAspect05.aj:3)",
  		"weaveinfo Type 'SimpleVarargs' (SimpleVarargs.java:17) advised by before advice from 'VarargsAspect05' (VarargsAspect05.aj:3)"});
  }
  
  // before(): call(* *(int,Integer...)) { } - slightly more complex pcut
  public void test_usingVarargsInPointcuts2() {
  	CompilationResult cR = binaryWeave("testcode.jar","VarargsAspect06.aj",0,0,true);
  	System.err.println(cR.getStandardError());
  	System.err.println(cR.getErrorMessages());
  	System.err.println(cR.getInfoMessages());
  	 
  	verifyWeavingMessagesOutput(cR,new String[]{
  		"weaveinfo Type 'SimpleVarargs' (SimpleVarargs.java:20) advised by before advice from 'VarargsAspect06' (VarargsAspect06.aj:3)",
  		"weaveinfo Type 'SimpleVarargs' (SimpleVarargs.java:21) advised by before advice from 'VarargsAspect06' (VarargsAspect06.aj:3)",
  		"weaveinfo Type 'SimpleVarargs' (SimpleVarargs.java:22) advised by before advice from 'VarargsAspect06' (VarargsAspect06.aj:3)"});
 }
  
}