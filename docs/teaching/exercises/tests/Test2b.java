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

public class Test2b extends Test {
    public Test2b(String name) { super(name); }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(Test2b.class);
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
}
