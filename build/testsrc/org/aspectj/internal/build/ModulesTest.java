/* *******************************************************************
 * Copyright (c) 1999-2001 Xerox Corporation, 
 *               2002 Palo Alto Research Center, Incorporated (PARC),
 *               2005 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 *     Wes Isberg     build tests
 * ******************************************************************/

package org.aspectj.internal.build;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import junit.framework.TestCase;

import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Path;
import org.aspectj.internal.tools.ant.taskdefs.AntBuilder;
import org.aspectj.internal.tools.ant.taskdefs.BuildModule;
import org.aspectj.internal.tools.build.Messager;
import org.aspectj.internal.tools.build.Module;
import org.aspectj.internal.tools.build.Modules;
import org.aspectj.internal.tools.build.Util;
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
        String[] knowns = {"runtime", "util", "weaver" };
        File[] files = new File[knowns.length];
        for (int i = 0; i < files.length; i++) {
            files[i] = new File(knowns[i]);
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

    
    /*********************************************************************
     * The following tests/code enable you to run the entire build in JUnit
     * to debug directly from Eclipse.  To compile using Javac, you will
     * need to add tools.jar to the run classpath.
     */
    public void skip_testBuildingAspectJModule() {
        final String moduleName = "org.aspectj.lib";
        
        File modulesDir = new File("..").getAbsoluteFile();
        File buildDir = new File(modulesDir, "aj-build");
        File distDir = new File(buildDir, "dist");
        File jarDir = new File(buildDir, "jars");
        File moduleDir = new File(modulesDir, moduleName);
        File jar = new File(jarDir, "org.aspectj.lib.jar");
        jarDir.mkdirs();
        distDir.mkdirs();
        if (jar.canRead()) {
            assertTrue(jar.delete());
        }
        Project project = new Project();
        project.setBaseDir(modulesDir);
        project.setName("testAspectjbuild");
        
        BuildModule bm = new BuildModule();
        bm.setProject(project);
        bm.setAssembleall(true);
        bm.setBuildConfig("");
        bm.setModule(moduleName);
        bm.setBasedir(new Path(project, modulesDir.getPath()));
        bm.setDistdir(new Path(project, buildDir.getPath()));
        bm.setJardir(new Path(project, jarDir.getPath()));
        bm.setModuledir(new Path(project, moduleDir.getPath()));
        bm.setTrimtesting(true);
        bm.setBuildConfig("");
        bm.setVersion("1.2");
        bm.setVerbose(true);
        bm.setFailonerror(true);
        
        bm.execute();
        
        assertTrue(jar.canRead());
    }
    
    public void skip_testBuildingProduct() {
        final String productName = "tools";
        File modulesDir = new File("..").getAbsoluteFile();
        File buildDir = new File(modulesDir, "aj-build");
        File distDir = new File(buildDir, "dist");
        File jarDir = new File(buildDir, "jars");
        File productDir = new File(modulesDir, 
                Util.path(new String[] {"build", "products", productName}));

        jarDir.mkdirs();
        distDir.mkdirs();

        Project project = new Project();
        project.setBaseDir(modulesDir);
        project.setName("testAspectjToolsbuild");
        project.addBuildListener(new EventBuildListener(Project.MSG_WARN));
        
        BuildModule bm = new BuildModule();
        bm.setProject(project);
        bm.setAssembleall(true);
        bm.setBuildConfig("");
        bm.setProductdir(new Path(project, productDir.getPath()));
        bm.setBasedir(new Path(project, modulesDir.getPath()));
        bm.setDistdir(new Path(project, distDir.getPath()));
        bm.setJardir(new Path(project, jarDir.getPath()));
        bm.setTrimtesting(true);
        bm.setBuildConfig("");
        bm.setVersion("1.2");
        bm.setFailonerror(true);
        bm.execute();
        
        File libDir = new File(distDir, "tools/lib");
        String[] jars = { "tools", "rt", "weaver", "lib"};
        for (int i = 0; i < jars.length; i++) {
            File jar = new File(libDir, "aspectj" + jars[i] + ".jar");
            assertTrue(jar.getPath(), jar.canRead()); 
            if (10 > jar.length()) {
                assertTrue(jar + " too small", false);
            }
        }
    }
    /**
     * Show messages from the task.
     * (normally shown by Ant's default listener)
     */
    static class EventBuildListener implements BuildListener {
        final int min;
        EventBuildListener(int min) {
            this.min = min;
        }
        public void buildFinished(BuildEvent event) {}
        public void buildStarted(BuildEvent event) {  }
        public void messageLogged(BuildEvent event) { 
            if (min <= event.getPriority()) {
                Task t = event.getTask();
                String src = (null == t ? "project" : t.getTaskName());
                System.out.println(src + ": " + event.getMessage());
            }
        }
        public void targetFinished(BuildEvent event) { }
        public void targetStarted(BuildEvent event) { }
        public void taskFinished(BuildEvent event) { }
        public void taskStarted(BuildEvent event) { }
        
    }    

}
