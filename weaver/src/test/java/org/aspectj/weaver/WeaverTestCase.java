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

package org.aspectj.weaver;

import java.io.File;

import org.aspectj.util.FileUtil;

import junit.framework.TestCase;

public abstract class WeaverTestCase extends TestCase {

	public static final String TESTDATA_PATH = "../weaver/testdata";
	public static final String OUTDIR_PATH = "../weaver/out";

	/** @return File outDir (writable) or null if unable to write */
	public static File getOutdir() {
		File result = new File(OUTDIR_PATH);
		if (result.mkdirs() || (result.canWrite() && result.isDirectory())) {
			return result;
		}
		return null;
	}

	/** best efforts to delete the output directory and any contents */
	public static void removeOutDir() {
		File outDir = getOutdir();
		if (null != outDir) {
			FileUtil.deleteContents(outDir);
			outDir.delete();
		}
	}

	public WeaverTestCase(String name) {
		super(name);
	}

}
