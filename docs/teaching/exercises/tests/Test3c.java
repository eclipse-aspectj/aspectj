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
import support.Log;

import junit.framework.*;

public class Test3c extends Test {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(Test3c.class);
    }

    public void setUp() {
        Log.clear();
        super.setUp();
    }

    public void testCreateLog() {
        assertEquals("", Log.getString());
    }

    public void testMoveLonePoint() {
        p1 = new Point(0, 0);
        p1.move(37, 88);
        assertEquals("moving as a part of null;", Log.getString());
    }

    public void testMoveGroupedPoint() {
        g = new Group(p1);
        p1.move(0, 0);
        assertEquals("moving as a part of " + g + ";", Log.getString());
    }
}
