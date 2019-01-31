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

import junit.framework.Test;
import junit.framework.TestCase;

/**
 * Alias to maintain convention for invoking JUnit from Ant
 * by discovery of module root tests that name is *ModuleTests.
 */
public class AllModuleTests extends TestCase {
    public static Test suite() {
        // does not include compiler tests, i.e., tests/../TestsModuleTests
        return AllTests.suite();
    }

}
