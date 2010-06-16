/*******************************************************************************
 * Copyright (c) 2010 Contributors 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andy Clement - SpringSource
 *******************************************************************************/
package org.aspectj.testing;

import java.io.File;

import org.aspectj.tools.ajc.AjcTestCase;

/**
 * Support simple file system operations in a test spec. Example:<br>
 * &lt;file deletefile="foo.jar"/&gt; will delete the file foo.jar from the sandbox.
 * 
 * @author Andy Clement
 */
public class FileSpec implements ITestStep {

	private String toDelete;

	// private String dir;
	// private AjcTest test;

	public FileSpec() {
	}

	public void setDeletefile(String file) {
		this.toDelete = file;
	}

	public void addExpectedMessage(ExpectedMessageSpec message) {
	}

	public void execute(AjcTestCase inTestCase) {
		File sandbox = inTestCase.getSandboxDirectory();
		if (toDelete != null) {
			new File(sandbox, toDelete).delete();
		}
	}

	public void setBaseDir(String dir) {
		// this.dir = dir;
	}

	public void setTest(AjcTest test) {
		// this.test = test;
	}

}
