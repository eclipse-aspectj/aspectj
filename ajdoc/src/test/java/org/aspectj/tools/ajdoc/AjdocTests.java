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


package org.aspectj.tools.ajdoc;

import java.io.File;

import org.aspectj.util.FileUtil;

public class AjdocTests {//extends TestCase {
    
	public static File ASPECTJRT_PATH;
    
    static {
        String[] paths = { "sp:aspectjrt.path", "sp:aspectjrt.jar",
                "../lib/test/aspectjrt.jar", "../aj-build/jars/aspectj5rt-all.jar",
                "../aj-build/jars/runtime.jar",
                "../runtime/bin"};
        ASPECTJRT_PATH = FileUtil.getBestFile(paths);
    }

}
