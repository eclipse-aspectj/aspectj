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

import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Commandline.Argument;
import org.aspectj.internal.tools.ant.taskdefs.BuildModule;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import junit.framework.TestCase;
/**
 * 
 */
public class BuildModuleTest extends TestCase {

    private static final String SKIP_MESSAGE = 
        "Define \"run.build.tests\" as a system property to run tests to build ";
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
                if (!BuildModuleTest.delete(files[i])
                    && result) {
                    result = false;
                }
            }
            return (file.delete() && result);
        }
    }

    ArrayList tempFiles = new ArrayList();
    boolean building;  // must be enabled for tests to run
    
	public BuildModuleTest(String name) {
		super(name);
        building = (null != System.getProperty("run.build.tests"));
	}

	protected void tearDown() throws Exception {
		super.tearDown();
        for (Iterator iter = tempFiles.iterator(); iter.hasNext();) {
			File file = (File) iter.next();
            if (!BuildModuleTest.delete(file)) {
                System.err.println("warning: BuildModuleTest unable to delete " + file);
            }
		}
	}
    
    public void testBuild() {
        checkBuild("build", null, null);
    }
    public void testAsm() {
        checkBuild("asm", null, null);
    }
    
    public void testRuntime() {
        checkBuild("runtime", null, null);
    }

    public void testAjbrowser() {
        checkBuild("ajbrowser", null, null);
    }

    public void testAjdt() {
        checkBuild("org.aspectj.ajdt.core", "org.aspectj.tools.ajc.Main",
            new String[] { "-version" });
    }
    
    public void testAspectjtools() {
        if (!building) {
            System.err.println(SKIP_MESSAGE + "aspectjtools");
            return;
        }
        File baseDir = new File("..");
        File tempBuildDir = new File(baseDir, "aj-build");
        File distDir = new File(tempBuildDir, "dist");
        File jarDir = new File(tempBuildDir, "jars");
        assertTrue(distDir.canWrite() || distDir.mkdirs());
        File productDir = new File(baseDir.getPath() + "/build/products/tools");
        assertTrue(""+productDir, productDir.canRead());
        checkBuildProduct(productDir, baseDir, distDir, jarDir);
    }

    void checkBuildProduct(File productDir, File baseDir, File distDir, File jarDir) {
        if (!building) {
            System.err.println(SKIP_MESSAGE + "product " + productDir);
            return;
        }
        assertTrue(null != productDir);
        assertTrue(productDir.canRead());

        checkJavac();

        BuildModule task = new BuildModule();
        Project project = new Project();
        task.setProject(project);
        assertTrue(jarDir.canWrite() || jarDir.mkdirs());
        // XXX restore tempFiles.add(jarDir);
        task.setJardir(new Path(project, jarDir.getAbsolutePath()));
        task.setProductdir(new Path(project, productDir.getAbsolutePath()));
        task.setBasedir(new Path(project, baseDir.getAbsolutePath()));
        task.setDistdir(new Path(project, distDir.getAbsolutePath()));
        task.setFailonerror(true);
        //task.setVerbose(true);
        task.setCreateinstaller(true);
        task.execute();
        // now run installer and do product tests?
    }

    void checkBuild(String module, String classname, String[] args) {
        if (!building) {
            System.err.println(SKIP_MESSAGE + "module " + module);
            return;
        }
        assertTrue(null != module);
        checkJavac();

        BuildModule task = new BuildModule();
        Project project = new Project();
        task.setProject(project);
        File jarDir = new File("tempJarDir");
        assertTrue(jarDir.canWrite() || jarDir.mkdirs());
        tempFiles.add(jarDir);
        task.setModuledir(new Path(project, "../" + module));
        task.setJardir(new Path(project, jarDir.getAbsolutePath()));
        task.setFailonerror(true);
        task.execute();
        
        if (null == classname) {
            return;
        }
        
        Java java = new Java();
        java.setProject(project);
        File jar = new File(jarDir, module + ".jar");
        java.setClasspath(new Path(project, jar.getAbsolutePath()));
        java.setClassname(classname);
        for (int i = 0; i < args.length; i++) {
            Argument arg = java.createArg();
            arg.setValue(args[i]);
		}
        java.execute();
    }

    void checkJavac() {
        boolean result = false;
        try {
            result = (null != Class.forName("sun.tools.javac.Main"));
        } catch (Throwable t) {
            // ignore
        }
        if (! result) {
            assertTrue("add tools.jar to the classpath for Ant's use of javac", false);
        }
    }

}
