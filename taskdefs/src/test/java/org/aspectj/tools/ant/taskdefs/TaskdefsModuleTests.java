/* *******************************************************************
 * Copyright (c) 1999-2001 Xerox Corporation, 
 *               2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 * ******************************************************************/
package org.aspectj.tools.ant.taskdefs;

import junit.framework.*;

public class TaskdefsModuleTests extends TestCase {

    public static TestSuite suite() { 
        TestSuite suite = new TestSuite(TaskdefsModuleTests.class.getName());
        suite.addTestSuite(Ajc11CompilerAdapterTest.class); 
        suite.addTestSuite(AjdocTest.class); 
        suite.addTestSuite(AjcTaskTest.class); 
        return suite;
    }

    public TaskdefsModuleTests(String name) { super(name); }
}  
