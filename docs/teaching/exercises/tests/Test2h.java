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

public class Test2h extends Test {
    public Test2h(String name) { super(name); }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(Test2h.class);
    }

    public void setUp() {
        super.setUp();
    }

    public void testSloth() {
        FigureElement fe = new SlothfulPoint(10, 10);
        try {
            fe.move(10, 10);
            fail("should have thrown IllegalStateException");
        } catch (IllegalStateException e) { }
    }

    public void movePoints() {
        p1.move(30, 45);
        p2.move(10, 33);
    }
}
