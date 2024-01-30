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

import junit.framework.*;

public class Test2f extends CoreTest {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(Test2f.class);
    }

    public void testSloth() {
        FigureElement fe = new SlothfulPoint(10, 10);
        try {
            fe.move(10, 10);
            fail("should have thrown IllegalStateException");
        } catch (IllegalStateException e) { }
    }

    public void testNonSloth() {
        Point p1 = new Point(10, 100);
        Point p2 = new Point(20, 200);
        Line  l1 = new Line(p1, p2);

        l1.move(3, 30);
    }
}
