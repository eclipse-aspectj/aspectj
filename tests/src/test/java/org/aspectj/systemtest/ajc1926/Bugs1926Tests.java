/*******************************************************************************
 * Copyright (c) 2026 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *******************************************************************************/
package org.aspectj.systemtest.ajc1926;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.aspectj.apache.bcel.classfile.JavaClass;
import org.aspectj.apache.bcel.classfile.Method;
import org.aspectj.testing.XMLBasedAjcTestCase;
import org.aspectj.tools.ajc.Ajc;
import org.aspectj.tools.ajc.CompilationResult;

import junit.framework.Test;

/**
 * @author Andy Clement
 */
public class Bugs1926Tests extends XMLBasedAjcTestCase {

  public static Test suite() {
    return XMLBasedAjcTestCase.loadSuite(Bugs1926Tests.class);
  }
  
  public void testGithubIssue366() throws Exception {
	for (int i=0;i<25;i++) {
		// We cannot use the regular runTest() option because the re-used Ajc instance won't trigger
		// the ordering problem. We need to create our own Ajc instances.
//		runTest("unstable inline method ordering");
		Ajc freshAjc = new Ajc();
	    // Hope these parameters are stable...
	    freshAjc.setBaseDir(new File("../tests/bugs1926/366"));
	    CompilationResult result = freshAjc.compile(new String[] {
	      "-26", "-classpath", System.getProperty("java.class.path"),
	      "Foo.java", "SimpleAspect.java"
	    });
	    assertNoMessages(result);
		JavaClass jc = getClassFrom(freshAjc.getSandboxDirectory(),"SimpleAspect");
		Method[] methods = jc.getMethods();
		List<Method> inlineMethods = new ArrayList<>();
		for (Method method: methods) {
			if (method.getName().startsWith("ajc$inline")) {
				inlineMethods.add(method);
			}
		}
		assertEquals(2,inlineMethods.size());
		// These will fail if during one iteration the order flips
		assertEquals("ajc$inlineAccessFieldGet$SimpleAspect$SimpleAspect$message",inlineMethods.get(0).getName());
		assertEquals("ajc$inlineAccessMethod$SimpleAspect$SimpleAspect$log",inlineMethods.get(1).getName());
	}
  }

  @Override
  protected java.net.URL getSpecFile() {
    return getClassResource("ajc1926.xml");
  }

}
