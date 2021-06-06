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

public class Test2c extends CoreTest {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(Test2c.class);
    }

    public void testSelf() {
        Point p1 = new Point(10, 100);
        Group g  = new Group(p1);

        try {
            g.add(g);
            fail("should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException ea) {
        }
    }

    public void testNotSelf() {
        Point p1 = new Point(10, 100);
        Group g1 = new Group(p1);
        Group g2 = new Group(p1);

        g1.add(g2);
    }
}
