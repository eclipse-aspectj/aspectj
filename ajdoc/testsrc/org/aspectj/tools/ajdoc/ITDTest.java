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

public class ITDTest extends TestCase {

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
    public void testITDShownInDoc() throws Exception {
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
        
        checkContentsOfC();
        checkContentsOfA();        

    }
    
    // check whether the "INTER-TYPE METHOD SUMMARY" AND
    // "INTER-TYPE FIELD SUMMARY" have been added to the generated 
    // html file for the class which is affected by these itds.
    // Also check that the correct mofifiers are showing ie. public
    // isn't there, but all others are (this mirrors javadoc behaviour)
    private void checkContentsOfC() throws Exception {
        File htmlC = new File("../ajdoc/testdata/pr119453/doc/pack/C.html");
        if (htmlC == null) {
			fail("couldn't find ../ajdoc/testdata/pr119453/doc/pack/C.html - were there compilation errors?");
		}
	    BufferedReader readerC = new BufferedReader(new FileReader(htmlC));
        boolean containsITDF = false;
        boolean containsITDM = false;
        boolean containsITDC = false;
        String lineC = readerC.readLine();
        while( lineC != null && (!containsITDF || !containsITDM || !containsITDC)) {
        	if (lineC.indexOf("INTER-TYPE METHOD SUMMARY") != -1) {
				containsITDM = true;
				boolean containsPublic = false;
				boolean containsString = false;
				boolean containsPackA = false;
				// walk through the information in this section
				String nextLine = readerC.readLine();
				while(nextLine != null && (nextLine.indexOf("========") == -1)) {
					if (nextLine.indexOf("public") != -1) {
						containsPublic = true;
					}
					if (nextLine.indexOf("String") != -1) {
						containsString = true;
					}
					if (nextLine.indexOf("pack.A") != -1) {
						containsPackA = true;
					}	
					nextLine = readerC.readLine();
				}
				assertFalse("inter-type method summary should not contain the 'public' modifier", containsPublic);
				assertTrue("inter-type method summary should contain the 'String' return type",containsString);
				assertTrue("inter-type method summary should contain declared by 'pack.A'", containsPackA);
				
				// we may have hit the "inter-type field summary" so set this to 
				// be the next line we look at.
				lineC = nextLine;
			} else if (lineC.indexOf("INTER-TYPE FIELD SUMMARY") != -1) {
				containsITDF = true;
				boolean containsPrivate = false;
				// walk through the information in this section
				String nextLine = readerC.readLine();
				while(nextLine != null 
						&& (nextLine.indexOf("========") == -1)
						&& !containsPrivate) {
					if (nextLine.indexOf("private") != -1) {
						containsPrivate = true;
					}
					nextLine = readerC.readLine();
				}
				assertTrue("inter-type field summary should contain the 'private' modifier",containsPrivate);
				
				// we may have hit the "inter-type field summary" so set this to 
				// be the next line we look at.
				lineC = nextLine;
			} else if (lineC.indexOf("NTER-TYPE CONSTRUCTOR SUMMARY") != -1) {
				// don't do any more checking here because have
				// checked in the itd method summary
				containsITDC = true;
			} else {
				lineC = readerC.readLine();
			}
        }
        readerC.close();
		
        assertTrue("should have put ITD Method information into " +
        		"../ajdoc/testdata/pr119453/doc/pack/C.html", containsITDM);
        assertTrue("should have put ITD Field information into " +
        		"../ajdoc/testdata/pr119453/doc/pack/C.html", containsITDF);
        assertTrue("should have put ITD Constructor information into " +
        		"../ajdoc/testdata/pr119453/doc/pack/C.html", containsITDC);

    }
    
    
    // check whether the correct modifiers have been added to the
    // declare summary and declare detail in the doc for the aspect
    private void checkContentsOfA() throws Exception {
        File htmlA = new File("../ajdoc/testdata/pr119453/doc/pack/A.html");
        if (htmlA == null) {
			fail("couldn't find ../ajdoc/testdata/pr119453/doc/pack/A.html - were there compilation errors?");
		}
	    BufferedReader readerA = new BufferedReader(new FileReader(htmlA));
        boolean containsDeclareDetail = false;
        boolean containsDeclareSummary = false;
        String lineA = readerA.readLine();
        while( lineA != null && (!containsDeclareDetail || !containsDeclareSummary )) {
        	if (lineA.indexOf("DECLARE DETAIL SUMMARY") != -1) {
				containsDeclareDetail = true;
				boolean containsPrivateInt = false;
				boolean containsPublicString = false;
				boolean containsITDFAsHeader = false;
				boolean containsCorrectConstInfo = false;
				boolean containsPackageVoid = false;
				// walk through the information in this section
				String nextLine = readerA.readLine();
				while(nextLine != null 
						&& (nextLine.indexOf("========") == -1)
						&& (!containsPrivateInt || !containsPublicString 
								|| !containsITDFAsHeader || !containsCorrectConstInfo)) {
					if (nextLine.indexOf("private&nbsp;int") != -1) {
						containsPrivateInt = true;
					} 
					if (nextLine.indexOf("public&nbsp;java.lang.String") != -1) {
						containsPublicString = true;
					} 
					if (nextLine.indexOf("<H3>C.y</H3>") != -1) {
						containsITDFAsHeader = true;
					} 
					if (nextLine.indexOf("public&nbsp;</TT><B>C.C") != -1 ) {
						containsCorrectConstInfo = true;
					}
					if (nextLine.indexOf("package&nbsp;void") != -1 ) {
						containsPackageVoid = true;
					}
					nextLine = readerA.readLine();
				}
				assertTrue("Declare detail summary should contain the 'private int' " +
						"modifiers", containsPrivateInt);
				assertTrue("Declare detail summary should contain the 'public java." +
						"lang.String' return type",containsPublicString);
				assertTrue("Declare detail summary should have 'C.y' as one header", 
						containsITDFAsHeader);
				assertTrue("Declare detail summary should have 'public C.C' for the " +
						"ITD constructor", containsCorrectConstInfo);
				assertFalse("Declare detail summary should not have 'package void' in it",
						containsPackageVoid);
				
				// we may have hit the "inter-type field summary" so set this to 
				// be the next line we look at.
				lineA = nextLine;
			} else if (lineA.indexOf("DECLARE SUMMARY") != -1) {
				containsDeclareSummary = true;
				boolean containsPrivate = false;
				boolean containsInt = false;
				boolean containsString = false;
				boolean containsPublic = false;
				boolean containsPackageVoid = false;
				// walk through the information in this section
				String nextLine = readerA.readLine();
				while(nextLine != null && (nextLine.indexOf("========") == -1)) {
					if (nextLine.indexOf("private") != -1) {
						containsPrivate = true;
					}
					if (nextLine.indexOf("int") != -1) {
						containsInt = true;
					}
					if (nextLine.indexOf("public") != -1) {
						containsPublic = true;
					}
					if (nextLine.indexOf("String") != -1) {
						containsString = true;
					}
					if (nextLine.indexOf("package&nbsp;void") != -1) {
						containsPackageVoid = true;
					}
					nextLine = readerA.readLine();
				}
				assertTrue("Declare summary should contain the 'private' modifier",containsPrivate);
				assertTrue("Declare summary should contain the 'int' return type",containsInt);
				assertFalse("Declare summary should not contain the 'public' modifier",containsPublic);
				assertTrue("Declare summary should contain the 'String' return type",containsString);
				assertFalse("Declare summary should not have 'package void' in it",
						containsPackageVoid);
				
				// we may have hit the "Declare Details" so set this to 
				// be the next line we look at.
				lineA = nextLine;
			} else {
				lineA = readerA.readLine();
			}
        }
        readerA.close();
		
        assertTrue("should have put Declare Detail information into " +
        		"../ajdoc/testdata/pr119453/doc/pack/A.html", containsDeclareDetail);
        assertTrue("should have put Declare Summary information into " +
        		"../ajdoc/testdata/pr119453/doc/pack/A.html", containsDeclareSummary);
               
    }
	
}
