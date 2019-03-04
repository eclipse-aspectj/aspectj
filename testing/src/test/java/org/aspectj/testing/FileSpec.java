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
	
	private String renameFrom;
	private String renameTo;

	// private String dir;
	// private AjcTest test;

	public FileSpec() {
	}

	public void setRenameFrom(String file) {
		this.renameFrom = file;
	}
	
	public void setRenameTo(String file) {
		this.renameTo = file;
	}
	
	public void setDeletefile(String file) {
		this.toDelete = file;
	}

	@Override
	public void addExpectedMessage(ExpectedMessageSpec message) {
	}

	@Override
	public void execute(AjcTestCase inTestCase) {
		File sandbox = inTestCase.getSandboxDirectory();
		if (toDelete != null) {
			File targetForDeletion = new File(sandbox, toDelete);
			if (targetForDeletion.isFile()) {
				boolean b = targetForDeletion.delete();
				if (!b) {
					throw new IllegalStateException("Failed to delete "+targetForDeletion);
				}
			} else {
				recursiveDelete(targetForDeletion);
			}
		}
		if (renameFrom != null) {
			if (renameTo == null) {
				throw new IllegalStateException("If setting renameFrom the renameTo should also be set");
			}
			File fileFrom = new File(sandbox, renameFrom);
			File fileTo = new File(sandbox, renameTo);
			fileFrom.renameTo(fileTo);
		}
	}

	private void recursiveDelete(File toDelete) {
		if (toDelete.isDirectory()) {
			File[] files = toDelete.listFiles();
			for (File f: files) {
				recursiveDelete(f);
			}
		}
		toDelete.delete();
	}

	@Override
	public void setBaseDir(String dir) {
		// this.dir = dir;
	}

	@Override
	public void setTest(AjcTest test) {
		// this.test = test;
	}

}
