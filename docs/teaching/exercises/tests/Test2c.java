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

public class Test2c extends Test {
    public Test2c(String name) { super(name); }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(Test2c.class);
    }

    public void setUp() {
        super.setUp();
    }

    public void testNull() {
        try {
            g.add(null);
            fail("should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException ea) {
        }
    }

    public void testSelf() {
        try {
            g.add(g);
            fail("should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException ea) {
        }
    }
}
