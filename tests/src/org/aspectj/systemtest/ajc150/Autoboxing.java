/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Andy Clement - initial implementation
 *******************************************************************************/
package org.aspectj.systemtest.ajc150;

import java.io.File;

import org.aspectj.tools.ajc.CompilationResult;

/**
This test must be run under a Java5 VM - so it is *not* currently
in the test suite !!!
*/
public class Autoboxing extends TestUtils {

	private boolean runningUnderJava5 = false;
	
	
	protected void setUp() throws Exception {
		super.setUp();
		baseDir = new File("../tests/java5/autoboxing");
	}
	
	
	public void testSimpleBoxing() {
		CompilationResult cR = binaryWeave("testcode.jar","SimpleAutoboxingAspect.aj",0,0,"-1.5");
		assertTrue("Expected two weaving messages (both on line 7) but got:"+getWeavingMessages(cR.getInfoMessages()).size(),
				getWeavingMessages(cR.getInfoMessages()).size()==2);
		RunResult rR = run("SimpleAutoboxing");
		verify(rR.getStdErr(),"Matching by Integer:20000");
		verify(rR.getStdErr(),"Matching by int:20000");
		verify(rR.getStdErr(),"method_takes_Integer=20000");
	}
	
	public void testIntegerBoxing() {
		CompilationResult cR = binaryWeave("testcode.jar","AspectInteger.aj",0,0,"-1.5");
		System.err.println(cR.getStandardError());
		assertTrue("Expected eight weaving messages but got: "+getWeavingMessages(cR.getInfoMessages()).size(),
				getWeavingMessages(cR.getInfoMessages()).size()==8);
		RunResult rR = run("AutoboxingI");
		verify(rR.getStdErr(),"Matching by Integer:10000");
		verify(rR.getStdErr(),"Matching by int:10000");
		verify(rR.getStdErr(),"method_takes_Integer=10000");
		verify(rR.getStdErr(),"Matching by Integer:20000");
		verify(rR.getStdErr(),"Matching by int:20000");
		verify(rR.getStdErr(),"method_takes_Integer=20000");
		verify(rR.getStdErr(),"Matching by Integer:30000");
		verify(rR.getStdErr(),"Matching by int:30000");
		verify(rR.getStdErr(),"method_takes_int=30000");
		verify(rR.getStdErr(),"Matching by Integer:40000");
		verify(rR.getStdErr(),"Matching by int:40000");
		verify(rR.getStdErr(),"method_takes_int=40000");
	}
	
	public void testCharacterBoxing() {
		CompilationResult cR = binaryWeave("testcode.jar","AspectChar.aj",0,0,"-1.5");
		System.err.println(cR.getStandardError());
		assertTrue("Expected eight weaving messages but got: "+getWeavingMessages(cR.getInfoMessages()).size(),
				getWeavingMessages(cR.getInfoMessages()).size()==8);
		RunResult rR = run("AutoboxingC");
		int lines = countLines(rR.getStdErr());
		assertTrue("Expected 12 lines of output but got: #"+lines+":\n"+rR.getStdErr(),lines==12);
	}
	
	public void testDoubleBoxing() {
		CompilationResult cR = binaryWeave("testcode.jar","AspectDouble.aj",0,0,"-1.5");
		System.err.println(cR.getStandardError());
		assertTrue("Expected eight weaving messages but got: "+getWeavingMessages(cR.getInfoMessages()).size(),
				getWeavingMessages(cR.getInfoMessages()).size()==8);
		RunResult rR = run("AutoboxingD");
		int lines = countLines(rR.getStdErr());
		assertTrue("Expected 12 lines of output but got: #"+lines+":\n"+rR.getStdErr(),lines==12);
	}
	
