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

import java.io.File;
import java.io.IOException;
import java.util.Date;

public class ProceedOnErrorTestCase extends CommandTestCase {

	public ProceedOnErrorTestCase(String name) {
		super(name);
	}

	/**
	 * Compile C1.java that defines C.class then compile C2.java which contains another version of C.class but also contains errors.
	 * Because -proceedOnError is not supplied, the .class file should not be touched when compiling C2.java.
	 */
	public void testNoProceedOnError() throws IOException {
		// try {
		// AjBuildManager.continueWhenErrors=false;
		checkCompile("src1/C1.java", NO_ERRORS);
		File f = new File(getSandboxName(), "C.class");
		long oldmodtime = f.lastModified();
		pause(2);
		checkCompile("src1/C2.java", new int[] { 1 });
		f = new File(getSandboxName(), "C.class");
		long newmodtime = f.lastModified();
		// Without -proceedOnError supplied, we should *not* change the time stamp on the .class file
		assertTrue("The .class file should not have been modified as '-proceedOnError' was not supplied (old="
				+ new Date(oldmodtime).toString() + ")(new=" + new Date(newmodtime).toString() + ")", oldmodtime == newmodtime);
		// } finally {
		// AjBuildManager.continueWhenErrors=true;
		// }
	}

	public void testProceedOnError() throws IOException {
		checkCompile("src1/C1.java", NO_ERRORS);
		File f = new File(getSandboxName(), "C.class");
		long oldmodtime = f.lastModified();
		pause(2);
		String sandboxName = getSandboxName();
		checkCompile("src1/C2.java", new String[] { "-proceedOnError" }, new int[] { 1 }, sandboxName);
		f = new File(sandboxName, "C.class");
		long newmodtime = f.lastModified();
		// Without -proceedOnError supplied, we should *not* change the time stamp on the .class file
		assertTrue("The .class file should have been modified as '-proceedOnError' *was* supplied (old="
				+ new Date(oldmodtime).toString() + ")(new=" + new Date(newmodtime).toString() + ")", newmodtime > oldmodtime);
	}

	private void pause(int secs) {
		try {
			Thread.sleep(secs * 1000);
		} catch (InterruptedException ie) {
		}
	}

}
