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

public class Test1a extends Test {
    public Test1a(String name) { super(name); }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(Test1a.class);
    }

    public void setUp() {
        Log.clear();
        super.setUp();
    }

    public void testCreateLog() {
        assertEquals("", Log.getString());
    }

    public void testSetXPointLog() {
	p1.setX(20);
        assertEquals("set;", Log.getString());
    }

    public void testSetYPointLog() {
	p1.setY(10);
        assertEquals("", Log.getString());
    }

    public void testGetYPointLog() {
	p1.getY();
        assertEquals("", Log.getString());
    }

    public void testMoveLineLog() {
	l1.move(40, 40);
        assertEquals("", Log.getString());
    }
}
