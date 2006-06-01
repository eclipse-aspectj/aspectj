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
import java.awt.Rectangle;

import junit.framework.*;

public class Test4c extends CoreTest {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(Test4c.class);
    }

    public void testBasicEquality() {
        assertTrue(g.getBounds() == g.getBounds());
    }

    public void testEqualityAfterAddition() {
        Point p0 = new Point(1, 2);
        Point p1 = new Point(2, 3);

        Group g0 = new Group(p0);
        Group g1 = new Group(p1);

        Rectangle r0 = g0.getBounds();
        Rectangle r1 = g1.getBounds();
        assertTrue(r0 != r1);
        g0.add(new Point(37, 90));
        assertTrue(g0.getBounds() == r0);
        assertTrue(g1.getBounds() != r0);
        assertTrue(g0.getBounds() != r1);
        assertTrue(g1.getBounds() == r1);

        g1.add(new Point(4, 8));
        assertTrue(g0.getBounds() == r0);
        assertTrue(g1.getBounds() != r0);
        assertTrue(g0.getBounds() != r1);
        assertTrue(g1.getBounds() == r1);
    }

    public void testEqualityAfterMove() {
        Point p0 = new Point(1, 2);
        Point p1 = new Point(2, 3);

        Group g0 = new Group(p0);
        Group g1 = new Group(p1);

        Rectangle r0 = g0.getBounds();
        Rectangle r1 = g1.getBounds();
        assertTrue(r0 != r1);
        assertTrue(g0.getBounds() == r0);
        assertTrue(g1.getBounds() != r0);
        assertTrue(g0.getBounds() != r1);
        assertTrue(g1.getBounds() == r1);

        g0.move(3, 1);
        Rectangle r00 = g0.getBounds();
        Rectangle r10 = g1.getBounds();

        assertTrue(r10 != r00);
        assertTrue(r0 != r00);
        assertTrue(g0.getBounds() == r00);
    }
}


