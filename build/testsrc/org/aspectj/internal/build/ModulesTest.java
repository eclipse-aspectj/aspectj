/* *******************************************************************
 * Copyright (c) 1999-2001 Xerox Corporation, 
 *               2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 * ******************************************************************/

package org.aspectj.internal.build;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import junit.framework.TestCase;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Path;
import org.aspectj.internal.tools.ant.taskdefs.AntBuilder;
import org.aspectj.internal.tools.build.Messager;
import org.aspectj.internal.tools.build.Module;
import org.aspectj.internal.tools.build.Modules;
/**
 * 
 */
public class ModulesTest extends TestCase {

   private static boolean delete(File file) { // XXX Util
        if ((null == file) || !file.exists()) {
            return true;
        }
        if (file.isFile()) {
            return file.delete();
        } else {
            File[] files = file.listFiles();
            boolean result = true;
            for (int i = 0; i < files.length; i++) {
                if (!ModulesTest.delete(files[i])
                    && result) {
                    result = false;
                }
            }
            return (file.delete() && result);
        }
    }

    ArrayList tempFiles = new ArrayList();
     
	public ModulesTest(String name) {
		super(name);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
        for (Iterator iter = tempFiles.iterator(); iter.hasNext();) {
			File file = (File) iter.next();
            if (!ModulesTest.delete(file)) {
                System.err.println("warning: ModulesTest unable to delete " + file);
            }
		}
	}
    Modules getModules(Messager handler) {
        File jarDir = new File("../aj-build-test-jars");
        if (!jarDir.exists()) {
            assertTrue(jarDir.mkdirs());
            tempFiles.add(jarDir);
        }
        File baseDir = new File("..");
        if (null == handler) {
            handler = new Messager();
        }
        return new Modules(baseDir, jarDir, true, handler);
    }
    
    public void testModuleCreation() {
        Modules modules = getModules(null);
        Module eclipse = modules.getModule("org.eclipse.jdt.core");
        assertTrue(eclipse.valid);
    }

    public void testAllModulesCreation() {
        File baseDir = new File("..");
        File[] files = baseDir.listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {
                File classpath = new File(files[i], ".classpath");
                if (classpath.exists()) {
                    checkModule(files[i].getName());
                }
            }
        }
    }
    
    void checkModule(String s) {
        if ("docs".equals(s) || "lib".equals(s)) {
            return;
        }
        Modules modules = getModules(null);
        Module module = modules.getModule(s);
        if (!module.valid) {
            assertTrue(module.toString(), false);
        }
    }
    
    public void testClasspathCreation() {
        Modules modules = getModules(null);
        Module ajdt = modules.getModule("org.aspectj.ajdt.core");
        assertTrue(ajdt.valid);
        
        Project project = new Project();
        project.setBaseDir(new File("."));
        project.setName("testClasspathCreation");
        File tempDir = new File(".");
        AntBuilder builder = (AntBuilder) AntBuilder.getBuilder("", project, tempDir);
        Path classpath = new Path(project);
        boolean hasLibraries = builder.setupClasspath(ajdt, classpath);
        assertTrue(hasLibraries);
        if ((null == classpath) || (2 > classpath.size())) {
            assertTrue(classpath.toString(), false);
        }
    }
   

}
