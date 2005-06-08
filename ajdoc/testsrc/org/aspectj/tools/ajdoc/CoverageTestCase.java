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
 * A long way to go until full coverage, but this is the place to add more.
 * 
 * @author Mik Kersten
 */
public class CoverageTestCase extends TestCase {

	protected File file0 = new File("testdata/coverage/InDefaultPackage.java");
	protected File file1 = new File("testdata/coverage/foo/ClassA.java");
	protected File aspect1 = new File("testdata/coverage/foo/UseThisAspectForLinkCheck.aj");
	protected File file2 = new File("testdata/coverage/foo/InterfaceI.java");
	protected File file3 = new File("testdata/coverage/foo/PlainJava.java");
	protected File file4 = new File("testdata/coverage/foo/ModelCoverage.java");
	protected File file5 = new File("testdata/coverage/fluffy/Fluffy.java");
	protected File file6 = new File("testdata/coverage/fluffy/bunny/Bunny.java");
	protected File file7 = new File("testdata/coverage/fluffy/bunny/rocks/Rocks.java");
	protected File file8 = new File("testdata/coverage/fluffy/bunny/rocks/UseThisAspectForLinkCheckToo.java");
	protected File file9 = new File("testdata/coverage/foo/PkgVisibleClass.java");
	protected File file10 = new File("testdata/coverage/foo/NoMembers.java");
    
	protected File outdir = new File("testdata/coverage/doc");
	
	public void testOptions() {
		outdir.delete();
		String[] args = { 
			"-private",
			"-encoding",
			"EUCJIS",
			"-docencoding",
			"EUCJIS",
			"-charset",
			"UTF-8",
            "-classpath",
            AjdocTests.ASPECTJRT_PATH.getPath(),
			"-d", 
			outdir.getAbsolutePath(),
			file0.getAbsolutePath(), 
		};
		org.aspectj.tools.ajdoc.Main.main(args);
	    assertTrue(true);
	}
	
    public void testCoveragePublicMode() {
        outdir.delete();
        String[] args = { 
            "-public",
            "-source", 
            "1.4",
            "-classpath",
            AjdocTests.ASPECTJRT_PATH.getPath(),
            "-d", 
            outdir.getAbsolutePath(),
            file3.getAbsolutePath(),
            file9.getAbsolutePath() 
        };
        org.aspectj.tools.ajdoc.Main.main(args);
    }
    
	public void testCoverage() {
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
			aspect1.getAbsolutePath(), 
			file0.getAbsolutePath(), 
			file1.getAbsolutePath(), 
			file2.getAbsolutePath(),
			file3.getAbsolutePath(),
			file4.getAbsolutePath(),
			file5.getAbsolutePath(),
			file6.getAbsolutePath(),
			file7.getAbsolutePath(),
			file8.getAbsolutePath(),
			file9.getAbsolutePath(),
            file10.getAbsolutePath()
		};
		org.aspectj.tools.ajdoc.Main.main(args);
	}
	
//	public void testPlainJava() {
//		outdir.delete();
//		String[] args = { "-d", 
//				outdir.getAbsolutePath(),
//				file3.getAbsolutePath() };
//		org.aspectj.tools.ajdoc.Main.main(args);
//	}
	
	protected void setUp() throws Exception {
		super.setUp();
	}
	
	protected void tearDown() throws Exception {
		super.tearDown();
	}
}
