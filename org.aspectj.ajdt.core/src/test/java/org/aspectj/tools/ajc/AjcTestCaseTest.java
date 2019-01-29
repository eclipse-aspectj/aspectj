/* *******************************************************************
 * Copyright (c) 2004 IBM Corporation
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Adrian Colyer, 
 * ******************************************************************/
package org.aspectj.tools.ajc;

import java.io.File;

import org.aspectj.util.FileUtil;

/**
 * @author colyer
 * Exercise the features of the AjcTestCase class and check they do as
 * expected
 */
public class AjcTestCaseTest extends AjcTestCase {

	public void testCompile() {
		File baseDir = new File("../tests/base/test106");
		String[] args = new String[] {"Driver.java","pkg/Obj.java"};
		CompilationResult result = ajc(baseDir,args);
		assertNoMessages(result);
		RunResult rresult = run("Driver",new String[0],null);
		System.out.println(rresult.getStdOut());
	}
	
	public void testIncrementalCompile() throws Exception {
		File baseDir = new File("../tests/incrementalju/initialTests/classAdded");
		String[] args = new String[] {"-sourceroots","src","-d",".","-incremental"};
		CompilationResult result = ajc(baseDir,args);
		assertNoMessages(result);
		RunResult rr = run("main.Main",new String[0],null);
		// prepare for increment
		FileUtil.copyFile(new File(baseDir,"src.20/main/Main.java"),
						 new File(ajc.getSandboxDirectory(),"src/main/Main.java"));
		assertFalse("main.Target does not exist",new File(ajc.getSandboxDirectory(),"main/Target.class").exists());
		result = ajc.doIncrementalCompile();
		assertNoMessages(result);
		assertTrue("main.Target created",new File(ajc.getSandboxDirectory(),"main/Target.class").exists());
		rr = run("main.Main",new String[0],null);
		System.out.println(rr.getStdOut());
	}
	
}
