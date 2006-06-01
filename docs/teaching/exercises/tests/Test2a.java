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

public class Test2a extends CoreTest {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(Test2a.class);
    }

    public void testTooSmall() {
        Point p1 = new Point(10, 100);
        try {
            p1.setX(-10);
            fail("should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException ea) {
        }
    }

    public void testNotTooSmall() {
        Point p1 = new Point(10, 100);
        p1.setX(0);
    }

    public void testMove() {
        Line l1 = new Line(new Point(10, 100), new Point(20, 200));
        try {
            l1.move(-500, -500);
            fail("should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException ea) {
        }
    }
}
