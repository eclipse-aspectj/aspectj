/* *******************************************************************
 * Copyright (c) 2005-2019 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 * ******************************************************************/
package org.aspectj.weaver.loadtime;

import java.lang.instrument.Instrumentation;

import junit.framework.TestCase;

/**
 * @author Andy Clement
 * @author Wes Isberg
 */
public class LoadtimeTest extends TestCase {

    public void testPremain() throws Exception {            
        Class<?>[] paramTypes = {String.class, Instrumentation.class };
        assertNotNull(Agent.class.getMethod("premain", paramTypes));
    }
}
