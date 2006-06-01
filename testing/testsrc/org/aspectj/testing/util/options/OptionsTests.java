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


package org.aspectj.testing.util.options;

import junit.framework.*;

public class OptionsTests extends TestCase {

    public static Test suite() { 
        TestSuite suite = new TestSuite(OptionsTests.class.getName());
        //$JUnit-BEGIN$
        suite.addTestSuite(OptionsTest.class); 
        //$JUnit-END$
        return suite;
    }

    public OptionsTests(String name) { super(name); }

}  
