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

public class Test4e extends CoreTest {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(Test4e.class);
    }

    public void testBasicEquality() {
        assertTrue(g.getBounds() == g.getBounds());
    }

    public void testNonGroupMove() {
        p1.move(3, 27);
    }

    public void testEqualityAfterAddition() {
        Rectangle r = g.getBounds();
        g.add(new Point(37, 90));
        assertTrue(g.getBounds() == r);
    }

    public void testEqualityAfterMove() {
        g = new Group(p1);
        Rectangle r0 = g.getBounds();
        assertTrue(g.getBounds() == r0);
        p1.move(3, 1);
        Rectangle r1 = g.getBounds();
        assertTrue(r0 != r1);
        assertTrue(r1 == g.getBounds());
    }

    public void testSecondEnclosingGroup() {
        g = new Group(p1);
        Group h = new Group(g);
        Rectangle r0 = h.getBounds();
        assertTrue(h.getBounds() == r0);
        p1.move(3, 1);
        Rectangle r1 = h.getBounds();
        assertTrue(r0 != r1);
        assertTrue(r1 == h.getBounds());
    }
}


