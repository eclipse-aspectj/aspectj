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

import junit.framework.Test;

import org.aspectj.apache.bcel.classfile.JavaClass;
import org.aspectj.apache.bcel.classfile.Method;
import org.aspectj.apache.bcel.util.ClassPath;
import org.aspectj.apache.bcel.util.SyntheticRepository;
import org.aspectj.testing.XMLBasedAjcTestCase;

public class Annotations extends XMLBasedAjcTestCase {

	  public static Test suite() {
	    return XMLBasedAjcTestCase.loadSuite(Annotations.class);
	  }

	  protected File getSpecFile() {
	    return new File("../tests/src/org/aspectj/systemtest/ajc150/ajc150.xml");
	  }
	  
  public void testCompilingAnnotation() {
  	runTest("compiling an annotation");
  }
  
  public void testCompilingAnnotatedFile() {
  	runTest("compiling annotated file");
  }
  
  public void testCompilingUsingWithinAndAnnotationTypePattern() {
  	runTest("annotations and within (src)");
  }
  
  /**
   * We had a bug where annotations were not present in the output class file for methods
   * that got woven.  This was due to unpacking bugs in LazyMethodGen.  This test compiles
   * a simple program then checks the annotations were copied across.
   */
  public void testBugWithAnnotationsLostOnWovenMethods() throws ClassNotFoundException {
  	runTest("losing annotations...");
  	
  	JavaClass jc = getClassFrom(ajc.getSandboxDirectory(),"Program");
    Method[] meths = jc.getMethods();
    for (int i = 0; i < meths.length; i++) {
		Method method = meths[i];
		if (method.getName().equals("m1")) {
			assertTrue("Didn't have annotations - were they lost? method="+method.getName(),method.getAnnotations().length==1);
		}
	}
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