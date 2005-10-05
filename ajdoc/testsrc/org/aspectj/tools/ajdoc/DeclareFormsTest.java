/* *******************************************************************
 * Copyright (c) 2003 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     Mik Kersten     initial implementation 
 * ******************************************************************/

package org.aspectj.tools.ajdoc;

import java.io.File;

import junit.framework.TestCase;

/**
 * @author Mik Kersten
 */
public class DeclareFormsTest extends TestCase {

	protected File file0 = new File("../ajdoc/testdata/declareForms/DeclareCoverage.java");
	protected File outdir = new File("../ajdoc/testdata/declareForms/doc");
	
	public void testCoverage() {
		assertTrue(file0.exists());
		outdir.delete();
		String[] args = { 
//			"-XajdocDebug",
			"-source", 
			"1.4",
			"-private",
            "-classpath",
            AjdocTests.ASPECTJRT_PATH.getPath(),
			"-d", 
			outdir.getAbsolutePath(),
			file0.getAbsolutePath(), 
		};
		org.aspectj.tools.ajdoc.Main.main(args);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}
	
	protected void tearDown() throws Exception {
		super.tearDown();
	}
}
