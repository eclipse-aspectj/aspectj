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

public class Test2g extends Test {
    public Test2g(String name) { super(name); }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(Test2g.class);
    }

    public void setUp() {
        super.setUp();
    }

    public void testBounds() {
        p1.setX(FigureElement.MAX_VALUE + 1);
        assertEquals(FigureElement.MAX_VALUE, p1.getX());
        p1.setY(FigureElement.MIN_VALUE - 1);
        assertEquals(FigureElement.MIN_VALUE, p1.getY());
    }

    public void testBox() {
        Box s = new Box(50, 50, 20000, 20000);
        assertEquals(FigureElement.MAX_VALUE, s.getP2().getX());
        assertEquals(FigureElement.MAX_VALUE, s.getP2().getY());
    }
}
