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
import java.awt.Rectangle;

import junit.framework.*;

public class Test4a extends Test {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(Test4a.class);
    }

    public void testGroupBounds() {
        assertEquals(g.getBounds(), FigureElement.MAX_BOUNDS);
    }
}
