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

import java.util.*;

import org.aspectj.ajdt.ajc.*;
import org.aspectj.bridge.*;

import junit.framework.*;

/**
 * @author hugunin
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class BasicCommandTestCase extends CommandTestCase {

	/**
	 * Constructor for CommandTestCase.
	 * @param name
	 */
	public BasicCommandTestCase(String name) {
		super(name);
	}
	
	public void testA() {
		checkCompile("src1/A.java", NO_ERRORS);
	}
	
	public void testA1() {
		checkCompile("src1/A1.java", NO_ERRORS);
	}
	
	public void testBadA() {
		checkCompile("src1/BadA.java", new int[] {7, 8});
	}
	
	public void testHello() {
		checkCompile("src1/Hello.java", NO_ERRORS);
	}

	public void testBadHello() {
		checkCompile("src1/BadHello.java", new int[] {5});
	}

	public void testMissingHello() {
		checkCompile("src1/MissingHello.java", TOP_ERROR);
	}
	
	public void testBadBinding() {
		checkCompile("src1/BadBinding.java", new int[] {2, 4, 8, 10, 13, 16, 19});
	}
	public void testThisAndModifiers() {
		checkCompile("src1/ThisAndModifiers.java", NO_ERRORS);
	}
	public void testDeclares() {
		checkCompile("src1/Declares.java", new int[] {3});
	}	
	
	public void testDeclareWarning() {
		checkCompile("src1/DeclareWarning.java", NO_ERRORS);
	}	
	
	
	public void testP1() {
		checkCompile("src1/p1/Foo.java", NO_ERRORS);
	}
	
	public void testUnimplementedSyntax() {
		checkCompile("src1/UnimplementedSyntax.java", 
			new int[] {5, 15, 16, 23});
	}
	public void testXlintWarn() {
		checkCompile("src1/Xlint.java", NO_ERRORS);
	}
	public void testXlintError() {
		List args = new ArrayList();

		args.add("-d");
		args.add("out");
		
		args.add("-classpath");
		args.add("../runtime/bin;../lib/junit/junit.jar;../testing-client/bin");
		args.add("-Xlint:error");
		args.add("testdata/src1/Xlint.java");
		
		runCompiler(args, new int[] {2});
	}
}
