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
 * ******************************************************************/

package org.aspectj.internal.build;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import junit.framework.TestCase;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Commandline.Argument;
import org.aspectj.internal.tools.ant.taskdefs.BuildModule;
import org.aspectj.internal.tools.ant.taskdefs.Checklics;
import org.aspectj.internal.tools.build.Util;

/**
 * Test our integrated taskdef build.
 * This responds to two environment variables:
 * (1) run.build.tests must be defined before
 *   tests that build the tree (and hence take minutes)
 *   will run;
 * (2) build.config takes the same form as it does for the
 *   builder task, e.g., "useEclipseCompiles" will avoid
 *   recompiling with Javac and adopt classes in the 
 *   {module}/bin directories.
 */
public class BuildModuleTest extends TestCase {

    private static boolean printInfoMessages = false;
    private static boolean printedMessage;
    private static final boolean REMOVE_JARS_AFTER_DEBUGGING = true;
    // to just build one module verbosely
    private static final String[] DEBUGS  
        = {};

    // skip those requiring ajdoc, which requires tools.jar
    // also skip those requiring java5 unless manually set up
    // also skip big ones to avoid slowing the build too much
    private static final String[] SKIPS 
        //= {};
       = {"aspectjtools", "ajdoc", "aspectj5rt", "run-all-junit-tests",
        "ajbrowser", "testing", "testing-drivers", "org.aspectj.ajdt.core", "weaver"};

    private static final String SKIP_MESSAGE = 
        "BuildModuleTest: Define \"run.build.tests\" as a system "
        + "property to run tests to build ";
    private static final String BUILD_CONFIG;
    static {
        String config = null;
        try {
            config = System.getProperty("build.config");
        } catch (Throwable t) {
            // ignore
        }
        BUILD_CONFIG = config;
        if (printInfoMessages) {
            System.out.println("BuildModuleTest build.config: " + config);
        }
    }
        
    List<File> tempFiles = new ArrayList<>();
    private File jarDir;
    private boolean deleteJars;
    boolean building;  // must be enabled for tests to run
    
	public BuildModuleTest(String name) {
		super(name);
        building = Boolean.getBoolean("run.build.tests");
	}
    protected void setUp() {
        // change to view whether prior output is used
        deleteJars = true; // todo
    }

    protected void tearDown() throws Exception {
		super.tearDown();
        if (debugging() && !REMOVE_JARS_AFTER_DEBUGGING) {
            if (0 < tempFiles.size()) {
                System.err.println("debugging files left: " + tempFiles);
            }
            return;
        }
        deleteTempFiles();
	}
    
    protected void deleteTempFiles() {
		for (File file : tempFiles) {
			if (!Util.delete(file)) {
				File[] list = file.listFiles();
				if (!Util.isEmpty(list)) {
					StringBuffer sb = new StringBuffer();
					sb.append("warning: BuildModuleTest unable to delete ");
					sb.append(file.toString());
					sb.append("\n"); // XXX platform
					for (File value : list) {
						sb.append("  ");
						sb.append(value.toString());
						sb.append("\n"); // XXX platform
					}
					System.err.println(sb.toString());
				}
			}
		}
    }

    public void testAllJunitTests() {
      checkBuild("run-all-junit-tests");
    }

	public void testBuild() {
        checkBuild("build", 
            Checklics.class.getName(), 
            new String[0], // help message
            true); // ant needed
    }
    
    public void testUtil() {
        checkBuild("util");
    }

    public void testAsm() {
        checkBuild("asm");
    }
    
    public void testRuntime() {
        checkBuild("runtime");
    }

