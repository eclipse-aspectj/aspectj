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

public class Test3b extends Test {
    public Test3b(String name) { super(name); }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(Test3b.class);
    }

    public void setUp() {
        super.setUp();
        Log.clear();
    }

    public void testCreateLog() {
        assertEquals("", Log.getString());
    }

    public void testCreateWithPointLog() {
	g = new Group(p1);
        assertEquals("adding Point;", Log.getString());
    }

    public void testCreateWithoutPointLog() {
	g = new Group(l1);
        assertEquals("", Log.getString());
    }

    public void testAddPointLog() {
	g.add(p1);
        assertEquals("adding Point;", Log.getString());
    }
    public void testAddNonPointLog() {
	g.add(l1);
        assertEquals("", Log.getString());
    }
}
