/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 * ******************************************************************/

package org.aspectj.ajdt.internal.compiler.batch;

import java.io.IOException;

import org.aspectj.testing.util.TestUtil;


public class CompileAndRunTestCase extends CommandTestCase {

	public CompileAndRunTestCase(String name) {
		super(name);
	}

	public void testAround() throws IOException {
		checkCompile("src1/AroundA.java", NO_ERRORS);
		TestUtil.runMain("out", "AroundAMain");
	}
	
	public void testInterType() throws IOException {
		checkCompile("src1/InterType.java", NO_ERRORS);
		TestUtil.runMain("out", "InterType");
	}
	
	public void testInterTypeMethods() throws IOException {
		checkCompile("src1/InterTypeMethods.java", NO_ERRORS);
		TestUtil.runMain("out", "InterTypeMethods");
	}
	
	public void testIf() throws IOException {
		CommandTestCase.checkCompile("src1/IfPcd.java", CommandTestCase.NO_ERRORS);
		TestUtil.runMain("out", "IfPcd");
	}
	
	public void testDeclareParentsFail() throws IOException {
		CommandTestCase.checkCompile("src1/ParentsFail.java", new int[] {3, 11, 19, 21});
	}
	
	public void testDeclareParents() throws IOException {
		CommandTestCase.checkCompile("src1/Parents.java", CommandTestCase.NO_ERRORS);
		TestUtil.runMain("out", "Parents");
	}
	
	public void testPerCflow() throws IOException {
		CommandTestCase.checkCompile("src1/PerCflow.java", CommandTestCase.NO_ERRORS);
		TestUtil.runMain("out", "PerCflow");
	}
		
	public void testPerObject() throws IOException {
		CommandTestCase.checkCompile("src1/PerObject.java", CommandTestCase.NO_ERRORS);
		TestUtil.runMain("out", "PerObject");
	}
		
	public void testDeclareSoft() throws IOException {
		CommandTestCase.checkCompile("src1/DeclareSoft.java", CommandTestCase.NO_ERRORS);
		TestUtil.runMain("out", "DeclareSoft");
	}
		
	public void testPrivileged() throws IOException {
		CommandTestCase.checkCompile("src1/Privileged.java", CommandTestCase.NO_ERRORS);
		TestUtil.runMain("out", "Privileged");
	}
		
	public void testHandler() throws IOException {
		CommandTestCase.checkCompile("src1/Handler.java", CommandTestCase.NO_ERRORS);
		TestUtil.runMain("out", "Handler");
	}
		
	public void testInterConstructors() throws IOException {
		CommandTestCase.checkCompile("src1/InterTypeConstructors.java", CommandTestCase.NO_ERRORS);
		TestUtil.runMain("out", "InterTypeConstructors");
	}
		
	public void testAroundA1() throws IOException {
		CommandTestCase.checkCompile("src1/AroundA1.java", CommandTestCase.NO_ERRORS);
		TestUtil.runMain("out", "AroundA1");
	}
		
}
