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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import junit.framework.TestCase;

import org.aspectj.util.FileUtil;

public class FullyQualifiedArgumentTest extends TestCase {

	private File outdir;
	private File c, a;
	    
	protected void setUp() throws Exception {
		super.setUp();
		outdir = new File("../ajdoc/testdata/pr119453/doc");
		c = new File("../ajdoc/testdata/pr119453/src/pack/C.java");
		a = new File("../ajdoc/testdata/pr119453/src/pack/A.aj");
	}
	
	protected void tearDown() throws Exception {
		super.tearDown();
		
		FileUtil.deleteContents(new File("ajdocworkingdir"));
		(new File("ajdocworkingdir")).delete();
		
		FileUtil.deleteContents(new File("testdata/pr119453/doc"));
		(new File("testdata/pr119453/doc")).delete();
	}
    
	/**
	 * Test for pr119453
	 */
    public void testPr58520() throws Exception {
        outdir.delete();
        String[] args = { 
              "-XajdocDebug",
              "-private",
            "-d", 
            outdir.getAbsolutePath(),
            c.getAbsolutePath(),
            a.getAbsolutePath()
        };
        org.aspectj.tools.ajdoc.Main.main(args);
        
        checkContentsOfA();        

    }
        
    // check whether the "advises" section of the "Advice Summary" contains
    // the fully qualified argument, so for example, it says it has href
    // .../ajdoc/testdata/pr119453/doc/pack/C.html#method3(java.lang.String) 
    // rather than .../ajdoc/testdata/pr119453/doc/pack/C.html#method3(String)
    private void checkContentsOfA() throws Exception {
        File htmlA = new File("../ajdoc/testdata/pr119453/doc/pack/A.html");
        if (htmlA == null) {
			fail("couldn't find ../ajdoc/testdata/pr119453/doc/pack/A.html - were there compilation errors?");
		}
	    BufferedReader readerA = new BufferedReader(new FileReader(htmlA));
        boolean containsAdviceSummary = false;
        String lineA = readerA.readLine();
        while( lineA != null && (!containsAdviceSummary)) {
        	if (lineA.indexOf("ADVICE SUMMARY") != -1) {
        		containsAdviceSummary = true;
        		boolean containsFullyQualifiedArgument = false;
        		boolean containsUnqualifiedArgument = false;
        		// walk through the information in this section
				String nextLine = readerA.readLine();
				while(nextLine != null 
						&& (nextLine.indexOf("========") == -1)
						&& (!containsFullyQualifiedArgument || 
								!containsUnqualifiedArgument)) {
					if (nextLine.indexOf("C.html#method3(java.lang.String)") != -1) {
						containsFullyQualifiedArgument = true;
					} 
					if (nextLine.indexOf("C.html#method3(String)") != -1) {
						containsUnqualifiedArgument = true;
					} 
					nextLine = readerA.readLine();
				}
				assertTrue("Advice summary should have link to " +
						"'C.html#method3(java.lang.String)'", 
						containsFullyQualifiedArgument);
				assertFalse("Advice summary should not have link to " +
						"'C.html#method3(String)'", 
						containsUnqualifiedArgument);
				
				lineA = nextLine;
			} else {
				lineA = readerA.readLine();
			}
        }
        readerA.close();
		
        assertTrue("should have Advice Summary information in " +
        		"../ajdoc/testdata/pr119453/doc/pack/A.html", containsAdviceSummary);
               
    }
	
}
