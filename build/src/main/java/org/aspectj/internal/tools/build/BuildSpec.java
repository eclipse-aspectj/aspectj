/* *******************************************************************
 * Copyright (c) 1999-2001 Xerox Corporation, 
 *               2002 Palo Alto Research Center, Incorporated (PARC),
 *               2005 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 * ******************************************************************/

 
package org.aspectj.internal.tools.build;


import java.io.File;

/** 
 * Open struct for specifying builds for both modules and products.
 * Separated from bulder to permit this to build many modules
 * concurrently.
 */
public class BuildSpec {
    public static final String DEFAULT_VERSION = "DEVELOPMENT";
    // shared
    public File baseDir;
    public File moduleDir;
    public File jarDir;
    public File tempDir;
    public File stagingDir;
    public String buildConfig;   
    public String version;
    public boolean rebuild;
    public boolean trimTesting;
    public boolean assembleAll;
    public boolean failonerror;
    public boolean verbose;

    // building products
    public File productDir;
    public boolean createInstaller;
    public File distDir;
    
    // building modules
    public String module;
    public BuildSpec() {
        version = DEFAULT_VERSION;
    }
        
    public boolean isProduct() {
        return (Util.canReadDir(productDir));
    }

    public boolean isModule() {
        return (!isProduct() && Util.canReadDir(moduleDir));
    }

    public boolean isValid() {
        return (isProduct() || isModule());
    }
    
    public String toString() {
        if (null != productDir) {
            return "product " + productDir.getName();
        } else if (null != moduleDir) {
            return "module " + moduleDir.getName();
        } else {
            return "<bad BuildSpec - "
            	+ " baseDir=" + baseDir
            	+ " jarDir=" + jarDir
            	+ " buildConfig=" + buildConfig
                + " module=" + module
            	+ ">";
        }
    }
}

