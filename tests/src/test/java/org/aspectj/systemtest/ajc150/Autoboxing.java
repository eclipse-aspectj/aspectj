/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Andy Clement - initial implementation
 *******************************************************************************/
package org.aspectj.systemtest.ajc150;

import org.aspectj.testing.XMLBasedAjcTestCase;

import junit.framework.Test;

/**
This test must be run under a Java5 VM - so it is *not* currently
in the test suite !!!
*/
public class Autoboxing extends XMLBasedAjcTestCase {

	  public static Test suite() {
	    return XMLBasedAjcTestCase.loadSuite(Autoboxing.class);
	  }

	  protected java.net.URL getSpecFile() {
	    return getClassResource("ajc150.xml");
	  }
	
	public void testSimpleBoxing() {
		runTest("simple boxing test");
	}
	
	public void testIntegerBoxing() {
		runTest("integer boxing");
	}

	public void testCharacterBoxing() {
		runTest("char boxing");
	}

	public void testDoubleBoxing() {
		runTest("double boxing");
	}

	public void testFloatBoxing() {
		runTest("float boxing");
	}
	
	public void testShortBoxing() {
		runTest("short boxing");
	}
	
	public void testLongBoxing() {
		runTest("long boxing");
	}
	
	public void testBooleanBoxing() {
		runTest("boolean boxing");
	}
	
	public void testByteBoxing() {
		runTest("byte boxing");
	}
	
	public void testBoxingAfterReturning() {
		runTest("boxing in after returning");
	}
//		CompilationResult cR = binaryWeave("testcode.jar","AspectAfterReturning.aj",0,0,"-1.5");
//		//System.err.println(cR.getStandardError());
//		assertTrue("Expected six weaving messages but got: "+getWeavingMessages(cR.getInfoMessages()).size(),
//				getWeavingMessages(cR.getInfoMessages()).size()==6);
//		RunResult rR = run("AspectAfterReturning");
//		int lines = countLines(rR.getStdErr());
//		assertTrue("Expected 6 lines of output but got: #"+lines+":\n"+rR.getStdErr(),lines==6);	
//	}
//		
//	public int countLines(String s) {
//		int count = 0;
//		while (s.indexOf("\n")!=-1) {
//			count++;
//			s = s.substring(s.indexOf("\n")+1);
//		}
//		return count;
//	}
//	
//	protected void verify(String output,String lookingFor) {
//		assertTrue("Didn't find expected string '"+lookingFor+"' in:\n"+output,output.indexOf(lookingFor)!=-1);
//	}
//	
}
