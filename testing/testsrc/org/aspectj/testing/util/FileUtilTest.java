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

package org.aspectj.testing.util;

import junit.framework.TestCase;

/**
 * 
 */
public class FileUtilTest extends TestCase {

	/**
	 * Constructor for FileUtilTest.
	 * @param arg0
	 */
	public FileUtilTest(String arg0) {
		super(arg0);
	}

    public void testFileEquals() {
        // File.equals(..) is based on lexical compare of filenames
//        File rf = new File("testsrc/org/aspectj/testing/util/FileUtilTest.java");
//        File rb = new File("testsrc\\org\\aspectj\\testing\\util\\FileUtilTest.java");
//        String a = rf.getAbsolutePath().replace('\\', '/');
//        File af = new File(a);
//        File ab = new File(a.replace('/', '\\'));
//        list.add(af);
//        list.add(ab);
//        list.add(rb);
//        assertTrue(list.contains(duplicateTwo));
//        assertTrue(list.contains(anotherOne));
    }
}
