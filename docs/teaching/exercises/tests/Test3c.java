/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     PARC     initial implementation
 * ******************************************************************/

package tests;

import figures.*;
import support.Log;
import junit.framework.*;
import java.util.List;
import java.util.Arrays;

public class Test3c extends CoreTest {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(Test3c.class);
    }

    public void testCreateWithPointLog() {
        Log.clear();
        Point p1 = new Point(10, 100);
        Group g  = new Group(p1);
        List foundLog = Log.getData();

        List desiredLog =
            Arrays.asList(new String[] {
                "adding Point"
            });
        assertEquals(desiredLog, foundLog);
    }

    public void testCreateWithoutPointLog() {
        Log.clear();
        Point p1 = new Point(10, 100);
        Point p2 = new Point(20, 200);
        Line  l  = new Line(p1, p2);
        Group g  = new Group(l);
        List foundLog = Log.getData();

        List desiredLog = Arrays.asList(new String[] {});
        assertEquals(desiredLog, foundLog);
    }
}

