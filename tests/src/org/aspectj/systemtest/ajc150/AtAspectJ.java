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
import org.aspectj.tools.ajc.CompilationResult;

public class AtAspectJ extends TestUtils {
	
  protected void setUp() throws Exception {
	super.setUp();
	baseDir = new File("../tests/java5/ataspectj");
  }
   
  // Before advice expressed in @AJ notation
  public void testSimpleBefore() {
  	CompilationResult cR = ajc(baseDir,new String[]{"SimpleBefore.java","-1.5","-showWeaveInfo"});
  	MessageSpec ms = new MessageSpec(null,null);
  	assertMessages(cR,ms);
  	RunResult rR = run("SimpleBefore");
  }
  
  // After advice expressed in @AJ notation
  public void testSimpleAfter() {
  	CompilationResult cR = ajc(baseDir,new String[]{"SimpleAfter.java","-1.5","-showWeaveInfo"});
  	MessageSpec ms = new MessageSpec(null,null);
  	assertMessages(cR,ms);
  	RunResult rR = run("SimpleAfter");
  }
  
  // !!! -XnoInline set for around advice to work
  public void testSingletonAspectBindingsTest() {
  	CompilationResult cR = null;
  
  	try {
  	 cR = ajc(baseDir,new String[]{"SingletonAspectBindingsTest.java","-1.5","-showWeaveInfo","-XnoInline"});
  	} finally {
  		System.err.println(cR.getStandardError());
  	}
  	MessageSpec ms = new MessageSpec(null,null);
  	assertMessages(cR,ms);
  	RunResult rR = run("SingletonAspectBindingsTest");
  }
}