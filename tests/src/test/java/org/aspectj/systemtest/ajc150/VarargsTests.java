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
 * Varargs, the rules/tests:
 * 
 * 1. cannot match on a varargs method by using 'Object[]' in your signature, 
 *    this affects call/execution/initialization/withincode
 */
public class VarargsTests extends XMLBasedAjcTestCase {

	  public static Test suite() {
	    return XMLBasedAjcTestCase.loadSuite(VarargsTests.class);
	  }

	  protected java.net.URL getSpecFile() {
	    return getClassResource("ajc150.xml");
	  }
	  
  // check when signature is from a call PCD
  //   should get message: 
  // "an array type as the last parameter in a signature does not match on the varargs declared method: <blah>"
  public void test001_cantMatchVarargsWithObjectArray_callPCD() {
  	runTest("varargs not matched by Object[] (call)");
  }

  // check when signature is from an execution PCD
  public void test002_cantMatchVarargsWithObjectArray_execPCD() {
  	runTest("varargs not matched by Object[] (exe)");
  }

  // check when signature is from an initialization PCD
  public void test003_cantMatchVarargsWithObjectArray_initPCD() {
  	runTest("varargs not matched by Object[] (init)");  	
  }

  // check when signature is from an withincode PCD
  public void test003_cantMatchVarargsWithObjectArray_withincodePCD() {
  	runTest("varargs not matched by Object[] (withincode)");
  }

  // before(): call(* *(Integer...)) { }
  public void test_usingVarargsInPointcuts1() {
  	runTest("call with varargs signature");
  }

  // before(): call(* *(int,Integer...)) { } - slightly more complex pcut
  public void test_usingVarargsInPointcuts2() {
  	runTest("call with varargs multi-signature");
  }
  
  public void testAJDKExamples() {
	  runTest("ajdk: varargs");
  }
  
  public void testStarVarargs() {
	  runTest("star varargs pattern");
  }
  
  public void testVarargsWithDotDotInPointcut() {
	  runTest("Varargs with .. in pointcut");
  }

}