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

import org.apache.tools.ant.*;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Commandline.Argument;
import org.aspectj.internal.tools.ant.taskdefs.*;
import org.aspectj.internal.tools.ant.taskdefs.BuildModule;
import org.aspectj.internal.tools.build.*;
import org.aspectj.internal.tools.build.Modules;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import junit.framework.*;
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
    private File jarDir;
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
        checkBuild("build", 
            Checklics.class.getName(), 
            new String[0], // help message
            true); // ant needed
    }
    
    public void testAsm() {
        checkBuild("asm");
    }
    
    public void testRuntime() {
        checkBuild("runtime");
    }

    public void testAjbrowser() {
        checkBuild("ajbrowser", 
            "org.aspectj.tools.ajbrowser.Main",
            new String[] {"-noExit", "-version"}); // compiler version
    }

    public void testAjdt() {
        checkBuild("org.aspectj.ajdt.core", 
           "org.aspectj.tools.ajc.Main",
            new String[] { "-noExit", "-version" });
    }
    public void testTestingDrivers() {
        checkBuild("testing-drivers", 
            "org.aspectj.testing.drivers.Harness", 
            new String[] {"-help"});
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

    public void testModuleAntecedantsClipped() {
        checkAntClipping("testing-drivers", true);
        checkAntClipping("taskdefs", false);
    }
    
    void checkAntClipping(String moduleName, boolean expectAnt) {
        File baseDir = new File("..").getAbsoluteFile();
        File jarDir = new File("../aj-build/jars").getAbsoluteFile();
        Messager handler = new Messager();
        Modules modules = new Modules(baseDir, jarDir, false, handler);
        Module td = modules.getModule(moduleName);
        ArrayList list = td.findKnownJarAntecedants();
        // should include ant
        boolean gotAnt = false;
        for (Iterator iter = list.iterator(); iter.hasNext();) {
            File lib = (File) iter.next();
            if (lib.getPath().endsWith("ant.jar")) {
                gotAnt = true;
                break;
            }
        }
        String label = (expectAnt ? "expected" : "not expecting")
                        + " ant in antecedants for " 
                        + moduleName;
        assertTrue(label, expectAnt == gotAnt);
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

    File getAntJar() {
        return new File("../lib/ant/lib/ant.jar");
    }
    
    File getJarDir() {
        if (null == jarDir) {
            jarDir = new File("tempJarDir");
            tempFiles.add(jarDir);
        }
        return jarDir;
    }
    
    BuildModule getTask(String module) {
        BuildModule task = new BuildModule();
        Project project = new Project();
        task.setProject(project);
        File jarDir = getJarDir();
        assertTrue(jarDir.canWrite() || jarDir.mkdirs());
        tempFiles.add(jarDir);
        task.setModuledir(new Path(project, "../" + module));
        task.setJardir(new Path(project, jarDir.getAbsolutePath()));
        return task;
    }
    
    void checkBuild(String module) { 
        checkBuild(module, null, null, false);
    }
    
    void checkBuild(String module, 
        String classname, 
        String[] args) {
        checkBuild(module, classname, args, false);
    }
    
    void checkBuild(String module, 
        String classname, 
        String[] args,
        boolean addAnt) {
        if (!building) {
            System.err.println(SKIP_MESSAGE + "module " + module);
            return;
        }
        assertTrue(null != module);
        checkJavac();
        
        // run without assembly
        BuildModule task = getTask(module);
        task.setAssembleall(false);
        task.execute();
        File jar = new File(getJarDir(), module + ".jar");
        assertTrue("cannot read " + jar, jar.canRead());
        assertTrue("cannot delete " + jar, jar.delete());

        // run with assembly
        task = getTask(module);
        task.setAssembleall(true);
        task.execute();
        jar = new File(getJarDir(), module + "-all.jar");
        assertTrue("cannot read " + jar, jar.canRead());
        
        // verify if possible
        if (null == classname) {
            return;
        }
        
        Java java = new Java();
        Project project = task.getProject();
        java.setProject(project);
        Path cp = new Path(project);
        cp.append(new Path(project, jar.getAbsolutePath()));
        if (addAnt) {
            cp.append(new Path(project, getAntJar().getAbsolutePath()));
        }
        java.setClasspath(cp);
        java.setClassname(classname);
        for (int i = 0; i < args.length; i++) {
            Argument arg = java.createArg();
            arg.setValue(args[i]);
		}
        java.setFailonerror(true);
        try {
            java.execute();
        } catch (BuildException e) {
            e.printStackTrace(System.err);
            assertTrue("BuildException running " + classname, false);
        }
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
