/* *******************************************************************
 * Copyright (c) 2004 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     Wes Isberg     initial implementation 
 * ******************************************************************/

package org.aspectj.tools.ajc;

import java.util.ArrayList;

import junit.framework.TestCase;

/**
 * 
 */
public class MainTest extends TestCase {

    public void testMainbare() {
        ArrayList list = new ArrayList();
        Main.bareMain(new String[] {"-help"}, false, list, null, null, null);
        assertTrue(1 == list.size());
        Object o = list.get(0);
        assertTrue(o instanceof String);
        assertTrue(-1 != ((String)o).indexOf("-aspectpath"));
        assertTrue(-1 != ((String)o).indexOf("-incremental"));
    }
}
