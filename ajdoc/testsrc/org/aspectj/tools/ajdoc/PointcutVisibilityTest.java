/* *******************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Mik Kersten     initial implementation 
 * ******************************************************************/
package org.aspectj.tools.ajdoc;

import java.io.File;
import java.util.List;

/**
 * @author Mik Kersten
 */
public class PointcutVisibilityTest extends AjdocTestCase {
    
	/**
	 * Test that passing the "public" argument only shows
	 * public pointcuts in the ajdoc
	 */
    public void testCoveragePublicMode() throws Exception {
    	initialiseProject("bug82340");
    	File[] files = {new File(getAbsoluteProjectDir() + File.separatorChar + "Pointcuts.java")};
    	runAjdoc("public",files);
        
        // ajdoc for Pointcut.java should contain info about
        // the public pointcuts but not the protected and
        // private one (since "public" was an argument)
        // Check that this is the case......
        File htmlFile = new File(getAbsolutePathOutdir() + "/foo/Pointcuts.html");
		if (!htmlFile.exists()) {
			fail("couldn't find " + htmlFile.getAbsolutePath() + " - were there compilation errors?");
		}
		// check the contents of the pointcut summary
		String[] strings = { "privatePointcut","protectedPointcut","publicPointcut"};
		List missing = AjdocOutputChecker.getMissingStringsInSection(htmlFile,strings,"POINTCUT SUMMARY");
		assertEquals("There should be two missing strings",2,missing.size());
		assertTrue("passing the 'public' argument means the private pointcut shouldn't appear in the ajdoc", missing.contains("privatePointcut"));
		assertTrue("passing the 'public' argument means the protected pointcut shouldn't appear in the ajdoc", missing.contains("protectedPointcut"));
    }
    
    /**
     * Test that passing the "protected" argument only shows
     * public and protected pointcuts in the ajdoc 
     */
    public void testCoverageProtectedMode() throws Exception {
    	initialiseProject("bug82340");
    	File[] files = {new File(getAbsoluteProjectDir() + File.separatorChar + "Pointcuts.java")};
    	runAjdoc("protected",files);
        
        // ajdoc for Pointcut.java should contain info about
        // the public and protected pointcuts but not the 
        // private one (since "protected" was an argument)
        // Check that this is the case......
        File htmlFile = new File(getAbsolutePathOutdir() + "/foo/Pointcuts.html");
		if (!htmlFile.exists()) {
			fail("couldn't find " + htmlFile.getAbsolutePath() + " - were there compilation errors?");
		}
		// check the contents of the pointcut summary
		String[] strings = { "privatePointcut","protectedPointcut","publicPointcut"};
		List missing = AjdocOutputChecker.getMissingStringsInSection(htmlFile,strings,"POINTCUT SUMMARY");
		assertEquals("There should be one missing strings",1,missing.size());
		assertEquals("passing the 'protected' argument means the private pointcut shouldn't appear in the ajdoc", 
				"privatePointcut", missing.get(0));
    }
    
    /**
     * Test that passing the "private" argument shows all
     * pointcuts (public, protected and private) in the ajdoc 
     */
    public void testCoveragePrivateMode() throws Exception {
    	initialiseProject("bug82340");
    	File[] files = {new File(getAbsoluteProjectDir() + File.separatorChar + "Pointcuts.java")};
    	runAjdoc("private",files);
        
        // ajdoc for Pointcut.java should contain info about
        // the public, protected and private pointcuts 
    	// (since "private" was an argument)
        // Check that this is the case......
        File htmlFile = new File(getAbsolutePathOutdir() + "/foo/Pointcuts.html");
		if (!htmlFile.exists()) {
			fail("couldn't find " + htmlFile.getAbsolutePath() + " - were there compilation errors?");
		}
		// check the contents of the pointcut summary
		String[] strings = { "privatePointcut","protectedPointcut","publicPointcut"};
		List missing = AjdocOutputChecker.getMissingStringsInSection(htmlFile,strings,"POINTCUT SUMMARY");
		assertTrue("passing the 'private' modifier means that private, protected and public " +
				"pointcuts should appear in the ajdoc",missing.isEmpty());
    }
    
}
