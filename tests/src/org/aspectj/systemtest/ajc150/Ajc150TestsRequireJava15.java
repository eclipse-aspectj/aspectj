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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import org.aspectj.asm.AsmManager;
import org.aspectj.tools.ajc.CompilationResult;


/**
 * These tests only execute in a 1.5 environment.
 */
public class Ajc150TestsRequireJava15 extends TestUtils {

  protected void setUp() throws Exception {
	super.setUp();
	baseDir = new File("../tests/bugs150");
  }
  
  public void testBadASMforEnums() throws IOException {
  	CompilationResult cR = ajc(baseDir,new String[]{"Rainbow.java","-emacssym","-1.5"});
  	// System.err.println(cR);
  	
  	ByteArrayOutputStream baos = new ByteArrayOutputStream();
  	PrintWriter pw = new PrintWriter(baos);
  	AsmManager.dumptree(pw,AsmManager.getDefault().getHierarchy().getRoot(),0);
  	pw.flush();
  	String tree = baos.toString();
  	assertTrue("Expected 'Red [enumvalue]' somewhere in here:"+tree,tree.indexOf("Red  [enumvalue]")!=-1);
  }
  
}