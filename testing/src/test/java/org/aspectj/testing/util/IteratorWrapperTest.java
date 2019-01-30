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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;

/**
 * 
 */
public class IteratorWrapperTest extends TestCase {

	/**
	 * Constructor for IteratorWrapperTest.
	 * @param name
	 */
	public IteratorWrapperTest(String name) {
		super(name);
	}

	public void testIteratorWrapper() {
        Object[][] exp = new Object[][] {};
        List[] in = new List[] {};
        checkIteratorWrapper(in, exp);

        in = new List[] {Collections.EMPTY_LIST};
        checkIteratorWrapper(in, exp);
        
        in = new List[] {Collections.EMPTY_LIST, Collections.EMPTY_LIST};
        checkIteratorWrapper(in, exp);
        
        Object[] ra1 = new Object[] { "1" };
        List one = Collections.unmodifiableList(Arrays.asList(ra1));
        in = new List[] {one};
        exp = new Object[][] { ra1 };
        checkIteratorWrapper(in, exp);

        in = new List[] {one, one};
        exp = new Object[][] { new Object[] { "1", "1"} };
        checkIteratorWrapper(in, exp);

        Object[] RA_ab = new String[] { "a", "b" };
        List List_ab = Collections.unmodifiableList(Arrays.asList(RA_ab));
        in = new List[] {List_ab};
        exp = new Object[][] { 
            new Object[] { "a" }, 
            new Object[] { "b" } 
            };
        checkIteratorWrapper(in, exp);

        in = new List[] {one, List_ab};
        exp = new Object[][] { 
            new Object[] { "1", "a" }, 
            new Object[] { "1", "b" },
            };
        checkIteratorWrapper(in, exp);

        Object[] RA_cd = new String[] { "c", "d" };
        List List_cd = Collections.unmodifiableList(Arrays.asList(RA_cd));
        
        in = new List[] {List_ab, List_cd};
        exp = new Object[][] { 
            new Object[] { "a", "c" }, 
            new Object[] { "b", "c" }, 
            new Object[] { "a", "d" }, 
            new Object[] { "b", "d" }
            };
        checkIteratorWrapper(in, exp);
        
        in = new List[] {one, one, one};
        exp = new Object[][] { 
            new Object[] { "1", "1", "1" } 
            };
        checkIteratorWrapper(in, exp);
        
        in = new List[] {List_ab, List_ab, List_ab};
        exp = new Object[][] { 
            new Object[] { "a", "a", "a" }, 
            new Object[] { "b", "a", "a" }, 
            new Object[] { "a", "b", "a" }, 
            new Object[] { "b", "b", "a" }, 
            new Object[] { "a", "a", "b" },
            new Object[] { "b", "a", "b" },
            new Object[] { "a", "b", "b" },
            new Object[] { "b", "b", "b" }
            };
        checkIteratorWrapper(in, exp);
        
        in = new List[] {one, List_ab, List_ab};
        exp = new Object[][] { 
            new Object[] { "1", "a", "a" }, 
            new Object[] { "1", "b", "a" },
            new Object[] { "1", "a", "b" }, 
            new Object[] { "1", "b", "b" },
            };
        checkIteratorWrapper(in, exp);       
        
        in = new List[] {one, List_ab, one};
        exp = new Object[][] { 
            new Object[] { "1", "a", "1" }, 
            new Object[] { "1", "b", "1" }
            };
        checkIteratorWrapper(in, exp);
        
        in = new List[] {List_ab, one, List_ab};
        exp = new Object[][] { 
            new Object[] { "a", "1", "a" }, 
            new Object[] { "b", "1", "a" },
            new Object[] { "a", "1", "b" }, 
            new Object[] { "b", "1", "b" }
            };
        checkIteratorWrapper(in, exp);
        
        in = new List[] {List_ab, one, List_ab, List_ab, Collections.EMPTY_LIST};
        exp = new Object[][] {};
        checkIteratorWrapper(in, exp);
        
    }

    void checkIteratorWrapper(List[] lists, Object[][] exp) {
        IteratorWrapper it = new IteratorWrapper(lists);
        for (int i = 0; i < exp.length; i++) {
			Object[] e = exp[i];
            if (!it.hasNext()) {
                String s = "exp[" + i + "]: " + Arrays.asList(e) + " it=" + it;
                assertTrue(s, false);
            }
            Object[] actual = (Object[]) it.next();
            checkEquals(e, actual, i);
		}
        if (it.hasNext()) {
            String s = "> " + exp.length + " it=" + it;
            assertTrue(s, false);
        }
    }

    void checkEquals(Object[] exp, Object[] actual, int index) {
        if (null == exp) {
            assertTrue(null == actual);
        } else {
            assertTrue(null != actual);
        }
        String s = "] exp=" + Arrays.asList(exp) + " act=" + Arrays.asList(actual);
        assertTrue(s, exp.length == actual.length);
        for (int i = 0; i < actual.length; i++) {
			assertTrue(null != exp[i]);
            assertTrue("[" + index + ", " + i + s, exp[i].equals(actual[i]));
		}
    }
}
