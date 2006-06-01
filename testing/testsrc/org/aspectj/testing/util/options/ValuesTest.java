/* *******************************************************************
 * Copyright (c) 2003 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Wes Isberg     initial implementation 
 * ******************************************************************/

package org.aspectj.testing.util.options;

import junit.framework.TestCase;

/**
 */
public class ValuesTest extends TestCase {

    public ValuesTest(String s) {
        super(s);
    }
    public void testInvert() {
        checkInvert(new int[0], 0, new int[0]);  // no input or missed => none found
        checkInvert(new int[0], 1, new int[] {0});  // no missed, input 1 => 1 found
        checkInvert(new int[] {0}, 1, new int[] {}); // 1 (all) missed, input 1 => none found
        checkInvert(new int[] {}, 1, new int[] {0}); // 0 (none) missed, input 1 => 1 found
        checkInvert(new int[] {1,2}, 3, new int[] {0}); // 2 missed, input 3 => 1 (first) found
        checkInvert(new int[] {0,2}, 3, new int[] {1}); // 2 missed, input 3 => 1 (middle) found
        checkInvert(new int[] {0,1}, 3, new int[] {2}); // 2 missed, input 3 => 1 (last) found
        checkInvert(new int[] {1,3}, 4, new int[] {0,2}); // 2 missed, input 4 => 2 found
        checkInvert(new int[] {5,6,7}, 8, new int[] {0,1,2,3,4}); // starting run
        checkInvert(new int[] {0,1,2,3,4}, 8, new int[] {5,6,7}); // ending run
        checkInvert(new int[] {0,5,6,7}, 8, new int[] {1,2,3,4}); // middle run
        checkInvert(new int[] {0,5,6,9},10, new int[] {1,2,3,4,7,8}); // two middle run
        checkInvert(new int[] {1,2,5,6,9},10, new int[] {0,3,4,7,8}); // start, 2 middle run
        checkInvert(new int[] {0,1,2,5,6},10, new int[] {3,4,7,8,9}); // middle, end run
    }
    
    void checkInvert(int[] missed, int length, int[] expected) {
        int[] actual = Values.invert(missed, length);
        assertTrue(null != actual);
        assertTrue(actual.length == expected.length);
        for (int i = 0; i < actual.length; i++) {
            if (expected[i] != actual[i]) {
                assertTrue("failed at " + i + render(expected, actual), false);
            }
        }
    }
    static String render(int[] expected, int[] actual) {
        StringBuffer sb = new StringBuffer();
        sb.append(" expected ");
        render(expected, sb);
        sb.append(" actual ");
        render(actual, sb);
        return sb.toString();
    }
    static void render(int[] ra, StringBuffer sb) {
        sb.append("[");
        for (int i = 0; i < ra.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append("" + ra[i]);
        }
        sb.append("]");
    }
}
