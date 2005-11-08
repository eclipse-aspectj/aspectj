/* *******************************************************************
 * Copyright (c) 1999-2001 Xerox Corporation, 
 *               2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 * ******************************************************************/

package org.aspectj.ajde;

import java.io.File;

import org.aspectj.tools.ajc.AjcTestCase;

/**
 * @author Mik Kersten
 */
public class SymbolFileGenerationTest extends AjcTestCase {
	private static final String DIR = "../ajde/testdata/examples/coverage";

	protected File dir = new File(DIR);
	protected File configFile = new File(DIR + "/coverage.lst");
	protected File esymFile = new File(DIR + "/ModelCoverage.ajesym");
	protected File outDir = new File(DIR + "/bin");	
	protected File crossRefsFile = new File(outDir.getAbsolutePath() + "/build.ajsym");
	
	protected void setUp() throws Exception {
		super.setUp();
	}
	
	public void testCrossRefsFileGeneration() {
		if (crossRefsFile.exists()) assertTrue(crossRefsFile.delete());
		if (esymFile.exists()) assertTrue(esymFile.delete());
		String[] args = new String[] {
				"-d",
				outDir.getAbsolutePath(),
				"-crossrefs",
				"@" + configFile.getAbsolutePath()
		};
		ajc(dir, args);
		
		assertFalse(esymFile.exists());
		assertTrue(crossRefsFile.exists());
	}

	public void testEmacssymGeneration() {
		if (crossRefsFile.exists()) assertTrue(crossRefsFile.delete());
		if (esymFile.exists()) assertTrue(esymFile.delete());
		String[] args = new String[] {
				"-d",
				outDir.getAbsolutePath(),
				"-emacssym",
				"@" + configFile.getAbsolutePath()
		};
		ajc(dir, args);
		
		assertTrue(esymFile.exists());
		assertFalse(crossRefsFile.exists());
	}
}
