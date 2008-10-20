/* *******************************************************************
 * Copyright (c) 1999-2001 Xerox Corporation, 
 *               2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 * ******************************************************************/

package org.aspectj.ajde;

import java.io.File;
import java.io.FileFilter;

import org.aspectj.tools.ajc.AjcTestCase;
import org.aspectj.util.FileUtil;

/**
 * @author Mik Kersten
 */
public class SymbolFileGenerationTest extends AjcTestCase {
	private static final String DIR = "../ajde/testdata/examples/coverage";

	protected File dir = new File(DIR);
	protected File configFile = new File(DIR + "/coverage.lst");
	protected File esymFile, outDir, crossRefsFile;

	protected void setUp() throws Exception {
		super.setUp();
		esymFile = new File(DIR + "/ModelCoverage.ajesym");
		outDir = new File(DIR + "/bin");
		crossRefsFile = new File(outDir.getAbsolutePath() + "/build.ajsym");
	}

	protected void tearDown() throws Exception {
		super.tearDown();

		FileUtil.deleteContents(new File(DIR), ajesymResourceFileFilter);
		FileUtil.deleteContents(new File(DIR + "/pkg"), ajesymResourceFileFilter);

		FileUtil.deleteContents(new File(DIR + "/bin"));
		(new File(DIR + "/bin")).delete();

	}

	public FileFilter ajesymResourceFileFilter = new FileFilter() {
		public boolean accept(File pathname) {
			String name = pathname.getName().toLowerCase();
			return name.endsWith(".ajesym");
		}
	};

	public void testCrossRefsFileGeneration() {
		if (crossRefsFile.exists())
			assertTrue(crossRefsFile.delete());
		if (esymFile.exists())
			assertTrue(esymFile.delete());
		String[] args = new String[] { "-d", outDir.getAbsolutePath(), "-crossrefs", "@" + configFile.getAbsolutePath() };
		ajc(dir, args);

		assertFalse(esymFile.exists());
		assertTrue(crossRefsFile.exists());
	}

	public void testEmacssymGeneration() {
		if (crossRefsFile.exists()) {
			assertTrue(crossRefsFile.delete());
		}
		if (esymFile.exists())
			assertTrue(esymFile.delete());
		String[] args = new String[] { "-d", outDir.getAbsolutePath(), "-emacssym", "@" + configFile.getAbsolutePath() };
		ajc(dir, args);

		assertTrue(esymFile.exists());
		assertFalse(crossRefsFile.exists());
	}
}
