/* *******************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Wes Isberg       initial implementation 
 * ******************************************************************/

package org.aspectj.lib.tracing;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class TracingTests extends TestCase {

    public static Test suite() { 
        TestSuite suite = new TestSuite(TracingTests.class.getName());
        //$JUnit-BEGIN$
        suite.addTestSuite(TraceJoinPointsTest.class); 
        //$JUnit-END$
        return suite;
    }

}  
