/* *******************************************************************
 * Copyright (c) 2019 Contributors
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * ******************************************************************/
package org.aspectj.ajdt.ajc;

import java.io.File;
import org.aspectj.testing.util.TestUtil;

/**
 * @author Andy Clement
 */
public class Constants {

    public static final String TESTDATA_PATH = "../org.aspectj.ajdt.core/testdata";

    public static final File TESTDATA_DIR = new File(TESTDATA_PATH);
    
    public static String aspectjrtClasspath() {
        return TestUtil.aspectjrtPath().getPath();        
    }
  	
}  
