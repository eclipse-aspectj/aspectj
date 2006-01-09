/********************************************************************
 * Copyright (c) 2005 Contributors. All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: IBM Corporation - initial API and implementation 
 * 				 Helen Hawkins   - iniital version
 *******************************************************************/
package org.aspectj.tools.ajdoc;

import java.io.File;

import org.aspectj.util.FileUtil;

import junit.framework.TestCase;


public class EnumTest extends TestCase {

	private File outdir;
	
	protected void setUp() throws Exception {
		super.setUp();
		outdir = new File("../ajdoc/testdata/pr119453/doc");
	}
	
	protected void tearDown() throws Exception {
		super.tearDown();
		
		FileUtil.deleteContents(new File("ajdocworkingdir"));
		(new File("ajdocworkingdir")).delete();
		
		FileUtil.deleteContents(new File("testdata/pr119453/doc"));
		(new File("testdata/pr119453/doc")).delete();
	}
	
	/**
	 * Test for pr122728 - no StringOutOfBoundsException
	 * when processing an Enum
	 */
    public void testEnum() throws Exception {
        outdir.delete();
		File f = new File("../ajdoc/testdata/pr122728/src/pack/MyEnum.java");
    	
        String[] args = { 
              "-XajdocDebug",
              "-private",
              "-source", 
              "1.5",
            "-d", 
            outdir.getAbsolutePath(),
            f.getAbsolutePath()
        };
        org.aspectj.tools.ajdoc.Main.main(args);
    }

	/**
	 * Test for pr122728 - no StringOutOfBoundsException
	 * when processing an Enum 
	 */
    public void testInlinedEnum() throws Exception {
    	outdir.delete();
		File f = new File("../ajdoc/testdata/pr122728/src/pack/ClassWithInnerEnum.java");
    	
        String[] args = { 
              "-XajdocDebug",
              "-private",
              "-source", 
              "1.5",
            "-d", 
            outdir.getAbsolutePath(),
            f.getAbsolutePath()
        };
        org.aspectj.tools.ajdoc.Main.main(args);
    }
    
	/**
	 * Test for pr122728 - no StringOutOfBoundsException
	 * when processing an Enum
	 */
    public void testEnumWithMethods() throws Exception {
    	outdir.delete();
		File f = new File("../ajdoc/testdata/pr122728/src/pack/EnumWithMethods.java");
    	
        String[] args = { 
              "-XajdocDebug",
              "-private",
              "-source", 
              "1.5",
            "-d", 
            outdir.getAbsolutePath(),
            f.getAbsolutePath()
        };
        org.aspectj.tools.ajdoc.Main.main(args);
    }
}
