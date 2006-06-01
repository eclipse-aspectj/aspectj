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

public class CoreTest extends TestCase {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(Test.class);
    }

    Box bb;
    Point p1;
    Point p2;
    Line l1;
    SlothfulPoint sloth1;
    Group g;

    public void setUp() {
        p1 = new Point(10, 100);
        p2 = new Point(20, 200);
        l1 = new Line(p1, p2);
        bb = new Box(5, 5, 10, 10);
        sloth1 = new SlothfulPoint(0, 0);
        g = new Group(p1);
    }

    public final void testCreate() {
        assertEquals(p1.getX(), 10);
        assertEquals(p1.getY(), 100);

        assertEquals(l1.getP1(), p1);
        assertEquals(l1.getP2(), p2);
    }

    public final void testSetPoint() {
        p1.setX(20);
        assertEquals(p1.getX(), 20);
        assertEquals(p1.getY(), 100);

        p1.setY(10);
        assertEquals(p1.getX(), 20);
        assertEquals(p1.getY(), 10);
    }

    public final void testMoveLine1() {
        l1.move(40, 40);
        assertEquals(l1.getP1(), p1);
        assertEquals(l1.getP2(), p2);

        assertEquals(p1.getX(), 50);
        assertEquals(p1.getY(), 140);

        assertEquals(p2.getX(), 60);
        assertEquals(p2.getY(), 240);
    }

    public final void testMoveLine2() {
        l1.move(-10, -10);
        assertEquals(p1.getX(), 0);
        assertEquals(p1.getY(), 90);

        assertEquals(p2.getX(), 10);
        assertEquals(p2.getY(), 190);
    }
}
