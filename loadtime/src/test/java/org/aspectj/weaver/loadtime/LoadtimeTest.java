/* *******************************************************************
 * Copyright (c) 2005-2019 Contributors.
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
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