	public void testFloatBoxing() {
		CompilationResult cR = binaryWeave("testcode.jar","AspectFloat.aj",0,0,"-1.5");
		System.err.println(cR.getStandardError());
		assertTrue("Expected eight weaving messages but got: "+getWeavingMessages(cR.getInfoMessages()).size(),
				getWeavingMessages(cR.getInfoMessages()).size()==8);
		RunResult rR = run("AutoboxingF");
		int lines = countLines(rR.getStdErr());
		assertTrue("Expected 12 lines of output but got: #"+lines+":\n"+rR.getStdErr(),lines==12);
	}
	
	public void testShortBoxing() {
		CompilationResult cR = binaryWeave("testcode.jar","AspectShort.aj",0,0,"-1.5");
		System.err.println(cR.getStandardError());
		assertTrue("Expected eight weaving messages but got: "+getWeavingMessages(cR.getInfoMessages()).size(),
				getWeavingMessages(cR.getInfoMessages()).size()==8);
		RunResult rR = run("AutoboxingS");
		int lines = countLines(rR.getStdErr());
		assertTrue("Expected 12 lines of output but got: #"+lines+":\n"+rR.getStdErr(),lines==12);
	}
	
	public void testLongBoxing() {
		CompilationResult cR = binaryWeave("testcode.jar","AspectLong.aj",0,0,"-1.5");
		System.err.println(cR.getStandardError());
		assertTrue("Expected eight weaving messages but got: "+getWeavingMessages(cR.getInfoMessages()).size(),
				getWeavingMessages(cR.getInfoMessages()).size()==8);
		RunResult rR = run("AutoboxingJ");
		int lines = countLines(rR.getStdErr());
		assertTrue("Expected 12 lines of output but got: #"+lines+":\n"+rR.getStdErr(),lines==12);
	}
	
	public void testBooleanBoxing() {
		CompilationResult cR = binaryWeave("testcode.jar","AspectBoolean.aj",0,0,"-1.5");
		System.err.println(cR.getStandardError());
		assertTrue("Expected eight weaving messages but got: "+getWeavingMessages(cR.getInfoMessages()).size(),
				getWeavingMessages(cR.getInfoMessages()).size()==8);
		RunResult rR = run("AutoboxingZ");
		int lines = countLines(rR.getStdErr());
		assertTrue("Expected 12 lines of output but got: #"+lines+":\n"+rR.getStdErr(),lines==12);
	}
	
	public void testByteBoxing() {
		CompilationResult cR = binaryWeave("testcode.jar","AspectByte.aj",0,0,"-1.5");
		System.err.println(cR.getStandardError());
		assertTrue("Expected eight weaving messages but got: "+getWeavingMessages(cR.getInfoMessages()).size(),
				getWeavingMessages(cR.getInfoMessages()).size()==8);
		RunResult rR = run("AutoboxingB");
		int lines = countLines(rR.getStdErr());
		assertTrue("Expected 12 lines of output but got: #"+lines+":\n"+rR.getStdErr(),lines==12);
	}
	
	public void testBoxingAfterReturning() {
		CompilationResult cR = binaryWeave("testcode.jar","AspectAfterReturning.aj",0,0,"-1.5");
		//System.err.println(cR.getStandardError());
		assertTrue("Expected six weaving messages but got: "+getWeavingMessages(cR.getInfoMessages()).size(),
				getWeavingMessages(cR.getInfoMessages()).size()==6);
		RunResult rR = run("AspectAfterReturning");
		int lines = countLines(rR.getStdErr());
		assertTrue("Expected 6 lines of output but got: #"+lines+":\n"+rR.getStdErr(),lines==6);	
	}
		
	public int countLines(String s) {
		int count = 0;
		while (s.indexOf("\n")!=-1) {
			count++;
			s = s.substring(s.indexOf("\n")+1);
		}
		return count;
	}
	
	protected void verify(String output,String lookingFor) {
		assertTrue("Didn't find expected string '"+lookingFor+"' in:\n"+output,output.indexOf(lookingFor)!=-1);
	}
	
}
