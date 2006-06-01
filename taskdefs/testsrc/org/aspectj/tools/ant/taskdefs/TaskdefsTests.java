/* *******************************************************************
 * Copyright (c) 1999-2001 Xerox Corporation, 
 *               2002 Palo Alto Research Center, Incorporated (PARC),
 *               2003 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 *     Wes Isberg     some 2003 tests
 * ******************************************************************/


package org.aspectj.tools.ant.taskdefs;

import junit.framework.*;

public class TaskdefsTests extends TestCase {

    public static Test suite() { 
        TestSuite suite = new TestSuite(TaskdefsTests.class.getName());
        //$JUnit-BEGIN$        
        suite.addTestSuite(Ajc11CompilerAdapterTest.class); 
        suite.addTestSuite(AjdocTest.class); 
        suite.addTestSuite(AjcTaskTest.class); 
        //$JUnit-END$
        return suite;
    }

    public TaskdefsTests(String name) { super(name); }

}  
