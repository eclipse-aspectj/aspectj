/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 * ******************************************************************/

package org.aspectj.ajdt.internal.compiler.batch;

import java.io.IOException;


public class CompileAndRunTestCase extends CommandTestCase {

	public CompileAndRunTestCase(String name) {
		super(name);
	}

	public void testAround() throws IOException {
		checkCompile("src1/AroundA.java", NO_ERRORS);
		runMain("AroundAMain");
	}
	
	public void testInterType() throws IOException {
		checkCompile("src1/InterType.java",new String[]{"-Xset:itdVersion=1"}, NO_ERRORS);
		runMain("InterType");
	}

	// new style itds where itdfs on interfaces are not mangled
//	public void testInterType2() throws IOException {
//		checkCompile("src1/InterType2.java", NO_ERRORS);
//		runMain("InterType2");
//	}
	
	public void testInterTypeMethods() throws IOException {
		checkCompile("src1/InterTypeMethods.java", NO_ERRORS);
		runMain("InterTypeMethods");
	}
	
	public void testIf() throws IOException {
		checkCompile("src1/IfPcd.java", CommandTestCase.NO_ERRORS);
		runMain("IfPcd");
	}
	
	public void testDeclareParentsFail() throws IOException {
		checkCompile("src1/ParentsFail.java", new int[] {3, 11, 19});
	}
	
	public void testDeclareParents() throws IOException {
		checkCompile("src1/Parents.java", CommandTestCase.NO_ERRORS);
		runMain("Parents");
	}
	
	public void testPerCflow() throws IOException {
		checkCompile("src1/PerCflow.java", CommandTestCase.NO_ERRORS);
		runMain("PerCflow");
	}
		
	public void testPerObject() throws IOException {
		checkCompile("src1/PerObject.java", CommandTestCase.NO_ERRORS);
		runMain("PerObject");
	}
		
	public void testDeclareSoft() throws IOException {
		checkCompile("src1/DeclareSoft.java", CommandTestCase.NO_ERRORS);
		runMain("DeclareSoft");
	}
		
	public void testPrivileged() throws IOException {
		checkCompile("src1/Privileged.java", CommandTestCase.NO_ERRORS);
		runMain("Privileged");
	}
		
	public void testHandler() throws IOException {
		checkCompile("src1/Handler.java", CommandTestCase.NO_ERRORS);
		runMain("Handler");
	}
		
	public void testInterConstructors() throws IOException {
		checkCompile("src1/InterTypeConstructors.java", CommandTestCase.NO_ERRORS);
		runMain("InterTypeConstructors");
	}
		
	public void testAroundA1() throws IOException {
		checkCompile("src1/AroundA1.java", CommandTestCase.NO_ERRORS);
		runMain("AroundA1");
	}
		
}
