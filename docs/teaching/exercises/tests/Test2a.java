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

public class Test2a extends Test {
    public Test2a(String name) { super(name); }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(Test2a.class);
    }

    public void setUp() {
        super.setUp();
    }

    public void testTooSmall() {
        try {
            p1.setX(-10);
            fail("should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException ea) {
        }
    }


    public void testTooBig() {
        try {
            p1.setY(1000);
            fail("should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException ea) {
        }
    }


    public void testMove() {
        try {
            l1.move(-500, -500);
            fail("should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException ea) {
        }
    }
}
