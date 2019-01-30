package org.aspectj.tools.ajdoc;
/* *******************************************************************
 * Copyright (c) 2003 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Mik Kersten     initial implementation 
 * ******************************************************************/

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * @author Mik Kersten
 */
public class AjdocModuleTests {
    public static Test suite() {
        TestSuite suite = new TestSuite(AjdocModuleTests.class.getName());
        suite.addTest(AjdocTests.suite());
        return suite;
    }
}
