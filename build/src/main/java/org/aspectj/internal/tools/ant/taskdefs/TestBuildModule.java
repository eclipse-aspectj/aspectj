/* *******************************************************************
 * Copyright (c) 1999-2001 Xerox Corporation, 
 *               2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 * ******************************************************************/

 
package org.aspectj.internal.tools.ant.taskdefs;

import java.io.File;
import java.util.Arrays;

import org.apache.tools.ant.Project;
import org.aspectj.internal.tools.build.BuildSpec;
import org.aspectj.internal.tools.build.Builder;
import org.aspectj.internal.tools.build.Util;

public class TestBuildModule {
//    private static boolean REBUILD = false;
    private static final String SYNTAX = "java {classname} <[product|module]dir>";
    public static void main(String[] args) {
        
        if ((null == args) || (1 > args.length)
            || !Util.canReadDir(new File(args[0]))) {
            System.err.println(SYNTAX);
            return;
        }
        File dir = new File(args[0]);
        // create a module
        if (Util.canReadDir(new File(dir, "dist"))) {
            createProduct(args);
        } else if (Util.canReadFile(new File(dir, ".classpath"))) {
            createModule(args);
        } else {
            System.err.println(SYNTAX);
        }
    }
    
    static void createModule(String[] args) {
        File moduleDir = new File(args[0]);
        File baseDir = moduleDir.getParentFile();
        if (null == baseDir) {
            baseDir = new File(".");
        }
        File jarDir = new File(baseDir, "aj-build-jars");
        if (!(Util.canReadDir(jarDir) || jarDir.mkdirs())) {
            System.err.println("createModule unable to create " + jarDir); 
            return;
        }
        
        // set module dir or basedir plus module name
        BuildSpec buildSpec = new BuildSpec();
        buildSpec.moduleDir = moduleDir;
        buildSpec.jarDir = jarDir;
        buildSpec.verbose = true;
        buildSpec.failonerror = true;
        buildSpec.trimTesting = true;
        buildSpec.rebuild = true;

        File tempDir = null;
        Project project = new Project();
        project.setProperty("verbose", "true");
        project.setName("TestBuildModule.createModule" + Arrays.asList(args));
        Builder builder = AntBuilder.getBuilder("", project, tempDir);
        builder.build(buildSpec);
    }
    
    static void createProduct(String[] args) {
        throw new Error("unimplemented");
    }
}        

