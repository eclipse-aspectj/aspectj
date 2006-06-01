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

import junit.framework.*;

public class Test2e extends CoreTest {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(Test2e.class);
    }

    public void testNonMoving() {
        Point sp = new Point(10, 10) { public void move(int x, int y) {} };
        try {
            sp.move(10, 10);
            fail("should have thrown IllegalStateException");
        } catch (IllegalStateException e) { }
    }

    public void testMoving() {
        Point p1 = new Point(10, 100);
        p1.move(3, 30);
    }
}
