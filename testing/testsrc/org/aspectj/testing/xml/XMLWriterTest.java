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

package org.aspectj.testing.xml;

import java.util.Arrays;

import junit.framework.TestCase;

/**
 * 
 */
public class XMLWriterTest extends TestCase {

	public XMLWriterTest(String name) {
		super(name);
	}

    /** @see LangUtilTest#testCommaSplit() */
    public void testUnflattenList() { 
        checkUnflattenList("", new String[] {""});
        checkUnflattenList("1", new String[] {"1"});
        checkUnflattenList(" 1 2 ", new String[] {"1 2"});
        checkUnflattenList(" 1 , 2 ", new String[] {"1", "2"});
        checkUnflattenList("1,2,3,4", new String[] {"1", "2", "3", "4"});
    }
    
    void checkUnflattenList(String input, String[] expected) {
        String[] actual = XMLWriter.unflattenList(input);
        String a = "" + Arrays.asList(actual);
        String e = "" + Arrays.asList(expected);
        assertTrue(e + "==" + a, e.equals(a));
    }   
}
