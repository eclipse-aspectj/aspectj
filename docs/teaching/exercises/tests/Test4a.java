/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors:
 *     PARC     initial implementation
 * ******************************************************************/

package tests;

import figures.*;
import java.awt.Rectangle;

import junit.framework.*;

public class Test4a extends CoreTest {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(Test4a.class);
    }

    public void testGroupBounds() {
        assertEquals(g.getBounds(), FigureElement.MAX_BOUNDS);
    }
}