    public void testAspectj5rt() {
        checkBuild("aspectj5rt"); 
    }
   
//    public void testLocalOutOfDate() { 
//        Messager handler = new Messager();
//        File jarDir = new File("c:/home/ws/head/aj-build/jars");
//        File baseDir = new File("c:/home/ws/head");
//        Modules mods = new Modules(baseDir, jarDir, handler);
//        Module module = mods.getModule("ajbrowser");
//        Result r = module.getResult(Result.kind(true, true));
//        r.outOfDate();
//    }
//    public void testAspectj5rtRequired() {
//        File baseDir = new File("..");        
//        Modules modules = new Modules(baseDir, getJarDir(), new Messager());
//        Module module = modules.getModule("aspectj5rt");
//        Result result = module.getResult(Result.kind(true, true));
//        Result[] results = result.getRequired();
//        System.out.println(result.toLongString());
//        System.out.println("results: " + Arrays.asList(results));
//        deleteTempFiles();
//    }

    public void xtestNoDuplicates() {
        File weaverAllJar = null;
        try {
            weaverAllJar = doTask("weaver",true, true, true);
        } catch (Throwable t) {
            System.err.println(getClass() + ".testNoDuplicates() incomplete");
            t.printStackTrace(System.err);
            return;
        }
        String dupError = duplicateEntryError(weaverAllJar);
        weaverAllJar.delete();
        if (null != dupError) {
            fail(dupError);
        }
    }
    public void testAjbrowser() {
        checkBuild("ajbrowser", 
            "org.aspectj.tools.ajbrowser.Main",
            new String[] {"-noExit", "-version"}); // compiler version
    }
    public void testTestingUtils() {
        checkBuild("testing-util"); 
    }

    public void testAjdt() {
        checkBuild("org.aspectj.ajdt.core", 
           "org.aspectj.tools.ajc.Main",
            new String[] { "-noExit", "-version" });
    }//
    public void testTesting() {
        checkBuild("testing", 
            "org.aspectj.testing.util.LangUtilTest", 
            new String[] {"ignored"});
    }
    public void testTestingDrivers() {
        checkBuild("testing-drivers", 
            "org.aspectj.testing.drivers.Harness", 
            new String[] {"-help"});
    }
    public void testWeaver() {
        checkBuild("weaver"); 
    }
    
    // ajdoc relies on tools.jar
    public void testAspectjtools() {
        if (!shouldBuild("aspectjtools")) {
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
        if (!shouldBuild(productDir.getPath())) {
            return;
        }
        assertTrue(null != productDir);
        assertTrue(productDir.canRead());

        checkJavac();

        BuildModule task = new BuildModule();
        Project project = new Project();
        task.setProject(project);
        assertTrue(jarDir.canWrite() || jarDir.mkdirs());
        tempFiles.add(jarDir);
        task.setJardir(new Path(project, jarDir.getAbsolutePath()));
        task.setProductdir(new Path(project, productDir.getAbsolutePath()));
        task.setBasedir(new Path(project, baseDir.getAbsolutePath()));
        task.setDistdir(new Path(project, distDir.getAbsolutePath()));
        task.setFailonerror(true);
        if (null != BUILD_CONFIG) {
            task.setBuildConfig(BUILD_CONFIG);
        }
        //task.setVerbose(true);
        task.setCreateinstaller(true);
        task.execute();
        // now run installer and do product tests?
    }

    File getAntJar() {
        return new File("../lib/ant/lib/ant.jar");
    }
    File getJUnitJar() {
        return new File("../lib/junit/junit.jar");
    }
    
    File getJarDir() {
        if (null == jarDir) {
            File baseDir = new File("../aj-build/");
            if (!baseDir.canWrite()) {
                baseDir = new File(".");
            }
            jarDir = new File(baseDir, "BuildModuleTest-jars");
            tempFiles.add(jarDir);
        }
        if (!jarDir.exists()) {
            assertTrue(jarDir.mkdirs());
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
        File moduleDir = new File(Util.path("..", module));
        assertTrue(moduleDir.canRead());
        task.setModuledir(new Path(project, moduleDir.getAbsolutePath()));
        task.setJardir(new Path(project, jarDir.getAbsolutePath()));
        task.setFailonerror(true);
        if (null != BUILD_CONFIG) {
            task.setBuildConfig(BUILD_CONFIG);
        }
        return task;
    }
    
    void checkBuild(String module) { 
        checkBuild(module, null, null, false);
    }
    
    void checkBuild(String module, 
        String classname, 
        String[] args) {
        checkBuild(module, classname, args, true);
    }

    boolean shouldBuild(String target) {
        if (null == target) {
            return false;
        }
        if (!building && !printedMessage) {
            System.err.println(SKIP_MESSAGE + target + " (this is the only warning)");
            printedMessage = true;
        }
        if (debugging()) {
			for (String debug : DEBUGS) {
				if (target.equals(debug)) {
					return true;
				}
			}
            return false;
        } else {
			for (String skip : SKIPS) {
				if (skip.equals(target)) {
					if (printInfoMessages) {
						System.err.println(target + " skipped build test [" + getClass().getName() + ".shouldBuild(..)]");
					}
					return false;
				}
			}
        }
        return building;
    }
    private static boolean debugging() {
        return ((null != DEBUGS) && (0 < DEBUGS.length));
    }
    private static String duplicateEntryError(File weaverAllJar) {
        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(weaverAllJar);
            Enumeration e = zipFile.entries();
            List<String> entryNames = new ArrayList<>();
            while (e.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) e.nextElement();
                String name = entry.getName();
                if (entryNames.contains(name)) {
                    return "duplicate entry: " + name;
                }
                entryNames.add(name);
            }
        } catch (ZipException e) {
            return "ZipException " + e;
        } catch (IOException e) {
            return "IOException " + e;
        } finally {
            if (null != zipFile) {
                try {
                    zipFile.close();
                } catch (IOException e) {
                    return "IOException closing " + zipFile + ": " + e;
                }
            }
        }
        return null;
    }
    
