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

public class Test3b extends CoreTest {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(Test3b.class);
    }

    public void testMovePointLog() {
        Point p1 = new Point(10, 100);

        Log.clear();
        p1.move(20, 30);
        List foundLog = Log.getData();

        List desiredLog =
            Arrays.asList(new String[] {
                "execution(void figures.Point.move(int, int)) at Point(10, 100)"
            });

        assertEquals(desiredLog, foundLog);
    }

    public void testMoveLineLog() {
        Point p1 = new Point(10, 100);
        Point p2 = new Point(20, 200);
        Line  l  = new Line(p1, p2);

        Log.clear();
        l.move(20, 30);
        List foundLog = Log.getData();

        List desiredLog =
            Arrays.asList(new String[] {
                "execution(void figures.Line.move(int, int)) at Line(Point(10, 100), Point(20, 200))",
                "execution(void figures.Point.move(int, int)) at Point(10, 100)",
                "execution(void figures.Point.move(int, int)) at Point(20, 200)"
            });
        assertEquals(desiredLog, foundLog);
    }
}
