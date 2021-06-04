/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors:
 *     PARC     initial implementation
 * ******************************************************************/

package tests;

import figures.*;
import support.Log;
import junit.framework.*;
import java.util.List;
import java.util.Arrays;

public class Test3e extends CoreTest {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(Test3e.class);
    }

    public void testDuplicateAdd() {
        Log.clear();
        Point p1 = new Point(10, 100);
        Group g1  = new Group(p1);
	try {
	    Group g2  = new Group(p1);
            fail("should have thrown IllegalStateException");
        } catch (IllegalStateException ea) {
	    assertEquals(g1.toString(), ea.getMessage());
        }
    }
}

