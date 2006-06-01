/* *******************************************************************
 * Copyright (c) 2003 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Wes Isberg     initial implementation 
 * ******************************************************************/

package org.aspectj.testing.taskdefs;

import junit.framework.*;

public class TaskdefTests extends TestCase {

    public static Test suite() { 
        TestSuite suite = new TestSuite(TaskdefTests.class.getName());
        //$JUnit-BEGIN$
        suite.addTestSuite(AjcTaskCompileCommandTest.class); 
        //$JUnit-END$
        return suite;
    }

    public TaskdefTests(String name) { super(name); }

}  
