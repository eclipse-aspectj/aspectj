/* *******************************************************************
 * Copyright (c) 1999-2001 Xerox Corporation, 
 *               2002 Palo Alto Research Center, Incorporated (PARC),
 *               2005-2006 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 *     Wes Isberg     build tests
 * ******************************************************************/
package org.aspectj.internal.build;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
import org.aspectj.internal.tools.build.Result;
import org.aspectj.internal.tools.build.Util;
import org.aspectj.internal.tools.build.Result.Kind;

public class ModulesTest extends TestCase {
	
    public static final List<String> MODULE_NAMES;
    
    private static final File BASE_DIR = new File("..");
    
    static {
        String[] names = {
        "ajbrowser", "ajde", "ajdoc", "asm",
        "bridge", "loadtime", "org.aspectj.ajdt.core",
        "runtime", "taskdefs", "testing-client", "testing-util",
        "tests", "util", "weaver"};
        List<String> list = Arrays.asList(names);
        MODULE_NAMES = Collections.unmodifiableList(list);
    }

   private static boolean delete(File file) { // XXX Util
        if ((null == file) || !file.exists()) {
            return true;
        }
        if (file.isFile()) {
            return file.delete();
        } else {
            File[] files = file.listFiles();
            boolean result = true;
			for (File value : files) {
				if (!ModulesTest.delete(value)
						&& result) {
					result = false;
				}
			}
            return (file.delete() && result);
        }
    }

    List<File> tempFiles = new ArrayList<>();
     
	public ModulesTest(String name) {
		super(name);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		for (File file : tempFiles) {
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
        return new Modules(baseDir, jarDir, handler);
    }
      
    public void testAllModulesCreation() {
        List<Module> badModules = new ArrayList<>();
        for (String name: MODULE_NAMES) {
            File dir = new File(BASE_DIR, name);
            if (dir.isDirectory()) {
                File classpath = new File(dir, ".classpath");
                if (classpath.exists()) {
                    Modules modules = getModules(null);
                    Module module = modules.getModule(name);
                    if (!module.valid) {
                        badModules.add(module);
                    }
                }
            }
        }
        if (!badModules.isEmpty()) {
            StringBuffer sb = new StringBuffer();
            for (Module module: badModules) {
                System.err.println(module.toLongString());
                sb.append("\n");
                sb.append(module);
            }
            fail(sb.toString());
        }
    }
    
    void checkModule(String s) {
        if ("docs".equals(s) || "lib".equals(s)) {
            return;
        }
        Modules modules = getModules(null);
        Module module = modules.getModule(s);
        if (!module.valid) {
            assertTrue(module.toLongString(), false);
        }
    }
    
    public void xtestClasspathCreation() {
        Modules modules = getModules(null);
        
        Module ajdt = modules.getModule("org.aspectj.ajdt.core");
        assertTrue(ajdt.valid);
        
        Project project = new Project();
        AntBuilder builder = getBuilder(project);
        Path classpath = new Path(project);
        Kind kind = Result.kind(Result.NORMAL, !Result.ASSEMBLE);
        Result result = ajdt.getResult(kind);
        boolean hasLibraries = builder.setupClasspath(result, classpath);
        assertTrue(hasLibraries);
        if ((null == classpath) || (2 > classpath.size())) {
            assertTrue(classpath.toString(), false);
        }
    }
    
    /**
     * This test requires two OSGI modules:
     * org.aspectj.util, which optionally depends on tempaspectjrt.
     * Currently, the Ant builder does not handle linked source folders,
     * and the OSGI support does not work around optional plugins.
     */
    public void skip_testOSGIModuleCreation() {
        final String MODULE = "org.aspectj.util";
        final String USES_MODULE = "tempaspectjrt";
        
        Modules modules = getModules(null);
        Module newutil = modules.getModule(MODULE);
        assertTrue(newutil.valid);
        
        Project project = new Project();
        AntBuilder builder = getBuilder(project);
        Path classpath = new Path(project);
        Kind kind = Result.kind(Result.NORMAL, !Result.ASSEMBLE);
        Result result = newutil.getResult(kind);
        builder.setupClasspath(result, classpath);
        System.out.println(newutil + " classpath: " + classpath);
        if ((1 != classpath.size())) {
            assertTrue(classpath.toString(), false);
        }
        String cpEntry = classpath.list()[0];
        if (!cpEntry.endsWith(USES_MODULE + ".jar")) {
            fail("expected " + classpath + " to end with " + USES_MODULE + ".jar");
        }
    }

    private AntBuilder getBuilder(Project project) {
        project.setBaseDir(new File("."));
        project.setName("testOSGIModuleCreation");
        File tempDir = new File(".");
        return (AntBuilder) AntBuilder.getBuilder("", project, tempDir);        
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
		for (String s : jars) {
			File jar = new File(libDir, "aspectj" + s + ".jar");
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
