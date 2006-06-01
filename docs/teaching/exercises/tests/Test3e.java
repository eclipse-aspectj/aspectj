/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

