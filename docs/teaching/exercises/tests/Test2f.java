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

public class Test2f extends Test {
    public Test2f(String name) { super(name); }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(Test2f.class);
    }

    public void setUp() {
        super.setUp();
    }

    public void testEasy() {
        Box sq = new Box(0, 0, 10, 10);
        sq.move(5,5);
        assertEquals(sq.getP0().getX(), 5);
        assertEquals(sq.getP0().getY(), 5);

        try {
            sq.getP0().setX(100);
            sq.getP1();
            fail("should have thrown IllegalStateException");
        } catch (IllegalStateException e) { }
    }
}
