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

public class Test2d extends CoreTest {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(Test2d.class);
    }

    public void testOutOfBounds() {
        Point p1 = new Point(10, 100);

        p1.setX(-10);
        p1.setY(-100);

        assertEquals(0, p1.getX());
        assertEquals(0, p1.getY());
    }

    public void testInBounds() {
        Point p1 = new Point(10, 100);

        p1.setX(30);
        p1.setY(300);

        assertEquals(30, p1.getX());
        assertEquals(300, p1.getY());
    }
}
