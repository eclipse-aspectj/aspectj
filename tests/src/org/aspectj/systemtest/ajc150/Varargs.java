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
public class Varargs extends TestUtils {
	
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
  public void test003_cantMatchVarargsWithObjectArray_withincodePCD() {
  	CompilationResult cR = binaryWeave("testcode.jar","VarargsAspect04.aj",0,1,true);
  	assertTrue("Did not get expected message about a varags mismatch, instead got: "+cR.getWarningMessages(),
  			((IMessage)cR.getWarningMessages().get(0)).toString().indexOf("varargs declared method")!=-1);
  	verifyWeavingMessagesOutput(cR,new String[]{});
  }
  
}