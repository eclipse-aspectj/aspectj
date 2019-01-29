/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Matthew Webster - initial implementation
 *******************************************************************************/
package org.aspectj.ajdt.internal.core.builder;

import java.io.File;

import org.aspectj.tools.ajc.AjcTestCase;
import org.aspectj.tools.ajc.CompilationResult;
import org.aspectj.weaver.WeaverMessages;

public class OutjarTest extends AjcTestCase {

	public static final String PROJECT_DIR = "OutjarTest";
	
	public static final String injarName  = "child.jar";
	public static final String aspectjarName  = "aspects.jar";
	public static final String outjarName = "outjar.jar";

	private File baseDir;
	
	/**
	 * Make copies of JARs used for -injars/-inpath and -aspectpath because so
	 * they are not overwritten when a test fails.
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		baseDir = new File("../org.aspectj.ajdt.core/testdata",PROJECT_DIR);
	}
	
	/**
	 * Aim: Check that -outjar does not coincide with a member of -injars. This
	 *      is because if a binary weave fails -outjar is deleted.
	 * 
	 * Inputs to the compiler:
	 *   -injar
	 *   -aspectpath
	 *   -outjar 
	 * 
	 * Expected result = Compile aborts with error message.
	 */
	public void testOutjarInInjars () {
		String[] args = new String[] {"-aspectpath", aspectjarName, "-injars", injarName, "-outjar", injarName};
		Message error = new Message(WeaverMessages.format(WeaverMessages.OUTJAR_IN_INPUT_PATH));
		Message fail = new Message("Usage:");
		MessageSpec spec = new MessageSpec(null,null,newMessageList(error),newMessageList(fail),null);
		CompilationResult result = ajc(baseDir,args);
//		System.out.println(result);
		assertMessages(result,spec);
	}
	
	/**
	 * Aim: Check that -outjar does not coincide with a member of -inpath. This
	 *      is because if a binary weave fails -outjar is deleted.
	 * 
	 * Inputs to the compiler:
	 *   -injar
	 *   -aspectpath
	 *   -outjar 
	 * 
	 * Expected result = Compile aborts with error message.
	 */
	public void testOutjarInInpath () {
		String[] args = new String[] {"-aspectpath", aspectjarName, "-inpath", injarName, "-outjar", injarName};
		Message error = new Message(WeaverMessages.format(WeaverMessages.OUTJAR_IN_INPUT_PATH));
		Message fail = new Message("Usage:");
		MessageSpec spec = new MessageSpec(null,null,newMessageList(error),newMessageList(fail),null);
		CompilationResult result = ajc(baseDir,args);
//		System.out.println(result);
		assertMessages(result,spec);
	}
	
	/**
	 * Aim: Check that -outjar does not coincide with a member of -aspectpath. This
	 *      is because if a binary weave fails -outjar is deleted.
	 * 
	 * Inputs to the compiler:
	 *   -injar
	 *   -aspectpath
	 *   -outjar 
	 * 
	 * Expected result = Compile aborts with error message.
	 */
	public void testOutjarInAspectpath () {
		String[] args = new String[] {"-aspectpath", aspectjarName, "-inpath", injarName, "-outjar", aspectjarName};
		Message error = new Message(WeaverMessages.format(WeaverMessages.OUTJAR_IN_INPUT_PATH));
		Message fail = new Message("Usage:");
		MessageSpec spec = new MessageSpec(null,null,newMessageList(error),newMessageList(fail),null);
		CompilationResult result = ajc(baseDir,args);
//		System.out.println(result);
		assertMessages(result,spec);
	}
	
	/**
	 * Aim: Check that -outjar is not present when compile fails.
	 * 
	 * Inputs to the compiler:
	 *   -injar
	 *   -aspectpath
	 *   -outjar 
	 * 
	 * Expected result = Compile fails with error message.
	 */
	public void testOutjarDeletedOnError () {
		String[] args = new String[] {"-aspectpath", aspectjarName, "-injars", injarName, "-outjar", outjarName,"-1.4"};
		Message error = new Message(WeaverMessages.format(WeaverMessages.CANT_FIND_TYPE_INTERFACES,"jar1.Parent"));
		MessageSpec spec = new MessageSpec(null,newMessageList(error));
		CompilationResult result = ajc(baseDir,args);
//		System.out.println(result);
		assertMessages(result,spec);
		File outjar = new File(ajc.getSandboxDirectory(),outjarName);
		assertFalse("-outjar " + outjar.getPath() + " should be deleted",outjar.exists());
	}

}
