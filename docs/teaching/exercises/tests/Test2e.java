/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Common Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     PARC     initial implementation
 * ******************************************************************/

package tests;

import figures.*;

import junit.framework.*;

public class Test2e extends TestCase {
    public Test2e(String name) { super(name); }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(Test.class);
        junit.textui.TestRunner.run(Test2e.class);
    }

    public void testSloth() {
        Point sp = new SlothfulPoint(10, 10);
        try {
            sp.move(10, 10);
            fail("should have thrown IllegalStateException");
        } catch (IllegalStateException e) { }
    }

    public void testNonSloth() {
	Point p1 = new Point(10, 100);
        p1.move(3, 30);
    }
}
