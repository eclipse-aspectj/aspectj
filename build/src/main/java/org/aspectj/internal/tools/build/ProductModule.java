/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
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
 * Struct associating module with target product distribution jar
 * and assembly instructions.
 * When building product distributions, a zero-length jar file 
 * in the dist directory may signify a module to be built, 
 * renamed, and included in the distribution.
 */
public class ProductModule {
    /** name of distribution directory in product directory */
    private static final String DIST = "dist";
    
    /** top-level product directory being produced */
    public final File productDir;
    
    /** path to file in distribution template dir for this module jar */
    public final File replaceFile;
    
    /** relative path within distribution of this product module jar */
    public final String relativePath;
    
    /** the module jar is the file to replace */
    public final Module module;

    /** if true, assemble all when building module */
    public final boolean assembleAll;
    
    public ProductModule(File productDir, File replaceFile, Module module, boolean assembleAll) {
        this.replaceFile = replaceFile;
        this.module = module;
        this.productDir = productDir;
        this.assembleAll = assembleAll;
        Util.iaxIfNull(module, "module");
        Util.iaxIfNotCanReadDir(productDir, "productDir");
        Util.iaxIfNotCanReadFile(replaceFile, "replaceFile");
        String productDirPath = productDir.getAbsolutePath();
        String replaceFilePath = replaceFile.getAbsolutePath();
        if (!replaceFilePath.startsWith(productDirPath)) {
            String m = "\"" + replaceFilePath 
                + "\" does not start with \""
                + productDirPath
                + "\"";
            throw new IllegalArgumentException(m);
        }
        replaceFilePath = replaceFilePath.substring(1+productDirPath.length());
        if (!replaceFilePath.startsWith(DIST)) {
            String m = "\"" + replaceFilePath 
                + "\" does not start with \"" + DIST + "\"";
            throw new IllegalArgumentException(m);
        }
        relativePath = replaceFilePath.substring(1 + DIST.length());
    }
    public String toString() {
        return "" + module + " for " + productDir;
    }
}