    private static String name(String module, boolean trimTesting, boolean assemble) {
        return module + (trimTesting?"":"-test") + (assemble?"-all":"");
    }
    private void deleteJar(File jar) {
        if (!deleteJars) {
            return ;
        }
        if (jar.exists()) {
            jar.delete();
        }
        if (jar.exists()) {
            try {
                Thread.sleep(5000);
            } catch (Throwable t) {                
            }
        }
        if (jar.exists()) {
            assertTrue("cannot delete " + jar, jar.delete());
        }
    }
    void checkBuild(String module, 
        String classname, 
        String[] args,
        boolean addAnt) {
        if (!shouldBuild(module)) {
            return;
        }
        assertTrue(null != module);
        checkJavac();
        doTask(module, true, false);
        doTask(module, true, true);
        doTask(module, false, false);
        File jar = doTask(module, false, true, true);

        // verify if possible
        if (null != classname) {
            Java java = new Java();
            Project project = new Project();
            java.setProject(project);
            java.setFailonerror(true);
            Path cp = new Path(project);
            assertTrue(jar.canRead());
            cp.append(new Path(project, jar.getAbsolutePath()));
            if (addAnt) {
                cp.append(new Path(project, getAntJar().getAbsolutePath()));
                cp.append(new Path(project, getJUnitJar().getAbsolutePath()));
            }
            java.setClasspath(cp);
            java.setClassname(classname);
            if (null != args) {
				for (String s : args) {
					Argument arg = java.createArg();
					arg.setValue(s);
				}
            }
            try {
                java.execute();
            } catch (BuildException e) {
                e.printStackTrace(System.err);
                assertTrue("BuildException running " + classname, false);
            }
        }
        deleteJar(jar);        
    }
    void doTask(String module, boolean trimTesting, boolean assembleAll) {
        doTask(module, trimTesting, assembleAll, false);
    }

    File doTask(String module, boolean trimTesting, boolean assembleAll, boolean keepJars) {
        BuildModule task = getTask(module);
        String name = name(module, trimTesting, assembleAll);
        File jar = new File(getJarDir(), name+ ".jar");
        task.setAssembleall(assembleAll);
        task.setTrimtesting(trimTesting);
        task.execute();
        if (!jar.canRead()) {
            File[] files = getJarDir().listFiles();
            fail("cannot read " + jar + " in " + Arrays.asList(files));
        }
        if (!keepJars && deleteJars) {
            deleteTempFiles();
        }
        return jar;
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
