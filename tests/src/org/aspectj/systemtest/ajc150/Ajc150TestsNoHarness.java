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

import org.aspectj.tools.ajc.CompilationResult;


/**
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
}