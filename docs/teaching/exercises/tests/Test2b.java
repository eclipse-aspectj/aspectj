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

public class Test2b extends CoreTest {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(Test2b.class);
    }

    public void testNull() {
        Point p1 = new Point(10, 100);
        Group g  = new Group(p1);

        try {
            g.add(null);
            fail("should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException ea) {
        }
    }

    public void testNonNull() {
        Point p1 = new Point(10, 100);
        Group g  = new Group(p1);
        Point p2 = new Point(20, 200);

        g.add(p2);
    }

}
