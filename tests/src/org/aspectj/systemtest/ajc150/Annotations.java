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
import java.util.ArrayList;
import java.util.List;

import org.aspectj.apache.bcel.classfile.JavaClass;
import org.aspectj.apache.bcel.classfile.Method;
import org.aspectj.tools.ajc.CompilationResult;

public class Annotations extends TestUtils {
	
  protected void setUp() throws Exception {
	super.setUp();
	baseDir = new File("../tests/java5/annotations");
  }
  
  public void testCompilingAnnotation() {
  	CompilationResult cR = ajc(baseDir,new String[]{"SimpleAnnotation.java","-1.5"});
  	MessageSpec ms = new MessageSpec(null,null);
  	assertMessages(cR,ms);
  }
  
  public void testCompilingAnnotatedFile() {
  	CompilationResult cR = ajc(baseDir,new String[]{"AnnotatedType.java","SimpleAnnotation.java","-1.5"});
  	MessageSpec ms = new MessageSpec(null,null);
  	assertMessages(cR,ms);
  }
  
  public void testCompilingUsingWithinAndAnnotationTypePattern() {
  	CompilationResult cR = ajc(new File(baseDir+File.separator+"within"),
  			new String[]{"PlainWithin.java","PlainWithinTests.java","-1.5"});
  	List expectedInfoMessages = new ArrayList();
  	expectedInfoMessages.add(new Message(21,"positive within match on annotation"));
	expectedInfoMessages.add(new Message(25,"negative within match on annotation"));
  	MessageSpec ms = new MessageSpec(expectedInfoMessages,null);
  	assertMessages(cR,ms);
  }
  
  /**
   * We had a bug where annotations were not present in the output class file for methods
   * that got woven.  This was due to unpacking bugs in LazyMethodGen.  This test compiles
   * a simple program then checks the annotations were copied across.
   */
  public void testBugWithAnnotationsLostOnWovenMethods() throws ClassNotFoundException {
  	CompilationResult cR = ajc(new File(baseDir+File.separator+"attarget"),
  			new String[]{"Program.java","AtTargetAspect.java","-1.5"});
  	System.err.println(cR.getStandardError());
  	List expectedInfoMessages = new ArrayList();
  	MessageSpec ms = new MessageSpec(null,null);
  	assertMessages(cR,ms);
  	
  	JavaClass jc = getClassFrom(ajc.getSandboxDirectory(),"Program");
    Method[] meths = jc.getMethods();
    for (int i = 0; i < meths.length; i++) {
		Method method = meths[i];
		if (method.getName().equals("m1")) {
			assertTrue("Didn't have annotations - were they lost? method="+method.getName(),method.getAnnotations().length==1);
		}
	}
  }
}